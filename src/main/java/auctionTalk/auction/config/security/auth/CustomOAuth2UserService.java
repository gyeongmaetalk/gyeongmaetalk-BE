package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.mapper.AuthMapper;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

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

        // DB에 존재하는지 확인
        Member member = memberRepository.findByClientIdAndLoginType(clientId, LoginType.from(registrationId))
                // 없으면 새로 생성
                .orElse(memberRepository.save(authMapper.toMember(clientId, LoginType.from(registrationId))));

        return new PrincipalDetails(member, attributes);
    }
}