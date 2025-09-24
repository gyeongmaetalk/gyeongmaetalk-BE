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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String cellPhone;

    @Column(nullable = false)
    private LocalDate birth;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Enumerated(EnumType.STRING)
    private Role role;
}
