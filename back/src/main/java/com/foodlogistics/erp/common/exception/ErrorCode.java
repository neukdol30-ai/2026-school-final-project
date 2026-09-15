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

    DUPLICATE_LOGIN_ID(
            HttpStatus.CONFLICT,
            "DUPLICATE_LOGIN_ID",
            "이미 사용 중인 아이디입니다."
    ),

    DUPLICATE_WAREHOUSE_CODE(
            HttpStatus.CONFLICT,
            "DUPLICATE_WAREHOUSE_CODE",
            "이미 사용 중인 창고 코드입니다."
    ),

    DUPLICATE_UNIT_CODE(
            HttpStatus.CONFLICT,
            "DUPLICATE_UNIT_CODE",
            "이미 사용 중인 단위 코드입니다."
    ),

    DUPLICATE_PRODUCT_CODE(
            HttpStatus.CONFLICT,
            "DUPLICATE_PRODUCT_CODE",
            "이미 사용 중인 상품 코드입니다."
    ),

    DUPLICATE_PARTNER_CODE(
            HttpStatus.CONFLICT,
            "DUPLICATE_PARTNER_CODE",
            "이미 사용 중인 거래처 코드입니다."
    ),

    DUPLICATE_BUSINESS_NUMBER(
            HttpStatus.CONFLICT,
            "DUPLICATE_BUSINESS_NUMBER",
            "이미 등록된 사업자등록번호입니다."
    ),

    DUPLICATE_PRODUCT_UNIT(
            HttpStatus.CONFLICT,
            "DUPLICATE_PRODUCT_UNIT",
            "이미 등록된 상품 단위입니다."
    ),

    BASE_UNIT_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "BASE_UNIT_ALREADY_EXISTS",
            "해당 상품에는 이미 기준 단위가 존재합니다."
    ),

    BASE_UNIT_CONVERSION_INVALID(
            HttpStatus.BAD_REQUEST,
            "BASE_UNIT_CONVERSION_INVALID",
            "기준 단위의 환산 수량은 1이어야 합니다."
    ),

    UNIT_NOT_AVAILABLE(
            HttpStatus.BAD_REQUEST,
            "UNIT_NOT_AVAILABLE",
            "존재하지 않거나 사용할 수 없는 단위입니다."
    ),

    PARTNER_ROLE_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "PARTNER_ROLE_REQUIRED",
            "공급업체 또는 판매처 중 하나 이상을 선택해야 합니다."
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
