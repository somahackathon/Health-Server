package team.soma.teto.health.global.error;

import java.time.Clock;
import java.util.List;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import team.soma.teto.health.global.response.ApiResponse;
import team.soma.teto.health.global.response.ErrorResponse;
import team.soma.teto.health.global.response.FieldErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.errorCode();
        return failure(errorCode, ErrorResponse.of(errorCode.code(), errorCode.message()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<FieldErrorResponse> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new FieldErrorResponse(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        return failure(CommonErrorCode.INVALID_INPUT, ErrorResponse.of(
                CommonErrorCode.INVALID_INPUT.code(),
                CommonErrorCode.INVALID_INPUT.message(),
                details
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable() {
        return failure(CommonErrorCode.INVALID_JSON);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String field = exception.getName();
        FieldErrorResponse detail = new FieldErrorResponse(field, CommonErrorCode.TYPE_MISMATCH.message());
        return failure(CommonErrorCode.TYPE_MISMATCH, ErrorResponse.of(
                CommonErrorCode.TYPE_MISMATCH.code(),
                CommonErrorCode.TYPE_MISMATCH.message(),
                List.of(detail)
        ));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestHeader(MissingRequestHeaderException exception) {
        return invalidParameter(exception.getHeaderName(), "required header is missing");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(MissingServletRequestParameterException exception) {
        return invalidParameter(exception.getParameterName(), "required parameter is missing");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestPart(MissingServletRequestPartException exception) {
        return invalidParameter(exception.getRequestPartName(), "required multipart part is missing");
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ApiResponse<Void>> handleMethodValidation() {
        return failure(CommonErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException() {
        return failure(CommonErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException() {
        return failure(CommonErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported() {
        return failure(CommonErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported() {
        return failure(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound() {
        return failure(CommonErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException() {
        return failure(CommonErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> failure(ErrorCode errorCode) {
        return failure(errorCode, ErrorResponse.of(errorCode.code(), errorCode.message()));
    }

    private ResponseEntity<ApiResponse<Void>> failure(ErrorCode errorCode, ErrorResponse errorResponse) {
        return new ResponseEntity<>(ApiResponse.failure(errorResponse, clock), new HttpHeaders(), HttpStatusCode.valueOf(errorCode.status().value()));
    }

    private ResponseEntity<ApiResponse<Void>> invalidParameter(String field, String reason) {
        FieldErrorResponse detail = new FieldErrorResponse(field, reason);
        return failure(CommonErrorCode.INVALID_INPUT, ErrorResponse.of(
                CommonErrorCode.INVALID_INPUT.code(),
                CommonErrorCode.INVALID_INPUT.message(),
                List.of(detail)
        ));
    }
}
