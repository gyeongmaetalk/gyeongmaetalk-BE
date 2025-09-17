package auctionTalk.auction.global.common.exception.ThrowClass;

import auctionTalk.auction.global.common.exception.base.BaseErrorCode;
import auctionTalk.auction.global.common.exception.base.GeneralException;

public class ReviewException extends GeneralException {

    public ReviewException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
