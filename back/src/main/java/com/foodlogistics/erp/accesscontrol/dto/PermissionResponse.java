package com.foodlogistics.erp.accesscontrol.dto;

public record PermissionResponse(
        Long appPermissionId,
        String permissionCode,
        String permissionName,
        String description,
        String assignedYn
) {
}
