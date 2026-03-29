package auctionTalk.auction.domain.counsel.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.counsel.dto.request.CounselApplyRequest;
import auctionTalk.auction.domain.counsel.dto.request.CounselFormCreateRequest;
import auctionTalk.auction.domain.counsel.dto.request.CounselFormUpdateRequest;
import auctionTalk.auction.domain.counsel.dto.response.*;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.counsel.service.CounselService;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.Role;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CounselController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(CounselControllerTest.TestMvcConfig.class)
class CounselControllerTest {

    @TestConfiguration
    static class TestMvcConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CounselService counselService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private ObjectMapper objectMapper;
    private Member member;
    private PrincipalDetails principalDetails;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        member = Member.builder()
                .id(1L)
                .clientId("test-client-id")
                .role(Role.USER)
                .build();

        principalDetails = new PrincipalDetails(member, Map.of());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principalDetails, null, principalDetails.getAuthorities()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("상담사 매칭 요청이 들어오면 추천 상담사 정보를 응답")
    void matchCounselor_success() throws Exception {
        CounselFormCreateRequest request = new CounselFormCreateRequest(
                "투자 목적", "수도권", "서류 준비", "아파트 경매", "개인"
        );

        MatchCounselorResponse response = MatchCounselorResponse.builder()
                .counselorId(1L)
                .counselorName("테스트 상담사")
                .score(4.5)
                .reviewCount(10)
                .counselCount(50)
                .description("10년 경력의 상담사입니다.")
                .build();

        given(counselService.matchCounselor(any(CounselFormCreateRequest.class), any(Member.class)))
                .willReturn(response);

        mockMvc.perform(post("/counsels/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.counselorId").value(1L))
                .andExpect(jsonPath("$.result.counselorName").value("테스트 상담사"))
                .andExpect(jsonPath("$.result.score").value(4.5));

        then(counselService).should().matchCounselor(any(CounselFormCreateRequest.class), any(Member.class));
    }

    @Test
    @DisplayName("상담사 매칭 중 비즈니스 예외가 발생하면 실패 응답을 반환")
    void matchCounselor_fail_serviceThrowsCustomException() throws Exception {
        CounselFormCreateRequest request = new CounselFormCreateRequest(
                "투자 목적", "수도권", "서류 준비", "아파트 경매", "개인"
        );

        given(counselService.matchCounselor(any(CounselFormCreateRequest.class), any(Member.class)))
                .willThrow(new CustomApiException(ErrorCode.MEMBER_IS_SUBSCRIBED));

        mockMvc.perform(post("/counsels/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("상담 폼 수정 요청이 들어오면 수정된 상담 폼 정보를 응답")
    void updateCounselForm_success() throws Exception {
        CounselFormUpdateRequest request = new CounselFormUpdateRequest(
                "실거주 목적", "서울", "명도까지 전반적으로", "아파트 경매", "법인"
        );

        CounselUpdateResponse response = CounselUpdateResponse.builder()
                .counselFormId(1L)
                .counselorId(1L)
                .counselorName("테스트 상담사")
                .score(4.5)
                .reviewCount(12)
                .build();

        given(counselService.updateCounselForm(eq(1L), any(CounselFormUpdateRequest.class), any(Member.class)))
                .willReturn(response);

        mockMvc.perform(patch("/counsels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.counselFormId").value(1L))
                .andExpect(jsonPath("$.result.counselorName").value("테스트 상담사"));

        then(counselService).should().updateCounselForm(eq(1L), any(CounselFormUpdateRequest.class), any(Member.class));
    }

    @Test
    @DisplayName("존재하지 않는 상담 폼 수정 요청이 들어오면 404 응답을 반환")
    void updateCounselForm_fail_notFound() throws Exception {
        CounselFormUpdateRequest request = new CounselFormUpdateRequest(
                "실거주 목적", "서울", "명도까지 전반적으로", "아파트 경매", "법인"
        );

        given(counselService.updateCounselForm(eq(999L), any(CounselFormUpdateRequest.class), any(Member.class)))
                .willThrow(new CustomApiException(ErrorCode.COUNSEL_NOT_FOUND));

        mockMvc.perform(patch("/counsels/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("상담 신청 요청이 들어오면 생성된 상담 정보를 응답")
    void applyCounsel_success() throws Exception {
        CounselFormCreateRequest formRequest = new CounselFormCreateRequest(
                "투자 목적", "수도권", "서류 준비", "아파트 경매", "개인"
        );

        CounselApplyRequest request = new CounselApplyRequest(
                formRequest, LocalDateTime.of(2025, 12, 30, 10, 0)
        );

        ApplyCounselResponse response = ApplyCounselResponse.builder()
                .counselId(1L)
                .counselFormId(1L)
                .counselDate(LocalDate.of(2025, 12, 30))
                .counselTime(LocalTime.of(10, 0))
                .cellPhone("010-1234-5678")
                .purpose("투자 목적")
                .area("수도권")
                .build();

        given(counselService.applyCounsel(any(Member.class), eq(1L), any(CounselApplyRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/counsels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.counselId").value(1L))
                .andExpect(jsonPath("$.result.counselFormId").value(1L));

        then(counselService).should().applyCounsel(any(Member.class), eq(1L), any(CounselApplyRequest.class));
    }

    @Test
    @DisplayName("상담 일정 변경 요청이 들어오면 변경된 상담 정보를 응답")
    void updateCounsel_success() throws Exception {
        ApplyCounselResponse response = ApplyCounselResponse.builder()
                .counselId(1L)
                .counselFormId(1L)
                .counselDate(LocalDate.of(2025, 12, 31))
                .counselTime(LocalTime.of(14, 0))
                .build();

        // 컨트롤러는 LocalDateTime 하나를 받아서 내부에서 LocalDate / LocalTime으로 분리하는 구조
        given(counselService.updateApplyCounsel(
                anyLong(), anyLong(), any(Member.class), anyLong(),
                any(LocalDate.class), any(LocalTime.class)
        )).willReturn(response);

        mockMvc.perform(patch("/counsels/1/update")
                        .param("counselorId", "1")
                        .param("counselFormId", "1")
                        .param("date", "2025-12-31T14:00:00"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.counselId").value(1L));
    }

    @Test
    @DisplayName("상담 일정 변경 요청에서 날짜 형식이 잘못되면 400 응답을 반환")
    void updateCounsel_fail_invalidDateFormat() throws Exception {
        mockMvc.perform(patch("/counsels/1/update")
                        .param("counselorId", "1")
                        .param("counselFormId", "1")
                        .param("date", "wrong-date"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("상담 가능 시간 조회 시 예약 가능한 시간 목록을 응답")
    void inquiryPossibleTime_success() throws Exception {
        List<LocalTime> availableTimes = List.of(
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                LocalTime.of(11, 0)
        );

        given(counselService.inquiryPossibleTime(eq(1L), any(LocalDate.class)))
                .willReturn(availableTimes);

        mockMvc.perform(get("/counsels/1/times")
                        .param("date", "2025-12-30"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(3));
    }

    @Test
    @DisplayName("상담 가능 시간 조회에서 날짜가 누락되면 400 응답을 반환")
    void inquiryPossibleTime_fail_missingDateParam() throws Exception {
        mockMvc.perform(get("/counsels/1/times"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("상담 정보가 없으면 NONE 상태를 응답")
    void getCounselInfo_whenNone() throws Exception {
        CounselCombinedResponse response = new CounselCombinedResponse(CounselStatus.NONE, null);

        given(counselService.getCounselInfo(any(Member.class))).willReturn(response);

        mockMvc.perform(get("/counsels/info"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.status").value("NONE"));
    }

    @Test
    @DisplayName("상담 정보가 있으면 상담 상태와 상세 정보를 함께 응답")
    void getCounselInfo_success() throws Exception {
        CounselInfoResponse infoResponse = CounselInfoResponse.builder()
                .counselId(1L)
                .counselorId(1L)
                .counselorName("테스트 상담사")
                .counselDate(LocalDate.of(2025, 12, 30))
                .counselTime(LocalTime.of(10, 0))
                .score(4.5)
                .reviewCount(2)
                .build();

        CounselCombinedResponse response = new CounselCombinedResponse(
                CounselStatus.COUNSEL_AFTER, infoResponse
        );

        given(counselService.getCounselInfo(any(Member.class))).willReturn(response);

        mockMvc.perform(get("/counsels/info"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.status").value("COUNSEL_AFTER"))
                .andExpect(jsonPath("$.result.info.counselId").value(1L))
                .andExpect(jsonPath("$.result.info.counselorName").value("테스트 상담사"))
                .andExpect(jsonPath("$.result.info.score").value(4.5))
                .andExpect(jsonPath("$.result.info.reviewCount").value(2));
    }
}