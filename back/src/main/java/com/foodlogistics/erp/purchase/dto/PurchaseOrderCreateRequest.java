package com.foodlogistics.erp.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
// React가 보내는 "발주서 전체 등록 요청"을 받는 DTO
public class PurchaseOrderCreateRequest {

    @NotNull(message = "공급업체를 선택해 주십시오.")
    @Positive(message = "공급업체 ID는 0보다 커야 합니다.")
    private Long supplierId;

    @NotNull(message = "창고를 선택해 주십시오.")
    @Positive(message = "창고 ID는 0보다 커야 합니다.")
    private Long warehouseId;

    @NotNull(message = "발주일을 입력해 주십시오.")
    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    @Size(max = 500, message = "공급업체 요청사항은 500자 이하여야 합니다.")
    private String requestNote;

    @Size(max = 500, message = "내부 메모는 500자 이하여야 합니다.")
    private String internalMemo;

    @NotEmpty(message = "발주 품목을 1개 이상 추가해 주십시오.")
    @Valid
    private List<PurchaseOrderItemCreateRequest> items;
}
