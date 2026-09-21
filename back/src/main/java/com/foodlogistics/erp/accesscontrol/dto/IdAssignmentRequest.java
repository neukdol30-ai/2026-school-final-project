package com.foodlogistics.erp.accesscontrol.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class IdAssignmentRequest {

    @NotNull(message = "배정 목록이 필요합니다.")
    private List<@Positive Long> ids;
}
