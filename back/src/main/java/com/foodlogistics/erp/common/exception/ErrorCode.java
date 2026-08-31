package com.foodlogistics.erp.common.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "입력값을 확인해 주십시오."
    ),

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            "잘못된 요청입니다."
    ),

    AUTHENTICATION_FAILED(
            HttpStatus.UNAUTHORIZED,
            "AUTHENTICATION_FAILED",
            "아이디 또는 비밀번호가 올바르지 않습니다."
    ),

    AUTHENTICATION_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTHENTICATION_REQUIRED",
            "인증이 필요합니다."
    ),

    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "ACCESS_DENIED",
            "접근 권한이 없습니다."
    ),

    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND",
            "요청한 데이터를 찾을 수 없습니다."
    ),

    METHOD_NOT_ALLOWED(
            HttpStatus.METHOD_NOT_ALLOWED,
            "METHOD_NOT_ALLOWED",
            "지원하지 않는 HTTP 메서드입니다."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버에 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

}
