package auctionTalk.auction.domain.review.entity;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.dto.request.ReviewUpdateRequest;
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
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Counsel counsel;

    private int score;

    private String content;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    public void updateReviewInfo(ReviewUpdateRequest requestDto){
        this.score = requestDto.getScore();
        this.content = requestDto.getContent();
    }

    public List<String> updateImages(List<String> remainKeys, List<String> addKeys) {

        // 🔥 삭제될 key 목록 수집
        List<String> deleteKeys = this.images.stream()
                .map(ReviewImage::getUrl)
                .filter(key -> !remainKeys.contains(key))
                .toList();

        // 1) DB 삭제 (orphanRemoval=true일 경우 ReviewImage remove로 자동 삭제)
        this.images.removeIf(img -> !remainKeys.contains(img.getUrl()));

        // 2) 추가
        if (addKeys != null) {
            for (String key : addKeys) {
                ReviewImage newImg = ReviewImage.builder()
                        .url(key)
                        .review(this)
                        .build();
                this.images.add(newImg);
            }
        }

        // 🔥 삭제해야 하는 key들을 서비스로 전달
        return deleteKeys;
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
