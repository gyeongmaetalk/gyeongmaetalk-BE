package auctionTalk.auction.domain.payment.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.entity.*;
import auctionTalk.auction.domain.order.repository.OrderRepository;
import auctionTalk.auction.domain.payment.dto.response.PaymentVerificationResult;
import auctionTalk.auction.domain.payment.entity.*;
import auctionTalk.auction.domain.payment.mapper.PaymentMapper;
import auctionTalk.auction.domain.payment.repository.PaymentRepository;
import auctionTalk.auction.domain.payment.service.verify.*;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.product.entity.*;
import auctionTalk.auction.domain.product.service.ProductService;
import auctionTalk.auction.domain.viewticket.repository.*;
import auctionTalk.auction.domain.viewticket.mapper.ViewTicketMapper;
import auctionTalk.auction.domain.viewticket.service.*;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import auctionTalk.auction.domain.subscription.mapper.SubscriptionMapper;
import auctionTalk.auction.domain.subscription.service.*;
import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig(PaymentTransactionTest.Config.class)
class PaymentTransactionTest {
    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {
            PaymentRepository.class,
            OrderRepository.class,
            MemberViewTicketWalletRepository.class,
            SubscriptionRepository.class,
            CounselRepository.class,
            CounselorRepository.class
    })
    @Import({
            PaymentConfirmServiceImpl.class,
            PaymentVerificationServiceResolver.class,
            PaymentSuccessProcessor.class,
            PaymentFulfillmentProcessor.class, // 추가
            PaymentFulfillmentServiceImpl.class,
            PaymentMapper.class,
            ViewTicketServiceImpl.class,
            ViewTicketMapper.class,
            SubscriptionServiceImpl.class,
            SubscriptionMapper.class
    })
    static class Config {

        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(
                    "jdbc:h2:mem:payments;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
                    "sa",
                    ""
            );
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("auctionTalk.auction.domain");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of(
                    "hibernate.hbm2ddl.auto", "create-drop",
                    "hibernate.physical_naming_strategy",
                    "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"
            ));
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }

        // EntityManager @Bean 삭제

        @Bean
        ProductService productService() {
            return mock(ProductService.class);
        }

        @Bean
        PaymentVerificationService paymentVerifier() {
            return new PaymentVerificationService() {
                public PaymentProvider supportProvider() {
                    return PaymentProvider.REVENUECAT;
                }

                public PaymentVerificationResult verify(PaymentVerificationCommand command) {
                    assertThat(
                            org.springframework.transaction.support.TransactionSynchronizationManager
                                    .isActualTransactionActive()
                    ).isFalse();

                    return PaymentVerificationResult.builder()
                            .provider(command.provider())
                            .storeProductId(command.productIdentifier())
                            .providerTransactionId(command.transactionIdentifier())
                            .build();
                }
            };
        }
    }

    @Autowired PaymentSuccessProcessor processor;
    @Autowired PaymentConfirmService confirmService;
    @Autowired PaymentRepository payments;
    @Autowired MemberViewTicketWalletRepository wallets;
    @Autowired ViewTicketGrantHistoryRepository grants;
    @Autowired SubscriptionRepository subscriptions;
    @Autowired PlatformTransactionManager transactionManager;
    @PersistenceContext EntityManager em;

    record Fixture(Long orderId, Long paymentId, Long memberId) {}

    Fixture fixture(boolean subscription, boolean missingCounsel) {
        return fixture(subscription, missingCounsel, null);
    }

    Fixture fixture(boolean subscription, boolean missingCounsel, Long memberId) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            Member member = memberId == null ? Member.builder().clientId(UUID.randomUUID().toString()).build()
                    : em.find(Member.class, memberId);
            if (memberId == null) em.persist(member);
            Product product = Product.builder().name("product").storeProductId("product").build();
            em.persist(product);
            ProductComponent ticket = ProductComponent.builder().name("ticket")
                    .componentType(ProductComponentType.VIEW_TICKET).build();
            em.persist(ticket);
            ProductComponentMapping mapping = ProductComponentMapping.builder()
                    .product(product).component(ticket).quantity(10).build();
            em.persist(mapping);
            product.getComponentMappings().add(mapping);
            Counselor counselor = Counselor.builder().name("counselor").build();
            em.persist(counselor);
            if (subscription) {
                ProductComponent component = ProductComponent.builder().name("subscription")
                        .componentType(ProductComponentType.SUBSCRIPTION).build();
                em.persist(component);
                var subMapping = ProductComponentMapping.builder().product(product).component(component).quantity(1).build();
                em.persist(subMapping);
                product.getComponentMappings().add(subMapping);
                if (!missingCounsel) em.persist(Counsel.builder().member(member).counselor(counselor).build());
            }
            Order order = Order.builder().member(member).product(product).orderStatus(OrderStatus.READY)
                    .orderNumber(UUID.randomUUID().toString()).counselorId(counselor.getId()).amount(100L).build();
            em.persist(order);
            Payment payment = Payment.builder().order(order).paymentProvider(PaymentProvider.REVENUECAT)
                    .paymentNumber(UUID.randomUUID().toString()).paymentStatus(PaymentStatus.PENDING)
                    .storeProductId("product").requestedAmount(100L).build();
            em.persist(payment);
            return new Fixture(order.getId(), payment.getId(), member.getId());
        });
    }

    PaymentVerificationResult result(String tx) {
        return PaymentVerificationResult.builder().provider(PaymentProvider.REVENUECAT)
                .providerTransactionId(tx).storeProductId("product").build();
    }

    void complete(Fixture f, String tx) {
        PaymentConfirmRequest request = new PaymentConfirmRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "orderId", f.orderId());
        org.springframework.test.util.ReflectionTestUtils.setField(request, "productIdentifier", "product");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "transactionIdentifier", tx);
        confirmService.confirm(f.memberId(), request);
    }

    @Test void ticketsAreGrantedOnceOnReplay() {
        Fixture f = fixture(false, false);
        String tx = UUID.randomUUID().toString();
        complete(f, tx);
        complete(f, tx);
        assertThat(wallets.findBalanceByMemberId(f.memberId())).contains(10);
        assertThat(payments.findById(f.paymentId()).orElseThrow().getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test void subscriptionsAreCreatedOnceOnReplay() {
        Fixture f = fixture(true, false);
        String tx = UUID.randomUUID().toString();
        complete(f, tx);
        complete(f, tx);
        assertThat(subscriptions.existsBySourceOrderId(f.orderId())).isTrue();
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                assertThat(em.createQuery("select count(s) from Subscription s where s.sourceOrderId = :id", Long.class)
                        .setParameter("id", f.orderId()).getSingleResult()).isEqualTo(1L));
        assertThat(wallets.findBalanceByMemberId(f.memberId())).contains(10);
    }

    @Test
    void fulfillmentFailureKeepsPaymentSuccessAndRollsBackFulfillment() {
        Fixture f = fixture(true, true);
        String tx = UUID.randomUUID().toString();

        assertThatThrownBy(() -> complete(f, tx))
                .isInstanceOf(RuntimeException.class);

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Payment payment = em.find(Payment.class, f.paymentId());

            assertThat(payment.getPaymentStatus())
                    .isEqualTo(PaymentStatus.SUCCESS);

            assertThat(payment.getProviderTransactionId())
                    .isEqualTo(tx);

            assertThat(payment.getOrder().getOrderStatus())
                    .isEqualTo(OrderStatus.COMPLETED);

            assertThat(payment.getPaymentFulfillmentStatus())
                    .isEqualTo(PaymentFulfillmentStatus.PENDING);

            assertThat(wallets.findBalanceByMemberId(f.memberId()))
                    .isEmpty();

            assertThat(subscriptions.existsBySourceOrderId(f.orderId()))
                    .isFalse();

            assertThat(
                    em.createQuery(
                                    "select count(g) from ViewTicketGrantHistory g where g.order.id = :id",
                                    Long.class
                            )
                            .setParameter("id", f.orderId())
                            .getSingleResult()
            ).isZero();
        });
    }

    @Test void simultaneousConfirmGrantsTicketsOnce() throws Exception {
        Fixture f = fixture(false, false);
        String tx = UUID.randomUUID().toString();
        race(() -> complete(f, tx), () -> complete(f, tx), false);
        assertThat(wallets.findBalanceByMemberId(f.memberId())).contains(10);
    }

    @Test void simultaneousConfirmCreatesSubscriptionOnce() throws Exception {
        Fixture f = fixture(true, false);
        String tx = UUID.randomUUID().toString();
        race(() -> complete(f, tx), () -> complete(f, tx), false);
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                assertThat(em.createQuery("select count(s) from Subscription s where s.sourceOrderId = :id", Long.class)
                        .setParameter("id", f.orderId()).getSingleResult()).isEqualTo(1L));
    }

    @Test void differentOrdersForSameMemberDoNotLoseWalletIncrements() throws Exception {
        Fixture first = fixture(false, false);
        Fixture second = fixture(false, false, first.memberId());
        race(() -> complete(first, UUID.randomUUID().toString()), () -> complete(second, UUID.randomUUID().toString()), false);
        assertThat(wallets.findBalanceByMemberId(first.memberId())).contains(20);
    }

    @Test void sameTransactionAcrossOrdersCommitsOnlyOnce() throws Exception {
        Fixture first = fixture(false, false);
        Fixture second = fixture(false, false);
        String tx = UUID.randomUUID().toString();
        race(() -> complete(first, tx), () -> complete(second, tx), true);
        long successes = List.of(first, second).stream()
                .filter(f -> payments.findById(f.paymentId()).orElseThrow().isSuccess()).count();
        assertThat(successes).isEqualTo(1L);
        assertThat(wallets.findBalanceByMemberId(first.memberId()).orElse(0)
                + wallets.findBalanceByMemberId(second.memberId()).orElse(0)).isEqualTo(10);
    }

    @Test void databaseUniqueConstraintRejectsTransactionEvenWithoutApplicationCheck() {
        Fixture first = fixture(false, false);
        Fixture second = fixture(false, false);
        String tx = UUID.randomUUID().toString();
        complete(first, tx);
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Payment payment = em.find(Payment.class, second.paymentId());
            payment.markSuccess(PaymentProvider.REVENUECAT, tx, "product", null, null, null, null);
            em.flush();
        })).isInstanceOf(RuntimeException.class);
        assertThat(payments.findById(second.paymentId()).orElseThrow().getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    void race(Runnable first, Runnable second, boolean oneMustFail) throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        var barrier = new CyclicBarrier(2);
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (Runnable action : List.of(first, second)) {
                futures.add(executor.submit(() -> {
                    barrier.await(5, TimeUnit.SECONDS);
                    try { action.run(); return true; }
                    catch (RuntimeException e) {
                        if (!oneMustFail) throw e;
                        return false;
                    }
                }));
            }
            int succeeded = 0;
            for (Future<Boolean> future : futures) if (future.get(20, TimeUnit.SECONDS)) succeeded++;
            assertThat(succeeded).isEqualTo(oneMustFail ? 1 : 2);
        } finally {
            executor.shutdownNow();
        }
    }
}
