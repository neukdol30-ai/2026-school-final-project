package com.foodlogistics.erp.accesscontrol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RoleCreateRequest {

    @NotBlank(message = "권한 그룹 코드를 입력해 주십시오.")
    @Size(max = 50)
    private String roleCode;

    @NotBlank(message = "권한 그룹명을 입력해 주십시오.")
    @Size(max = 100)
    private String roleName;

    @Size(max = 200)
    private String description;

    @NotNull(message = "권한 목록이 필요합니다.")
    private List<@Positive Long> permissionIds =
            List.of();
}
