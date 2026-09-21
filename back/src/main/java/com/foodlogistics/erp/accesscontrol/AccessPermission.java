package com.foodlogistics.erp.accesscontrol;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccessPermission {

    private Long appPermissionId;
    private String permissionCode;
    private String permissionName;
    private String description;
    private String assignedYn;
}
