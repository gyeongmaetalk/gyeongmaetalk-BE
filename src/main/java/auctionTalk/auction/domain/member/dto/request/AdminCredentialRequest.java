package auctionTalk.auction.domain.member.dto.request;

/** 관리자 계정 생성·로그인 요청 본문 */
public record AdminCredentialRequest(String username, String password) {
}
