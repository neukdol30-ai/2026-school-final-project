package com.foodlogistics.erp.accesscontrol;

import com.foodlogistics.erp.accesscontrol.dto.IdAssignmentRequest;
import com.foodlogistics.erp.accesscontrol.dto.PermissionResponse;
import com.foodlogistics.erp.accesscontrol.dto.RoleCreateRequest;
import com.foodlogistics.erp.accesscontrol.dto.RoleResponse;
import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.user.AppUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessControlService {

    private final AccessControlMapper accessControlMapper;
    private final AppUserMapper appUserMapper;

    public List<RoleResponse> getRoles(
            Long companyId,
            String keyword,
            String useYn
    ) {
        return accessControlMapper.findRoles(
                        companyId,
                        trimToNull(keyword),
                        normalizeUseYn(useYn)
                )
                .stream()
                .map(this::toRoleResponse)
                .toList();
    }

    @Transactional
    public RoleResponse createRole(
            Long companyId,
            Long appUserId,
            RoleCreateRequest request
    ) {
        String roleCode = request
                .getRoleCode()
                .trim()
                .toUpperCase(Locale.ROOT);
        String roleName = request.getRoleName().trim();

        if ("OWNER".equals(roleCode)) {
            throw new BusinessException(
                    ErrorCode.OWNER_ACCESS_IMMUTABLE
            );
        }

        if (accessControlMapper.countDuplicateRole(
                companyId,
                roleCode,
                roleName
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_ROLE
            );
        }

        AccessRole role = new AccessRole();
        role.setCompanyId(companyId);
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setRoleType("CUSTOM");
        role.setDescription(
                trimToNull(request.getDescription())
        );
        role.setUseYn("Y");
        role.setCreatedBy(appUserId);
        role.setUpdatedBy(appUserId);

        List<Long> requestedPermissionIds =
                distinctIds(request.getPermissionIds());

        validatePermissionIds(requestedPermissionIds);
        List<Long> permissionIds =
                includeRequiredReadPermissions(
                        requestedPermissionIds
        );

        try {
            accessControlMapper.insertRole(role);

            permissionIds.forEach(permissionId ->
                    accessControlMapper
                            .upsertRolePermission(
                                    role.getAppRoleId(),
                                    permissionId,
                                    appUserId
                            )
            );
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_ROLE
            );
        }

        return toRoleResponse(
                findRole(companyId, role.getAppRoleId())
        );
    }

    public List<PermissionResponse> getPermissions() {
        return accessControlMapper
                .findAllPermissions()
                .stream()
                .map(this::toPermissionResponse)
                .toList();
    }

    public List<PermissionResponse> getRolePermissions(
            Long companyId,
            Long roleId
    ) {
        findRole(companyId, roleId);

        return accessControlMapper
                .findPermissionsForRole(
                        companyId,
                        roleId
                )
                .stream()
                .map(this::toPermissionResponse)
                .toList();
    }

    @Transactional
    public void updateRolePermissions(
            Long companyId,
            Long appUserId,
            Long roleId,
            IdAssignmentRequest request
    ) {
        AccessRole role = findRole(companyId, roleId);

        if ("OWNER".equals(role.getRoleType())) {
            throw new BusinessException(
                    ErrorCode.OWNER_ACCESS_IMMUTABLE
            );
        }

        if (!"CUSTOM".equals(role.getRoleType())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        List<Long> requestedPermissionIds =
                distinctIds(request.getIds());

        validatePermissionIds(requestedPermissionIds);
        List<Long> permissionIds =
                includeRequiredReadPermissions(
                        requestedPermissionIds
        );

        accessControlMapper.deactivateRolePermissions(
                roleId,
                appUserId
        );

        permissionIds.forEach(permissionId ->
                accessControlMapper.upsertRolePermission(
                        roleId,
                        permissionId,
                        appUserId
                )
        );
    }

    public List<RoleResponse> getUserRoles(
            Long companyId,
            Long userId
    ) {
        validateUser(companyId, userId);

        return accessControlMapper
                .findRolesForUser(companyId, userId)
                .stream()
                .map(this::toRoleResponse)
                .toList();
    }

    @Transactional
    public void updateUserRoles(
            Long companyId,
            Long appUserId,
            Long userId,
            IdAssignmentRequest request
    ) {
        validateUser(companyId, userId);

        if (accessControlMapper
                .countActiveOwnerRoleForUser(
                        companyId,
                        userId
                ) > 0) {
            throw new BusinessException(
                    ErrorCode.OWNER_ACCESS_IMMUTABLE
            );
        }

        List<Long> roleIds =
                distinctIds(request.getIds());

        validateAssignableRoleIds(
                companyId,
                roleIds
        );

        accessControlMapper.deactivateUserCustomRoles(
                companyId,
                userId,
                appUserId
        );

        roleIds.forEach(roleId ->
                accessControlMapper.upsertUserRole(
                        userId,
                        roleId,
                        appUserId
                )
        );
    }

    private AccessRole findRole(
            Long companyId,
            Long roleId
    ) {
        AccessRole role =
                accessControlMapper.findRoleById(
                        companyId,
                        roleId
                );

        if (role == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }

        return role;
    }

    private void validateUser(
            Long companyId,
            Long userId
    ) {
        if (appUserMapper.findById(
                companyId,
                userId
        ) == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }
    }

    private void validatePermissionIds(
            List<Long> permissionIds
    ) {
        if (permissionIds.isEmpty()) {
            return;
        }

        List<AccessPermission> permissions =
                accessControlMapper.findAllPermissions();

        Set<Long> existingIds = permissions.stream()
                .map(AccessPermission::getAppPermissionId)
                .collect(java.util.stream.Collectors.toSet());

        if (!existingIds.containsAll(permissionIds)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Set<String> allCodes = permissions.stream()
                .map(AccessPermission::getPermissionCode)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> selectedCodes = permissions.stream()
                .filter(permission -> permissionIds.contains(
                        permission.getAppPermissionId()
                ))
                .map(AccessPermission::getPermissionCode)
                .collect(java.util.stream.Collectors.toSet());

        boolean missingReadPermission = selectedCodes.stream()
                .map(code -> requiredReadCode(code, allCodes))
                .anyMatch(requiredReadCode ->
                        requiredReadCode != null
                                && !selectedCodes.contains(
                                requiredReadCode
                        )
                );

        if (missingReadPermission) {
            throw new BusinessException(
                    ErrorCode.READ_PERMISSION_REQUIRED
            );
        }
    }

    private String requiredReadCode(
            String permissionCode,
            Set<String> allCodes
    ) {
        if (permissionCode.endsWith("_READ")) {
            return null;
        }

        int actionSeparator = permissionCode.lastIndexOf('_');

        if (actionSeparator < 0) {
            return null;
        }

        String readCode = permissionCode.substring(
                0,
                actionSeparator
        ) + "_READ";

        return allCodes.contains(readCode)
                ? readCode
                : null;
    }

    private List<Long> includeRequiredReadPermissions(
            List<Long> permissionIds
    ) {
        if (permissionIds.isEmpty()) {
            return permissionIds;
        }

        List<AccessPermission> allPermissions =
                accessControlMapper.findAllPermissions();
        Map<Long, AccessPermission> permissionsById =
                allPermissions.stream().collect(
                        Collectors.toMap(
                                AccessPermission::getAppPermissionId,
                                Function.identity()
                        )
                );
        Map<String, Long> idsByCode =
                allPermissions.stream().collect(
                        Collectors.toMap(
                                permission -> permission
                                        .getPermissionCode()
                                        .toUpperCase(Locale.ROOT),
                                AccessPermission::getAppPermissionId
                        )
                );
        LinkedHashSet<Long> completedIds =
                new LinkedHashSet<>(permissionIds);

        permissionIds.forEach(permissionId -> {
            AccessPermission permission =
                    permissionsById.get(permissionId);
            String permissionCode = permission
                    .getPermissionCode()
                    .toUpperCase(Locale.ROOT);

            if (permissionCode.endsWith("_READ")) {
                return;
            }

            int actionSeparator =
                    permissionCode.lastIndexOf('_');

            if (actionSeparator < 0) {
                return;
            }

            Long readPermissionId = idsByCode.get(
                    permissionCode.substring(
                            0,
                            actionSeparator
                    ) + "_READ"
            );

            if (readPermissionId != null) {
                completedIds.add(readPermissionId);
            }
        });

        return List.copyOf(completedIds);
    }

    private void validateAssignableRoleIds(
            Long companyId,
            List<Long> roleIds
    ) {
        if (roleIds.isEmpty()) {
            return;
        }

        if (accessControlMapper
                .findAssignableRoleIds(companyId, roleIds)
                .size() != roleIds.size()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private List<Long> distinctIds(List<Long> ids) {
        return List.copyOf(
                new LinkedHashSet<>(ids)
        );
    }

    private String normalizeUseYn(String useYn) {
        String normalized = trimToNull(useYn);

        if (normalized == null
                || "ALL".equalsIgnoreCase(normalized)) {
            return null;
        }

        normalized =
                normalized.toUpperCase(Locale.ROOT);

        if (!"Y".equals(normalized)
                && !"N".equals(normalized)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private RoleResponse toRoleResponse(
            AccessRole role
    ) {
        return new RoleResponse(
                role.getAppRoleId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getRoleType(),
                role.getDescription(),
                role.getUseYn(),
                role.getPermissionCount(),
                role.getAssignedYn()
        );
    }

    private PermissionResponse toPermissionResponse(
            AccessPermission permission
    ) {
        return new PermissionResponse(
                permission.getAppPermissionId(),
                permission.getPermissionCode(),
                permission.getPermissionName(),
                permission.getDescription(),
                permission.getAssignedYn()
        );
    }
}
