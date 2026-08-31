package com.foodlogistics.erp.common.exception;

import com.foodlogistics.erp.common.response.ApiError;
import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.common.response.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleBusinessException(BusinessException exception
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        log.warn(
                "Business exception : code={}, message={}",
                errorCode.getCode(),
                exception.getMessage()
        );

        ApiError apiError = ApiError.of(
                errorCode.getCode(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleValidationException(MethodArgumentNotValidException exception
    ) {
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

        List<ValidationError> fields = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        log.warn(
                "Validation exception: fieldCount={}",
                fields.size()
        );

        ApiError apiError = ApiError.of(
                errorCode.getCode(),
                errorCode.getMessage(),
                fields
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleUnreadableMessage(
            HttpMessageNotReadableException exception
    ){
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

        log.warn(
                "Invalid request body: {}",
                exception.getMessage()
        );

        ApiError apiError = ApiError.of(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException exception
    ){
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;

        log.warn(
                "Method not allowed exception: {}",
                exception.getMessage()
        );

        ApiError apiError = ApiError.of(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>>
    handleUnexpectedException(Exception exception
    ){
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        log.error(
                "Unexpected server exception",
                exception
        );

        ApiError apiError = ApiError.of(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(apiError));
    }
}
