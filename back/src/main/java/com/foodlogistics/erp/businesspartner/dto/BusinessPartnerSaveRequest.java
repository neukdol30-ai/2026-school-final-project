package com.foodlogistics.erp.businesspartner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BusinessPartnerSaveRequest {

    @NotBlank(message = "거래처 코드를 입력해 주십시오.")
    @Size(
            max = 30,
            message = "거래처 코드는 30자 이하여야 합니다."
    )
    private String partnerCode;

    @NotBlank(message = "거래처명을 입력해 주십시오.")
    @Size(
            max = 100,
            message = "거래처명은 100자 이하여야 합니다."
    )
    private String partnerName;

    @Pattern(
            regexp = "^$|^\\d{3}-?\\d{2}-?\\d{5}$",
            message = "사업자등록번호 형식을 확인해 주십시오."
    )
    private String businessNumber;

    @Size(
            max = 50,
            message = "대표자명은 50자 이하여야 합니다."
    )
    private String representativeName;

    @Size(
            max = 50,
            message = "담당자명은 50자 이하여야 합니다."
    )
    private String contactName;

    @Size(
            max = 20,
            message = "전화번호는 20자 이하여야 합니다."
    )
    private String phone;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(
            max = 254,
            message = "이메일은 254자 이하여야 합니다."
    )
    private String email;

    @Size(
            max = 10,
            message = "우편번호는 10자 이하여야 합니다."
    )
    private String postalCode;

    @Size(
            max = 200,
            message = "기본주소는 200자 이하여야 합니다."
    )
    private String address1;

    @Size(
            max = 200,
            message = "상세주소는 200자 이하여야 합니다."
    )
    private String address2;

    @NotBlank(message = "공급업체 여부를 입력해 주십시오.")
    @Pattern(
            regexp = "[YN]",
            message = "공급업체 여부는 Y 또는 N이어야 합니다."
    )
    private String supplierYn;

    @NotBlank(message = "판매처 여부를 입력해 주십시오.")
    @Pattern(
            regexp = "[YN]",
            message = "판매처 여부는 Y 또는 N이어야 합니다."
    )
    private String customerYn;
}