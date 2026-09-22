package com.foodlogistics.erp.inbound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
// React가 보내는 INBOUND DRAFT 취소 요청을 받는 DTO
public class InboundCancelRequest {

    @NotBlank(message = "취소사유를 입력해 주십시오.")
    @Size(max = 500, message = "취소사유는 500자 이하여야 합니다.")
    private String cancelReason;
}
