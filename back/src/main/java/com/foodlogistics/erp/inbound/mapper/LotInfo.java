package com.foodlogistics.erp.inbound.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
// 기존 LOT 조회 결과
public class LotInfo {

    private Long lotId;

    private Long companyId;

    private Long productId;

    private String lotNo;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;
}