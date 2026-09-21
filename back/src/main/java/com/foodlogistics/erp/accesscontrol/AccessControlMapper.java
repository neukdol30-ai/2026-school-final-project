package com.foodlogistics.erp.accesscontrol;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccessControlMapper {

    List<AccessRole> findRoles(
            @Param("companyId") Long companyId,
            @Param("keyword") String keyword,
            @Param("useYn") String useYn
    );

    AccessRole findRoleById(
            @Param("companyId") Long companyId,
            @Param("roleId") Long roleId
    );

    int countDuplicateRole(
            @Param("companyId") Long companyId,
            @Param("roleCode") String roleCode,
            @Param("roleName") String roleName
    );

    int insertRole(AccessRole role);

    List<AccessPermission> findPermissionsForRole(
            @Param("companyId") Long companyId,
            @Param("roleId") Long roleId
    );

    List<AccessPermission> findAllPermissions();

    List<Long> findExistingPermissionIds(
            @Param("ids") List<Long> ids
    );

    int deactivateRolePermissions(
            @Param("roleId") Long roleId,
            @Param("updatedBy") Long updatedBy
    );

    int upsertRolePermission(
            @Param("roleId") Long roleId,
            @Param("permissionId") Long permissionId,
            @Param("updatedBy") Long updatedBy
    );

    List<AccessRole> findRolesForUser(
            @Param("companyId") Long companyId,
            @Param("userId") Long userId
    );

    List<Long> findAssignableRoleIds(
            @Param("companyId") Long companyId,
            @Param("ids") List<Long> ids
    );

    int countActiveOwnerRoleForUser(
            @Param("companyId") Long companyId,
            @Param("userId") Long userId
    );

    int deactivateUserCustomRoles(
            @Param("companyId") Long companyId,
            @Param("userId") Long userId,
            @Param("updatedBy") Long updatedBy
    );

    int upsertUserRole(
            @Param("userId") Long userId,
            @Param("roleId") Long roleId,
            @Param("updatedBy") Long updatedBy
    );
}
