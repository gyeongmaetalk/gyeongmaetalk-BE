package auctionTalk.auction.domain.review.dto.response;

/**
 * 상담사 리뷰 개수와 평균 별점. DB에서 COUNT/AVG로 바로 계산한 값을 담는다.
 * 리뷰가 없으면 AVG가 null로 오므로 0으로 바꿔서 돌려준다.
 */
public record ReviewStats(Long reviewCount, Double averageScore) {

    public int count() {
        return reviewCount == null ? 0 : reviewCount.intValue();
    }

    public double average() {
        return averageScore == null ? 0.0 : averageScore;
    }
}
