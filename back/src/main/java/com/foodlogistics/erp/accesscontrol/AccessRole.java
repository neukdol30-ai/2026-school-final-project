package com.foodlogistics.erp.accesscontrol;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccessRole {

    private Long appRoleId;
    private Long companyId;
    private String roleCode;
    private String roleName;
    private String roleType;
    private String description;
    private String useYn;
    private Integer permissionCount;
    private String assignedYn;
    private Long createdBy;
    private Long updatedBy;
}
