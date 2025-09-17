package auctionTalk.auction.api.review.domain;

import auctionTalk.auction.api.counsel.domain.Counsel;
import auctionTalk.auction.api.review.presentation.dto.ReviewDto;
import auctionTalk.auction.api.user.domain.User;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Counsel counsel;

    private int score;

    private String content;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private List<ReviewImage> images = new ArrayList<>();

    public void updateReviewInfo(ReviewDto.ReviewUpdateRequestDto requestDto){
        this.score = requestDto.getScore();
        this.content = requestDto.getContent();
    }

    public void changeImages(List<ReviewImage> reviewImages) {
        // 새로운 이미지로 변경
        this.images = reviewImages;
    }

    public String getThumbnail() {
        return this.images.stream()
                .findFirst()
                .map(ReviewImage::getUrl)
                .orElse(null);
    }
}
