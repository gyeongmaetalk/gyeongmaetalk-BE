package auctionTalk.auction.global.validation;

import auctionTalk.auction.domain.counsel.dto.request.CounselFormCreateRequest;
import auctionTalk.auction.domain.review.dto.request.ReviewCreateRequest;
import auctionTalk.auction.domain.review.dto.request.ReviewUpdateRequest;
import auctionTalk.auction.global.exception.CustomRestControllerAdvice;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 요청 DTO에 붙은 @NotBlank, @Min 같은 제약이 실제로 동작하는지 확인한다.
 * Bean Validation 구현체가 클래스패스에 없으면 @Valid가 아무것도 검사하지 않는다.
 */
class RequestValidationTest {

    private static Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static ReviewCreateRequest review(int score, String content) {
        // 프론트는 counselId를 보내지 않는다(서비스는 로그인 회원의 상담으로 리뷰를 만든다).
        return new ReviewCreateRequest(null, score, content, List.of());
    }

    @Test
    @DisplayName("별점이 1~5 밖이면 리뷰 생성 요청이 거부된다")
    void reviewScoreOutOfRange() {
        Validator validator = validator();

        assertThat(validator.validate(review(0, "좋았습니다"))).isNotEmpty();
        assertThat(validator.validate(review(6, "좋았습니다"))).isNotEmpty();
        assertThat(validator.validate(new ReviewUpdateRequest(6, "좋았습니다", List.of(), List.of()))).isNotEmpty();
    }

    @Test
    @DisplayName("정상 리뷰는 counselId 없이도 통과한다")
    void validReviewPasses() {
        Set<ConstraintViolation<ReviewCreateRequest>> violations = validator().validate(review(5, "좋았습니다"));

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("리뷰 내용이 250자를 넘으면 거부된다")
    void reviewContentTooLong() {
        assertThat(validator().validate(review(5, "가".repeat(251)))).isNotEmpty();
    }

    @RestController
    static class EchoController {
        @PostMapping("/test/counsel-form")
        String create(@Valid @RequestBody CounselFormCreateRequest request) {
            return "ok";
        }
    }

    @Test
    @DisplayName("@Valid 요청 본문의 필수값이 비어 있으면 400과 필드 메시지를 반환한다")
    void blankFieldReturns400() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new EchoController())
                .setControllerAdvice(new CustomRestControllerAdvice())
                .build();

        mockMvc.perform(post("/test/counsel-form")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"purpose":"","area":"수도권","serviceType":"서류 준비",
                                 "interest":"상가","participantType":"개인"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("상담 목적은 필수 입력값입니다."));
    }
}
