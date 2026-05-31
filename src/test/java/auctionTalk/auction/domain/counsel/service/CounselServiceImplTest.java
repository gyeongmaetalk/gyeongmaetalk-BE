package auctionTalk.auction.domain.counsel.service;

import auctionTalk.auction.domain.counsel.dto.request.CounselApplyRequest;
import auctionTalk.auction.domain.counsel.dto.request.CounselFormCreateRequest;
import auctionTalk.auction.domain.counsel.dto.request.CounselFormUpdateRequest;
import auctionTalk.auction.domain.counsel.dto.response.*;
import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselForm;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.counsel.mapper.CounselMapper;
import auctionTalk.auction.domain.counsel.repository.CounselFormRepository;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.Role;
import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.domain.review.repository.ReviewRepository;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CounselServiceImplTest {

    @InjectMocks
    private CounselServiceImpl counselService;

    @Mock
    private CounselMapper counselMapper;

    @Mock
    private CounselRepository counselRepository;

    @Mock
    private CounselorRepository counselorRepository;

    @Mock
    private CounselFormRepository counselFormRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ReviewRepository reviewRepository;

    private Member member;
    private Counselor counselor;
    private CounselForm counselForm;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .clientId("test-client-id")
                .role(Role.USER)
                .build();

        counselor = Counselor.builder()
                .id(1L)
                .name("테스트 상담사")
                .description("10년 경력의 상담사입니다.")
                .experience(10)
                .counselCount(50)
                .license("경매지도사")
                .Specialization("아파트 경매")
                .cellPhone("010-1234-5678")
                .build();

        counselForm = CounselForm.builder()
                .id(1L)
                .member(member)
                .purpose("투자 목적")
                .area("수도권")
                .serviceType("서류 준비")
                .interest("아파트 경매")
                .participantType("개인")
                .build();
    }

    @Nested
    @DisplayName("상담사 매칭")
    class MatchCounselorTest {

        @Test
        @DisplayName("구독 이력이 없는 사용자는 상담사 매칭 성공")
        void matchCounselor_success() {
            CounselFormCreateRequest request = new CounselFormCreateRequest(
                    "투자 목적", "수도권", "서류 준비", "아파트 경매", "개인"
            );

            MatchCounselorResponse expectedResponse = MatchCounselorResponse.builder()
                    .counselorId(1L)
                    .counselorName("테스트 상담사")
                    .score(0.0)
                    .reviewCount(0)
                    .counselCount(50)
                    .build();

            given(subscriptionRepository.existsByMemberAndSubscriptionStatus(member, SubscriptionStatus.IN_PROGRESS))
                    .willReturn(false);
            given(counselorRepository.getCounselor(1L)).willReturn(counselor);
            given(reviewRepository.findAllByCounsel_Counselor(counselor)).willReturn(List.of());
            given(counselMapper.toMatchCounselorResponse(counselor, 0.0, 0)).willReturn(expectedResponse);

            MatchCounselorResponse result = counselService.matchCounselor(request, member);

            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.getCounselorId()).isEqualTo(1L);
            assertThat(result.getCounselorName()).isEqualTo("테스트 상담사");

            then(subscriptionRepository).should()
                    .existsByMemberAndSubscriptionStatus(member, SubscriptionStatus.IN_PROGRESS);
            then(counselorRepository).should().getCounselor(1L);
            then(counselMapper).should().toMatchCounselorResponse(counselor, 0.0, 0);
        }

        @Test
        @DisplayName("구독 중인 사용자는 새로운 상담사 매칭을 요청 불가")
        void matchCounselor_fail_whenMemberIsSubscribed() {
            CounselFormCreateRequest request = new CounselFormCreateRequest(
                    "투자 목적", "수도권", "서류 준비", "아파트 경매", "개인"
            );

            given(subscriptionRepository.existsByMemberAndSubscriptionStatus(member, SubscriptionStatus.IN_PROGRESS))
                    .willReturn(true);

            assertThatThrownBy(() -> counselService.matchCounselor(request, member))
                    .isInstanceOf(CustomApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.MEMBER_IS_SUBSCRIBED);

            then(counselorRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("상담 폼 수정")
    class UpdateCounselFormTest {

        @Test
        @DisplayName("상담 폼 수정 요청이 들어오면 기존 상담 폼 정보가 요청값으로 갱신")
        void updateCounselForm_success() {
            Long counselFormId = 1L;

            CounselFormUpdateRequest request = new CounselFormUpdateRequest(
                    "실거주 목적", "서울", "명도까지 전반적으로", "아파트 경매", "법인"
            );

            CounselUpdateResponse expectedResponse = CounselUpdateResponse.builder()
                    .counselFormId(counselFormId)
                    .counselorId(1L)
                    .counselorName("테스트 상담사")
                    .score(0.0)
                    .reviewCount(0)
                    .build();

            given(counselorRepository.getCounselor(1L)).willReturn(counselor);
            given(counselFormRepository.getCounselFormById(counselFormId)).willReturn(counselForm);
            given(counselFormRepository.save(any(CounselForm.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(reviewRepository.findAllByCounsel_Counselor(counselor)).willReturn(List.of());
            given(counselMapper.toCounselUpdateResponse(counselor, counselFormId, 0.0, 0))
                    .willReturn(expectedResponse);

            CounselUpdateResponse result = counselService.updateCounselForm(counselFormId, request, member);

            assertThat(result).isEqualTo(expectedResponse);
            assertThat(counselForm.getPurpose()).isEqualTo("실거주 목적");
            assertThat(counselForm.getArea()).isEqualTo("서울");
            assertThat(counselForm.getServiceType()).isEqualTo("명도까지 전반적으로");
            assertThat(counselForm.getInterest()).isEqualTo("아파트 경매");
            assertThat(counselForm.getParticipantType()).isEqualTo("법인");

            then(counselFormRepository).should().save(counselForm);
            then(counselMapper).should().toCounselUpdateResponse(counselor, counselFormId, 0.0, 0);
        }

        @Test
        @DisplayName("존재하지 않는 상담 폼을 수정하려고 하면 상담 정보를 찾을 수 없다는 예외가 발생")
        void updateCounselForm_fail_whenCounselFormNotFound() {
            Long invalidCounselFormId = 999L;

            CounselFormUpdateRequest request = new CounselFormUpdateRequest(
                    "실거주 목적", "서울", "명도까지 전반적으로", "아파트 경매", "법인"
            );

            given(counselorRepository.getCounselor(1L)).willReturn(counselor);
            given(counselFormRepository.getCounselFormById(invalidCounselFormId))
                    .willThrow(new CustomApiException(ErrorCode.COUNSEL_NOT_FOUND));

            assertThatThrownBy(() -> counselService.updateCounselForm(invalidCounselFormId, request, member))
                    .isInstanceOf(CustomApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.COUNSEL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("상담 신청")
    class ApplyCounselTest {

        @Test
        @DisplayName("상담 신청 시 요청한 날짜와 시간이 분리되어 상담 정보로 저장")
        void applyCounsel_success() {
            Long counselorId = 1L;
            LocalDateTime counselDateTime = LocalDateTime.of(2025, 12, 30, 10, 0);
            LocalDate expectedDate = counselDateTime.toLocalDate();
            LocalTime expectedTime = counselDateTime.toLocalTime();

            CounselFormCreateRequest formRequest = new CounselFormCreateRequest(
                    "투자 목적", "수도권", "서류 준비", "아파트 경매", "개인"
            );
            CounselApplyRequest request = new CounselApplyRequest(formRequest, counselDateTime);

            Counsel counsel = Counsel.builder()
                    .id(1L)
                    .member(member)
                    .counselor(counselor)
                    .counselForm(counselForm)
                    .counselDate(expectedDate)
                    .counselTime(expectedTime)
                    .counselStatus(CounselStatus.COUNSEL_BEFORE)
                    .build();

            ApplyCounselResponse expectedResponse = ApplyCounselResponse.builder()
                    .counselId(1L)
                    .counselFormId(1L)
                    .counselDate(expectedDate)
                    .counselTime(expectedTime)
                    .cellPhone("010-1234-5678")
                    .build();

            given(counselorRepository.getCounselor(counselorId)).willReturn(counselor);
            given(counselMapper.toCounselForm(formRequest, member)).willReturn(counselForm);
            given(counselFormRepository.save(counselForm)).willReturn(counselForm);
            given(counselMapper.toCounsel(member, counselor, expectedDate, expectedTime, counselForm))
                    .willReturn(counsel);
            given(counselRepository.save(counsel)).willReturn(counsel);
            given(counselMapper.toApplyCounselResponse(1L, counselForm, expectedDate, expectedTime, counselor))
                    .willReturn(expectedResponse);

            ApplyCounselResponse result = counselService.applyCounsel(member, counselorId, request);

            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.getCounselDate()).isEqualTo(expectedDate);
            assertThat(result.getCounselTime()).isEqualTo(expectedTime);

            then(counselMapper).should().toCounselForm(formRequest, member);
            then(counselMapper).should().toCounsel(member, counselor, expectedDate, expectedTime, counselForm);
            then(counselMapper).should()
                    .toApplyCounselResponse(1L, counselForm, expectedDate, expectedTime, counselor);

            var inOrder = inOrder(counselFormRepository, counselRepository);
            inOrder.verify(counselFormRepository).save(counselForm);
            inOrder.verify(counselRepository).save(counsel);
        }
    }

    @Nested
    @DisplayName("상담 일정 변경")
    class UpdateApplyCounselTest {

        @Test
        @DisplayName("상담 일정 변경 시 기존 상담의 날짜와 시간이 함께 변경")
        void updateApplyCounsel_success() {
            Long counselId = 1L;
            Long counselFormId = 1L;
            Long counselorId = 1L;
            LocalDate newDate = LocalDate.of(2025, 12, 31);
            LocalTime newTime = LocalTime.of(14, 0);

            Counsel counsel = Counsel.builder()
                    .id(counselId)
                    .member(member)
                    .counselor(counselor)
                    .counselForm(counselForm)
                    .counselDate(LocalDate.of(2025, 12, 30))
                    .counselTime(LocalTime.of(10, 0))
                    .counselStatus(CounselStatus.COUNSEL_BEFORE)
                    .build();

            ApplyCounselResponse expectedResponse = ApplyCounselResponse.builder()
                    .counselId(counselId)
                    .counselFormId(counselFormId)
                    .counselDate(newDate)
                    .counselTime(newTime)
                    .build();

            given(counselorRepository.getCounselor(counselorId)).willReturn(counselor);
            given(counselFormRepository.getCounselFormById(counselFormId)).willReturn(counselForm);
            given(counselRepository.getCounselById(counselId)).willReturn(counsel);
            given(counselRepository.save(any(Counsel.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(counselMapper.toApplyCounselResponse(counselId, counselForm, newDate, newTime, counsel.getCounselor()))
                    .willReturn(expectedResponse);

            ApplyCounselResponse result = counselService.updateApplyCounsel(
                    counselId, counselFormId, member, counselorId, newDate, newTime
            );

            assertThat(result).isEqualTo(expectedResponse);
            assertThat(counsel.getCounselDate()).isEqualTo(newDate);
            assertThat(counsel.getCounselTime()).isEqualTo(newTime);

            then(counselRepository).should().save(counsel);
            then(counselMapper).should()
                    .toApplyCounselResponse(counselId, counselForm, newDate, newTime, counsel.getCounselor());
        }

        @Test
        @DisplayName("존재하지 않는 상담 일정을 변경하려고 하면 상담 정보를 찾을 수 없다는 예외가 발생")
        void updateApplyCounsel_fail_whenCounselNotFound() {
            Long invalidCounselId = 999L;

            given(counselorRepository.getCounselor(1L)).willReturn(counselor);
            given(counselFormRepository.getCounselFormById(1L)).willReturn(counselForm);
            given(counselRepository.getCounselById(invalidCounselId))
                    .willThrow(new CustomApiException(ErrorCode.COUNSEL_NOT_FOUND));

            assertThatThrownBy(() -> counselService.updateApplyCounsel(
                    invalidCounselId, 1L, member, 1L,
                    LocalDate.of(2025, 12, 31), LocalTime.of(14, 0)
            ))
                    .isInstanceOf(CustomApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.COUNSEL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("상담 가능 시간 조회")
    class InquiryPossibleTimeTest {

        @Test
        @DisplayName("예약이 없는 날에는 상담사의 전체 가능 시간이 반환")
        void inquiryPossibleTime_noReservations_returnsAllSlots() {
            LocalDate date = LocalDate.of(2025, 12, 30);
            List<LocalTime> allSlots = counselor.getAvailableTimeSlots();

            given(counselorRepository.getCounselor(1L)).willReturn(counselor);
            given(counselRepository.findReservedTimes(1L, date)).willReturn(List.of());

            List<LocalTime> result = counselService.inquiryPossibleTime(1L, date);

            assertThat(result).hasSize(allSlots.size());
            assertThat(result).containsExactlyElementsOf(allSlots);
        }

        @Test
        @DisplayName("이미 예약된 시간은 상담 가능 시간 목록에서 제외")
        void inquiryPossibleTime_withReservations_excludesReservedTimes() {
            LocalDate date = LocalDate.of(2025, 12, 30);
            LocalTime reservedTime1 = LocalTime.of(10, 0);
            LocalTime reservedTime2 = LocalTime.of(11, 0);

            given(counselorRepository.getCounselor(1L)).willReturn(counselor);
            given(counselRepository.findReservedTimes(1L, date))
                    .willReturn(List.of(reservedTime1, reservedTime2));

            List<LocalTime> result = counselService.inquiryPossibleTime(1L, date);

            assertThat(result).doesNotContain(reservedTime1, reservedTime2);
            assertThat(result).hasSize(counselor.getAvailableTimeSlots().size() - 2);
        }

        @Test
        @DisplayName("해당 날짜의 모든 시간이 예약되어 있으면 빈 목록을 반환")
        void inquiryPossibleTime_allReserved_returnsEmptyList() {
            LocalDate date = LocalDate.of(2025, 12, 30);
            List<LocalTime> allSlots = counselor.getAvailableTimeSlots();

            given(counselorRepository.getCounselor(1L)).willReturn(counselor);
            given(counselRepository.findReservedTimes(1L, date)).willReturn(allSlots);

            List<LocalTime> result = counselService.inquiryPossibleTime(1L, date);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("상담 정보 조회")
    class GetCounselInfoTest {

        @Test
        @DisplayName("진행 중인 상담이 없으면 NONE 상태와 함께 빈 상담 정보를 반환")
        void getCounselInfo_whenNoCounsel_returnsNoneStatus() {
            given(counselRepository.findByMember(member)).willReturn(Optional.empty());

            CounselCombinedResponse result = counselService.getCounselInfo(member);

            assertThat(result.getStatus()).isEqualTo(CounselStatus.NONE);
            assertThat(result.getInfo()).isNull();

            then(counselFormRepository).shouldHaveNoInteractions();
            then(reviewRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("진행 중인 상담이 있으면 상담 상태와 상세 정보를 함께 반환")
        void getCounselInfo_whenCounselExists_returnsCounselInfo() {
            LocalDate counselDate = LocalDate.of(2025, 12, 30);
            LocalTime counselTime = LocalTime.of(10, 0);

            Counsel existingCounsel = Counsel.builder()
                    .id(1L)
                    .member(member)
                    .counselor(counselor)
                    .counselForm(counselForm)
                    .counselDate(counselDate)
                    .counselTime(counselTime)
                    .counselStatus(CounselStatus.COUNSEL_BEFORE)
                    .build();

            CounselInfoResponse infoResponse = CounselInfoResponse.builder()
                    .counselId(1L)
                    .counselorId(1L)
                    .counselorName("테스트 상담사")
                    .counselDate(counselDate)
                    .counselTime(counselTime)
                    .build();

            given(counselRepository.findByMember(member)).willReturn(Optional.of(existingCounsel));
            given(counselFormRepository.getCounselFormByMember(member)).willReturn(counselForm);
            given(reviewRepository.findAllByCounsel_Counselor(counselor)).willReturn(List.of());
            given(reviewRepository.existsByCounsel(existingCounsel)).willReturn(false);
            given(counselMapper.toCounselInfoResponse(
                    1L, counselor, counselForm, counselDate, counselTime, false, 0.0, 0
            )).willReturn(infoResponse);

            CounselCombinedResponse result = counselService.getCounselInfo(member);

            assertThat(result.getStatus()).isEqualTo(CounselStatus.COUNSEL_BEFORE);
            assertThat(result.getInfo()).isNotNull();
            assertThat(result.getInfo().getCounselId()).isEqualTo(1L);
            assertThat(result.getInfo().getCounselorName()).isEqualTo("테스트 상담사");

            then(counselMapper).should().toCounselInfoResponse(
                    1L, counselor, counselForm, counselDate, counselTime, false, 0.0, 0
            );
        }

        @Test
        @DisplayName("리뷰가 존재하면 평균 점수와 리뷰 수를 계산해 상담 정보에 반영")
        void getCounselInfo_withReviews_calculatesAverageScore() {
            LocalDate counselDate = LocalDate.of(2025, 12, 30);
            LocalTime counselTime = LocalTime.of(10, 0);

            Counsel existingCounsel = Counsel.builder()
                    .id(1L)
                    .member(member)
                    .counselor(counselor)
                    .counselForm(counselForm)
                    .counselDate(counselDate)
                    .counselTime(counselTime)
                    .counselStatus(CounselStatus.COUNSEL_AFTER)
                    .build();

            Review mockReview1 = mock(Review.class);
            Review mockReview2 = mock(Review.class);
            given(mockReview1.getScore()).willReturn(4);
            given(mockReview2.getScore()).willReturn(5);

            CounselInfoResponse infoResponse = CounselInfoResponse.builder()
                    .counselId(1L)
                    .counselorId(1L)
                    .counselorName("테스트 상담사")
                    .score(4.5)
                    .reviewCount(2)
                    .build();

            given(counselRepository.findByMember(member)).willReturn(Optional.of(existingCounsel));
            given(counselFormRepository.getCounselFormByMember(member)).willReturn(counselForm);
            given(reviewRepository.findAllByCounsel_Counselor(counselor))
                    .willReturn(List.of(mockReview1, mockReview2));
            given(reviewRepository.existsByCounsel(existingCounsel)).willReturn(true);
            given(counselMapper.toCounselInfoResponse(
                    1L, counselor, counselForm, counselDate, counselTime, true, 4.5, 2
            )).willReturn(infoResponse);

            CounselCombinedResponse result = counselService.getCounselInfo(member);

            assertThat(result.getStatus()).isEqualTo(CounselStatus.COUNSEL_AFTER);
            assertThat(result.getInfo().getScore()).isEqualTo(4.5);
            assertThat(result.getInfo().getReviewCount()).isEqualTo(2);

            then(counselMapper).should().toCounselInfoResponse(
                    1L, counselor, counselForm, counselDate, counselTime, true, 4.5, 2
            );
        }
    }
}