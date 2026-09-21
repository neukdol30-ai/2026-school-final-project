package com.foodlogistics.erp.inbound.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
// LOT 관리상품의 LOT 한 건을 받는 요청 DTO
public class InboundItemLotRequest {

    @NotBlank(message = "LOT 번호를 입력해 주십시오.")
    @Size(
            max = 50,
            message = "LOT 번호는 50자 이하여야 합니다."
    )
    private String lotNo;

    @NotNull(message = "LOT 기준수량을 입력해 주십시오.")
    @DecimalMin(
            value = "0.001",
            message = "LOT 기준수량은 0.001 이상이어야 합니다."
    )
    @Digits(
            integer = 16,
            fraction = 3,
            message = "LOT 기준수량은 정수 16자리, 소수 3자리 이하여야 합니다."
    )
    private BigDecimal baseLotQty;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;
}