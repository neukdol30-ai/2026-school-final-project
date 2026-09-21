package com.foodlogistics.erp.outbound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 확정된 출고서를 취소할 때 남기는 사유다.
public record OutboundCancelRequestDto(
        @NotBlank(message = "출고 취소 사유를 입력해 주세요.")
        @Size(max = 500, message = "출고 취소 사유는 500자 이하여야 합니다.")
        String cancelReason
) {
}
