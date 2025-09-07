package auctionTalk.auction.global.common.exception.securityError;

import auctionTalk.auction.global.common.exception.base.GlobalErrorCode;
import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {

    public JwtAuthenticationException(GlobalErrorCode code) {
        super(code.name());
    }
}