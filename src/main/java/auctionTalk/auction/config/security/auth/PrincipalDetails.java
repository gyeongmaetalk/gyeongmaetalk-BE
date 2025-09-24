package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class PrincipalDetails implements UserDetails {
    private Member member;

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
}
