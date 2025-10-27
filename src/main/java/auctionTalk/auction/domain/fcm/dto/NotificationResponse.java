package auctionTalk.auction.domain.fcm.dto;

import auctionTalk.auction.domain.fcm.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    @Schema(description = "알림 id")
    private Long id;

    @Schema(description = "제목", example = "리뷰 작성")
    private String title;

    @Schema(description = "내용", example = "무료 상담은 어떠셨나요? 후기를 남겨주세요")
    private String body;

    @Schema(description = "(리뷰 or 추천매물ID)", example = "1")
    private Long contentId;

    @Schema(description = "상담사 이름", example = "이정훈")
    private String counselorName;

    @Schema(description = "추천매물 이름(리뷰 알림일 경우 null)", example = "서울 역세권 30평 아파트")
    private String propertyName;

    @Schema(description = "추천매물 썸네일(리뷰 알림일 경우 null)", example = "https://s3.bucket/property_thumbnail.png")
    private String thumbnail;

    @Schema(description = "상담시간(추천 매물 알림일 경우 null)", example = "2025-10-26T16:30:00")
    private LocalDateTime counselTime;

    @Schema(description = "확인 여부", example = "false")
    private boolean isRead;

    @Schema(description = "알림 발송 시간", example = "2025-10-10T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "알림 타입", example = "RECOMMENDED_PROPERTY")
    private NotificationType type;

}
