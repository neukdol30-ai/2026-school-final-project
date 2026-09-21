package com.foodlogistics.erp.inbound.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
// 신규 LOT INSERT용 내부 객체
public class LotInsertParam {

    private Long lotId;

    private Long companyId;

    private Long productId;

    private String lotNo;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;
}