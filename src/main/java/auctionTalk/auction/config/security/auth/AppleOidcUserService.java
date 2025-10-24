package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.mapper.AuthMapper;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppleOidcUserService extends OidcUserService {
    private final MemberRepository memberRepository;
    private final AuthMapper authMapper;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest); // id_token 검증 포함
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "apple"
        String clientId = oidcUser.getSubject(); // = "sub"

        Member member = memberRepository.findByClientIdAndLoginType(clientId, LoginType.from(registrationId))
                .orElseGet(() -> memberRepository.save(authMapper.toMember(clientId, LoginType.from(registrationId))));

        return new DefaultOidcUser(
                oidcUser.getAuthorities(),     // 권한 그대로
                oidcUser.getIdToken(),         // id_token
                oidcUser.getUserInfo(),        // user_info
                "sub"                          // username attribute key
        );
    }
}