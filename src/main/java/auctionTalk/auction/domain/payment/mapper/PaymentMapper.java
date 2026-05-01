package auctionTalk.auction.domain.payment.mapper;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.payment.dto.response.AdminInquiryPayment;
import auctionTalk.auction.domain.payment.dto.response.AdminPaymentPagingResponse;
import auctionTalk.auction.domain.payment.dto.response.PaymentConfirmResponse;
import auctionTalk.auction.domain.payment.entity.Payment;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.product.entity.Product;
import auctionTalk.auction.domain.property.entity.PropertyPayment;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toPayment(Order order, String paymentNumber, String storeProductId) {
        return Payment.builder()
                .order(order)
                .paymentNumber(paymentNumber)
                .paymentProvider(order.getPaymentProvider())
                .paymentStatus(PaymentStatus.PENDING)
                .storeProductId(storeProductId)
                .rawProviderPayload(null)
                .requestedAmount(order.getAmount())
                .build();
    }

    public PaymentConfirmResponse toPaymentConfirmResponse(Order order, Payment payment, Product product) {
        return PaymentConfirmResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .paymentNumber(payment.getPaymentNumber())
                .productId(product.getId())
                .productName(product.getName())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(payment.getPaymentStatus())
                .approvedAmount(payment.getApprovedAmount())
                .approvedAt(payment.getApprovedAt())
                .build();
    }

    public AdminInquiryPayment toAdminInquiryPaymentFromSubscription(Subscription subscription) {
        return AdminInquiryPayment.builder()
                .id(subscription.getId())
                .memberId(subscription.getMember().getId())
                .payDate(subscription.getCreatedAt())
                .userName(subscription.getMember().getName())
                .cellPhone(subscription.getMember().getCellPhone())
                .build();
    }

    public AdminInquiryPayment toAdminInquiryPaymentFromPropertyPayment(PropertyPayment payment) {
        return AdminInquiryPayment.builder()
                .id(payment.getProperty().getId())
                .memberId(payment.getMember().getId())
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
}
