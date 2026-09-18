package com.foodlogistics.erp.purchase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
// 발주 반려 요청에서 반려사유를 받는 DTO
public class PurchaseOrderRejectRequest {

    @NotBlank(message = "반려사유를 입력해 주십시오.")
    @Size(max = 500, message = "반려사유는 500자 이하여야 합니다.")
    private String rejectionReason;
}
