package auctionTalk.auction.domain.payment.infrastructure.revenuecat;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.domain.payment.infrastructure.revenuecat.dto.RevenueCatCustomerResponse;
import auctionTalk.auction.domain.payment.service.verify.PaymentVerificationCommand;
import auctionTalk.auction.global.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RevenueCatPaymentAdapterTest {
    RevenueCatClient client = mock(RevenueCatClient.class);
    MemberRepository members = mock(MemberRepository.class);
    RevenueCatPurchaseVerifier verifier = new RevenueCatPurchaseVerifier();
    RevenueCatPaymentAdapter adapter = new RevenueCatPaymentAdapter(client, verifier, members);
    PaymentVerificationCommand command = new PaymentVerificationCommand(1L, 2L, PaymentProvider.REVENUECAT,
            "ticket", "123", 100L);

    RevenueCatCustomerResponse response(String subscriber) throws Exception {
        return new ObjectMapper().readValue("{\"subscriber\":" + subscriber + "}", RevenueCatCustomerResponse.class);
    }

    @Test void purchaseIsMappedToCommonResultWithCanonicalTransaction() throws Exception {
        when(members.getMember(1L)).thenReturn(Member.builder().revenueCatAppUserId("rc-user").build());
        when(client.getCustomer("rc-user")).thenReturn(response("""
                {"non_subscriptions":{"ticket":[{"id":"rc_123","store_transaction_id":"store_123",
                "store":"app_store","is_sandbox":true,"purchase_date":"2026-05-01T10:00:00Z"}]}}
                """));
        var result = adapter.verify(command);
        assertThat(result.getProvider()).isEqualTo(PaymentProvider.REVENUECAT);
        assertThat(result.getProviderTransactionId()).isEqualTo("store_123");
        assertThat(result.getStoreProductId()).isEqualTo("ticket");
        assertThat(result.getStore()).isEqualTo("app_store");
        assertThat(result.getSandbox()).isTrue();
        assertThat(result.getApprovedAt()).isEqualTo("2026-05-01T10:00:00");
    }

    @Test void unknownProductIsRejected() throws Exception {
        var response = response("{\"non_subscriptions\":{\"other\":[]}}");
        assertThatThrownBy(() -> verifier.verifyNonSubscriptionPurchase(response, "ticket", "123"))
                .isInstanceOf(CustomApiException.class);
    }

    @Test void unmatchedTransactionIsRejectedEvenWithActiveEntitlement() throws Exception {
        var response = response("""
                {"entitlements":{"premium":{"product_identifier":"ticket","expires_date":null}},
                "non_subscriptions":{"ticket":[{"id":"another"}]}}
                """);
        assertThatThrownBy(() -> verifier.verifyNonSubscriptionPurchase(response, "ticket", "123"))
                .isInstanceOfSatisfying(CustomApiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.REVENUECAT_PURCHASE_NOT_FOUND));
    }

    @Test void malformedResponseIsRejected() {
        assertThatThrownBy(() -> verifier.verifyNonSubscriptionPurchase(null, "ticket", "123"))
                .isInstanceOf(CustomApiException.class);
    }

    @Test void transientFailureIsRetriedInsideAdapter() throws Exception {
        when(members.getMember(1L)).thenReturn(Member.builder().revenueCatAppUserId("rc-user").build());
        when(client.getCustomer("rc-user")).thenThrow(new CustomApiException(ErrorCode.REVENUECAT_VERIFICATION_FAILED))
                .thenReturn(response("{\"non_subscriptions\":{\"ticket\":[{\"id\":\"123\"}]}}"));
        assertThat(adapter.verify(command).getProviderTransactionId()).isEqualTo("123");
        verify(client, times(2)).getCustomer("rc-user");
    }

    @Test void exhaustedRetriesPropagateVerificationFailure() {
        when(members.getMember(1L)).thenReturn(Member.builder().revenueCatAppUserId("rc-user").build());
        when(client.getCustomer("rc-user")).thenThrow(new CustomApiException(ErrorCode.REVENUECAT_VERIFICATION_FAILED));
        assertThatThrownBy(() -> adapter.verify(command)).isInstanceOf(CustomApiException.class);
        verify(client, times(5)).getCustomer("rc-user");
    }
}
