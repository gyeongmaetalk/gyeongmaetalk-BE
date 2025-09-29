package auctionTalk.auction.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clientId;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    private String name;

    private String cellPhone;

    private LocalDate birth;

    @Column(nullable = false)
    private boolean registered = false;

    @Enumerated(EnumType.STRING)
    private Role role;

    public void completeRegistration(String name, LocalDate birth, String cellPhone) {
        this.name = name;
        this.birth = birth;
        this.cellPhone = cellPhone;
        this.registered = true;
    }
}
