package auctionTalk.auction.global.common.exception;

import auctionTalk.auction.global.common.CommonResponse;
import auctionTalk.auction.global.common.exception.base.GeneralException;
import auctionTalk.auction.global.common.exception.base.GlobalErrorCode;
import auctionTalk.auction.global.common.exception.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        CommonResponse<Object> body = CommonResponse.onFailure(
                GlobalErrorCode.BAD_BODY.getCode(),
                "요청 본문을 읽을 수 없습니다. BODY 자체를 읽을 수 없는 상태입니다.. (형식 오류)",
                null
        );

        return super.handleExceptionInternal(
                ex,
                body,
                headers,
                HttpStatus.BAD_REQUEST,
                request
        );
    }


    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ConstraintViolationException 추출 도중 에러 발생"));

        return handleExceptionInternalConstraint(e, GlobalErrorCode.valueOf(errorMessage), request);
    }

    @NotNull
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatus status, WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors()
                .forEach(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
                    errors.merge(fieldName, errorMessage, (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
                });

        return handleExceptionInternalArgs(e, GlobalErrorCode.valueOf("BAD_REQUEST"),request,errors);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        e.printStackTrace();

        return handleExceptionInternalFalse(e, GlobalErrorCode.SERVER_ERROR.getHttpStatus(),request, e.getMessage());
    }

//    @ExceptionHandler(value = GeneralException.class)
//    public ResponseEntity onThrowException(GeneralException generalException,
//                                           @AuthenticationPrincipal User user, HttpServletRequest request) {
//        getExceptionStackTrace(generalException, user, request);
//        ErrorResponseDTO errorReasonHttpStatus = generalException.getErrorReasonHttpStatus();
//        System.out.println(generalException.getMessage());
//        System.out.println(generalException.getErrorCode());
//        return handleExceptionInternal(generalException,errorReasonHttpStatus, request);
//    }


    private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorResponseDTO reason,
                                                           HttpServletRequest request) {

        CommonResponse<Object> body = CommonResponse.onFailure(reason.getCode(),reason.getMessage(),null);
//        e.printStackTrace();

        WebRequest webRequest = new ServletWebRequest(request);
        return super.handleExceptionInternal(
                e,
                body,
                null,
                reason.getHttpStatus(),
                webRequest
        );
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e,
                                                                HttpStatus status, WebRequest request, String errorPoint) {
        CommonResponse<Object> body = CommonResponse.onFailure(GlobalErrorCode.SERVER_ERROR.getCode(), GlobalErrorCode.SERVER_ERROR.getMessage(),errorPoint);
        return super.handleExceptionInternal(
                e,
                body,
                HttpHeaders.EMPTY,
                status,
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, GlobalErrorCode errorCommonStatus,
                                                               WebRequest request, Map<String, String> errorArgs) {
        CommonResponse<Object> body = CommonResponse.onFailure(errorCommonStatus.getCode(),errorCommonStatus.getMessage(),errorArgs);
        return super.handleExceptionInternal(
                e,
                body,
                HttpHeaders.EMPTY,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalConstraint(Exception e, GlobalErrorCode errorCommonStatus,
                                                                     WebRequest request) {
        CommonResponse<Object> body = CommonResponse.onFailure(errorCommonStatus.getCode(), errorCommonStatus.getMessage(), null);
        return super.handleExceptionInternal(
                e,
                body,
                HttpHeaders.EMPTY,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }

//    private void getExceptionStackTrace(Exception e, @AuthenticationPrincipal User user,
//                                        HttpServletRequest request) {
//        StringWriter sw = new StringWriter();
//        PrintWriter pw = new PrintWriter(sw);
//
//        pw.append("\n==========================!!!ERROR TRACE!!!==========================\n");
//        pw.append("uri: ").append(request.getRequestURI()).append(" ").append(request.getMethod()).append("\n");
//        if (user != null) {
//            pw.append("uid: ").append(user.getUsername()).append("\n");
//        }
//        pw.append(e.getMessage());
//        System.out.println(e.getMessage());
//        pw.append("\n=====================================================================");
//        log.error(sw.toString());
//    }
}

