package auctionTalk.auction.global.common.exception.base;

import auctionTalk.auction.global.common.exception.dto.ErrorResponseDTO;

public interface BaseErrorCode {

    public ErrorResponseDTO getReason();

    public ErrorResponseDTO getReasonHttpStatus();
}