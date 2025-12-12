package auctionTalk.auction.domain.member.entity;

public enum LoginType {
    KAKAO, APPLE, LOCAL;

    public static LoginType from(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "kakao" -> KAKAO;
            case "apple" -> APPLE;
            default -> throw new IllegalArgumentException("지원하지 않는 로그인 타입: " + registrationId);
        };
    }
}