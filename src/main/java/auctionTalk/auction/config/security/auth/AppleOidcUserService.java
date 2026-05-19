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
    private final AuthMapper authMapper;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        // Apple에서 id_token 파싱하여 OIDC 사용자 정보 획득
        OidcUser oidcUser = super.loadUser(userRequest);

        String appleSub = oidcUser.getSubject();
        log.info("🍎 Apple 로그인 시도: sub = {}", appleSub);

        String clientId = oidcUser.getSubject();
        LoginType loginType = LoginType.APPLE;

        Member member = memberRepository
                .findIdByClientIdAndLoginTypeIncludingDeleted(clientId, loginType.name())
                .map(memberId -> {
                    int restoredCount = memberRepository.restoreById(memberId);

                    log.info("[APPLE_MEMBER_RESTORE_RESULT] memberId={}, restoredCount={}",
                            memberId, restoredCount);

                    return memberRepository.findById(memberId)
                            .orElseThrow(() -> new IllegalStateException("복구한 회원을 다시 조회할 수 없습니다. memberId=" + memberId));
                })
                .orElseGet(() -> {
                    Member newMember = authMapper.toMember(clientId, loginType);
                    return memberRepository.save(newMember);
                });

        return new PrincipalDetails(member, oidcUser.getAttributes());
    }
}