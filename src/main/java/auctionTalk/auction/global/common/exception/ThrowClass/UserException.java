package auctionTalk.auction.global.common.exception.ThrowClass;

import auctionTalk.auction.global.common.exception.base.BaseErrorCode;
import auctionTalk.auction.global.common.exception.base.GeneralException;

public class UserException extends GeneralException {

    public UserException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
