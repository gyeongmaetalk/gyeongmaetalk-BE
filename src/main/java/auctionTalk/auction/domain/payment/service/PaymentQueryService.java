package auctionTalk.auction.domain.payment.service;

import auctionTalk.auction.domain.payment.dto.mapper.PaymentMapper;
import auctionTalk.auction.domain.payment.dto.response.AdminInquiryPayment;
import auctionTalk.auction.domain.payment.dto.response.AdminPaymentPagingResponse;
import auctionTalk.auction.domain.payment.entity.PaymentType;
import auctionTalk.auction.domain.property.entity.PropertyPayment;
import auctionTalk.auction.domain.property.repository.PropertyPaymentRepository;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentQueryService {

    private final SubscriptionRepository subscriptionRepository;
    private final PropertyPaymentRepository propertyPaymentRepository;
    private final PaymentMapper paymentMapper;

    public AdminPaymentPagingResponse<AdminInquiryPayment> InquiryPayments(PaymentType paymentType, int size, int page) {
        {
            Pageable pageable = PageRequest.of(page, size);

            return switch (paymentType) {
                case SUBSCRIPTION -> {
                    Page<Subscription> result = subscriptionRepository.findAllByOrderByCreatedAtDesc(pageable);

                    Page<AdminInquiryPayment> mapped =
                            result.map(paymentMapper::toAdminInquiryPaymentFromSubscription);

                    yield paymentMapper.toAdminPaymentPagingResponse(mapped);
                }

                case PROPERTY -> {
                    Page<PropertyPayment> result = propertyPaymentRepository.findAllByOrderByCreatedAtDesc(pageable);

                    Page<AdminInquiryPayment> mapped =
                            result.map(paymentMapper::toAdminInquiryPaymentFromPropertyPayment);

                    yield paymentMapper.toAdminPaymentPagingResponse(mapped);
                }
            };
        }
    }
}
