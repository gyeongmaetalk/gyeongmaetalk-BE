package auctionTalk.auction.global.common.exception.ThrowClass;

import auctionTalk.auction.global.common.exception.base.BaseErrorCode;
import auctionTalk.auction.global.common.exception.base.GeneralException;

public class CounselException extends GeneralException {

    public CounselException(BaseErrorCode errorCode) {
        super(errorCode);
    }

}
