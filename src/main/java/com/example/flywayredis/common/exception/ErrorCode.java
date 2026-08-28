package com.example.flywayredis.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    PRODUCT_NOT_FOUND(404,"PRODUCT_NOT_FOUND","상품을 찾을 수 없습니다."),
    USER_NOT_FOUND(404,"USER_NOT_FOUND","사용자를 찾을 수 없습니다."),
    NOT_SELLER_OF_PRODUCT(403,"NOT_SELLER_OF_PRODUCT","해당 상품의 판매자가 아닙니다.");

    private final int status;
    private final String code;
    private final String message;


}
