package com.foodlogistics.erp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "아이디를 입력해 주십시오.")
    @Size(
            max = 50,
            message = "아이디는 50자 이하여야 합니다."
    )
    private String loginId;

    @NotBlank(message = "초기 비밀번호를 입력해 주십시오.")
    @Size(
            min = 8,
            max = 100,
            message = "비밀번호는 8자 이상 100자 이하여야 합니다."
    )
    private String initialPassword;

    @NotBlank(message = "사용자 이름을 입력해 주십시오.")
    @Size(
            max = 50,
            message = "사용자 이름은 50자 이하여야 합니다."
    )
    private String userName;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(
            max = 254,
            message = "이메일은 254자 이하여야 합니다."
    )
    private String email;

    @Size(
            max = 20,
            message = "전화번호는 20자 이하여야 합니다."
    )
    private String phone;

    @Size(
            max = 50,
            message = "직급명은 50자 이하여야 합니다."
    )
    private String positionName;
}