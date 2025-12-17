package auctionTalk.auction.domain.payment.service;

import auctionTalk.auction.domain.payment.dto.mapper.PaymentMapper;
import auctionTalk.auction.domain.payment.dto.request.AdminPaymentSearchRequest;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentQueryService {

    private final SubscriptionRepository subscriptionRepository;
    private final PropertyPaymentRepository propertyPaymentRepository;
    private final PaymentMapper paymentMapper;

    public AdminPaymentPagingResponse<AdminInquiryPayment> InquiryPayments(
            PaymentType type, LocalDate startDate, LocalDate endDate, int page, int size)
    {
        {
            Pageable pageable = PageRequest.of(page, size);

            LocalDateTime startAt = (startDate == null) ? null : startDate.atStartOfDay();
            LocalDateTime endAt = (endDate == null) ? null : endDate.atTime(LocalTime.MAX);

            return switch (type) {
                case SUBSCRIPTION -> {
                    Page<Subscription> result = findSubscriptionsByDate(startAt, endAt, pageable);
                    Page<AdminInquiryPayment> mapped =
                            result.map(paymentMapper::toAdminInquiryPaymentFromSubscription);

                    yield paymentMapper.toAdminPaymentPagingResponse(mapped);
                }

                case PROPERTY -> {
                    Page<PropertyPayment> result = findPropertyPaymentsByDate(startAt, endAt, pageable);
                    Page<AdminInquiryPayment> mapped =
                            result.map(paymentMapper::toAdminInquiryPaymentFromPropertyPayment);

                    yield paymentMapper.toAdminPaymentPagingResponse(mapped);
                }
            };
        }
    }

    private Page<Subscription> findSubscriptionsByDate(LocalDateTime startAt, LocalDateTime endAt, Pageable pageable) {
        if (startAt != null && endAt != null) {
            return subscriptionRepository.findAllByCreatedAtBetweenOrderByCreatedAtDesc(startAt, endAt, pageable);
        }
        if (startAt != null) {
            return subscriptionRepository.findAllByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(startAt, pageable);
        }
        if (endAt != null) {
            return subscriptionRepository.findAllByCreatedAtLessThanEqualOrderByCreatedAtDesc(endAt, pageable);
        }
        return subscriptionRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private Page<PropertyPayment> findPropertyPaymentsByDate(LocalDateTime startAt, LocalDateTime endAt, Pageable pageable) {
        if (startAt != null && endAt != null) {
            return propertyPaymentRepository.findAllByCreatedAtBetweenOrderByCreatedAtDesc(startAt, endAt, pageable);
        }
        if (startAt != null) {
            return propertyPaymentRepository.findAllByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(startAt, pageable);
        }
        if (endAt != null) {
            return propertyPaymentRepository.findAllByCreatedAtLessThanEqualOrderByCreatedAtDesc(endAt, pageable);
        }
        return propertyPaymentRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
