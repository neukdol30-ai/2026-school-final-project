package com.foodlogistics.erp.accesscontrol.dto;

public record RoleResponse(
        Long appRoleId,
        String roleCode,
        String roleName,
        String roleType,
        String description,
        String useYn,
        Integer permissionCount,
        String assignedYn
) {
}
