package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.mapper.AuthMapper;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleOidcUserService extends OidcUserService {

    private final MemberRepository memberRepository;
    private final AuthMapper authMapper; // ✅ Member 생성 로직 통일

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        // Apple에서 id_token 파싱하여 OIDC 사용자 정보 획득
        OidcUser oidcUser = super.loadUser(userRequest);

        String appleSub = oidcUser.getSubject();
        log.info("🍎 Apple 로그인 시도: sub = {}", appleSub);

        // 기존 회원 조회 or 신규 생성
        Member member = memberRepository.findByClientIdAndLoginType(appleSub, LoginType.APPLE)
                .orElseGet(() -> {
                    Member newMember = authMapper.toMember(appleSub, LoginType.APPLE);
                    return memberRepository.save(newMember);
                });

        // PrincipalDetails로 통일하여 반환
        return new PrincipalDetails(member, oidcUser.getAttributes());
    }
}