package auctionTalk.auction.domain.payment.mapper;

import auctionTalk.auction.domain.payment.dto.response.AdminInquiryPayment;
import auctionTalk.auction.domain.payment.dto.response.AdminPaymentPagingResponse;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.property.entity.PropertyPayment;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public AdminInquiryPayment toAdminInquiryPaymentFromSubscription(Subscription subscription) {
        return AdminInquiryPayment.builder()
                .payDate(subscription.getCreatedAt())
                .userName(subscription.getMember().getName())
                .cellPhone(subscription.getMember().getCellPhone())
                .paymentStatus(toPaymentStatusFromSubscription(subscription.getSubscriptionStatus()))
                .build();
    }

    public AdminInquiryPayment toAdminInquiryPaymentFromPropertyPayment(PropertyPayment payment) {
        return AdminInquiryPayment.builder()
                .payDate(payment.getCreatedAt())
                .userName(payment.getMember().getName())
                .cellPhone(payment.getMember().getCellPhone())
                .paymentStatus(payment.getStatus())
                .build();
    }

    public <T>AdminPaymentPagingResponse<T> toAdminPaymentPagingResponse(Page<T> payments) {
        return AdminPaymentPagingResponse.<T>builder()
                .payments(payments.getContent())
                .page(payments.getNumber())
                .totalPages(payments.getTotalPages())
                .totalElements((int) payments.getTotalElements())
                .isFirst(payments.isFirst())
                .isLast(payments.isLast())
                .build();
    }

    private PaymentStatus toPaymentStatusFromSubscription(SubscriptionStatus status) {
        return switch (status) {
            case PENDING -> PaymentStatus.READY;
            case IN_PROGRESS -> PaymentStatus.SUCCESS;
            case PAYMENT_FAILED, CANCELED -> PaymentStatus.FAIL;
            case COMPLETED -> null;
        };
    }
}
