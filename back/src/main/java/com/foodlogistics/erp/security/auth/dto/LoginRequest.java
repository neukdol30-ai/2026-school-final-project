package com.foodlogistics.erp.security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "아이디를 입력해 주십시오.")
    @Size(
            max = 50,
            message = "아이디는 50자 이하여야 합니다"
    )
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해 주십시오.")
    @Size(
            max = 100,
            message = "비밀벅호는 100자 이하여야 합니다."
    )
    private String password;
}
