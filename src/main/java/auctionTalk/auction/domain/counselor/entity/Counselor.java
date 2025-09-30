package auctionTalk.auction.domain.counselor.entity;

import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Counselor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String Specialization;

    private Integer experience;

    private String cellPhone;

    private String description;

    private String profileImage;

    private int counselCount;

    private String license;

    /**
     * 모든 상담사의 상담 가능 시간대 (10:00 ~ 20:30, 30분 단위 고정)
     */
    public List<LocalTime> getAvailableTimeSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(20, 30);

        while (!start.isAfter(end)) {
            slots.add(start);
            start = start.plusMinutes(30);
        }

        return slots;
    }

}