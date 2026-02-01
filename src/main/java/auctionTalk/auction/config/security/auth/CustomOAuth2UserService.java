package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.Role;
import auctionTalk.auction.domain.member.mapper.AuthMapper;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final AuthMapper authMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttribute = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String clientId = attributes.get(userNameAttribute).toString();

        Member member = memberRepository.findByClientIdAndLoginType(clientId, LoginType.from(registrationId))
                .orElseGet(() -> {
                    String name = extractName(registrationId, attributes);

                    Member newMember = authMapper.toMember(clientId, LoginType.from(registrationId));
                    if (StringUtils.hasText(name)) {
                        newMember.updateName(name);
                    }

                    return memberRepository.save(newMember);
                });

        return new PrincipalDetails(member, attributes);
    }

    private String extractName(String registrationId, Map<String, Object> attributes) {

        // Kakao
        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount =
                    (Map<String, Object>) attributes.get("kakao_account");

            if (kakaoAccount != null) {
                Map<String, Object> profile =
                        (Map<String, Object>) kakaoAccount.get("profile");

                if (profile != null) {
                    return (String) profile.get("nickname");
                }
            }
        }

        // fallback
        return "사용자";
    }

}