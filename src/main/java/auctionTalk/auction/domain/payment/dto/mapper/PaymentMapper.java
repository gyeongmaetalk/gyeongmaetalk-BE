package auctionTalk.auction.domain.payment.dto.mapper;

import auctionTalk.auction.domain.payment.dto.response.AdminInquiryPayment;
import auctionTalk.auction.domain.payment.dto.response.AdminPaymentPagingResponse;
import auctionTalk.auction.domain.property.entity.PropertyPayment;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public AdminInquiryPayment toAdminInquiryPaymentFromSubscription(Subscription subscription) {
        return AdminInquiryPayment.builder()
                .payDate(subscription.getCreatedAt())
                .amount(subscription.getAmount())
                .orderId(subscription.getOrderId())
                .paymentKey(subscription.getPaymentKey())
                .userName(subscription.getMember().getUsername())
                .cellPhone(subscription.getMember().getCellPhone())
                .build();
    }

    public AdminInquiryPayment toAdminInquiryPaymentFromPropertyPayment(PropertyPayment payment) {
        return AdminInquiryPayment.builder()
                .payDate(payment.getCreatedAt())
                .amount(payment.getAmount())
                .orderId(payment.getOrderId())
                .paymentKey(payment.getPaymentKey())
                .userName(payment.getMember().getUsername())
                .cellPhone(payment.getMember().getCellPhone())
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
}
