package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class PrincipalDetails implements UserDetails, OAuth2User {
    private Member member;
    private final boolean isRegistered;
    private Map<String, Object> attributes;

    // ✅ JWT 로그인용 (attributes만 넘기는 버전)
    public PrincipalDetails(Member member, Map<String, Object> attributes) {
        this.member = member;
        this.isRegistered = true;
        this.attributes = attributes != null ? attributes : Collections.emptyMap();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.<GrantedAuthority>of(
                new SimpleGrantedAuthority("ROLE_" + member.getRole().name()) // 🔹 기본 Role 추가
        );
    }

    @Override
    public String getPassword() {
        return null; // 비밀번호는 소셜 로그인만 사용
    }

    @Override
    public String getUsername() {
        return String.valueOf(member.getId());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(member.getId());
    }
}
