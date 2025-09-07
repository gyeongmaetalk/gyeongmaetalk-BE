package auctionTalk.auction.global.common.exception.base;

import auctionTalk.auction.global.common.exception.dto.ErrorResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public ErrorResponseDTO getErrorReason() {
        return this.errorCode.getReason();
    }

    public ErrorResponseDTO getErrorReasonHttpStatus() {
        return this.errorCode.getReasonHttpStatus();
    }

    public String getErrorCode(){
        return this.errorCode.getReason().getCode();
    }
}