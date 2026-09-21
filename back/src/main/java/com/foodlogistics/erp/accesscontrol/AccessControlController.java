package com.foodlogistics.erp.accesscontrol;

import com.foodlogistics.erp.accesscontrol.dto.IdAssignmentRequest;
import com.foodlogistics.erp.accesscontrol.dto.PermissionResponse;
import com.foodlogistics.erp.accesscontrol.dto.RoleCreateRequest;
import com.foodlogistics.erp.accesscontrol.dto.RoleResponse;
import com.foodlogistics.erp.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access-control")
@RequiredArgsConstructor
public class AccessControlController {

    private final AccessControlService accessControlService;

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ACCESS_CONTROL_READ')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>>
    getRoles(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String useYn
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        accessControlService.getRoles(
                                getCompanyId(jwt),
                                keyword,
                                useYn
                        )
                )
        );
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ACCESS_CONTROL_READ') and hasAuthority('ACCESS_CONTROL_MANAGE')")
    public ResponseEntity<ApiResponse<RoleResponse>>
    createRole(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RoleCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        accessControlService.createRole(
                                getCompanyId(jwt),
                                getAppUserId(jwt),
                                request
                        )
                ));
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ACCESS_CONTROL_READ')")
    public ResponseEntity<
            ApiResponse<List<PermissionResponse>>
            > getPermissions() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        accessControlService
                                .getPermissions()
                )
        );
    }

    @GetMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ACCESS_CONTROL_READ')")
    public ResponseEntity<
            ApiResponse<List<PermissionResponse>>
            > getRolePermissions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        accessControlService
                                .getRolePermissions(
                                        getCompanyId(jwt),
                                        roleId
                                )
                )
        );
    }

    @PutMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ACCESS_CONTROL_READ') and hasAuthority('ACCESS_CONTROL_MANAGE')")
    public ResponseEntity<ApiResponse<Void>>
    updateRolePermissions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roleId,
            @Valid @RequestBody IdAssignmentRequest request
    ) {
        accessControlService.updateRolePermissions(
                getCompanyId(jwt),
                getAppUserId(jwt),
                roleId,
                request
        );

        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority('ACCESS_CONTROL_READ')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>>
    getUserRoles(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        accessControlService.getUserRoles(
                                getCompanyId(jwt),
                                userId
                        )
                )
        );
    }

    @PutMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority('ACCESS_CONTROL_READ') and hasAuthority('ACCESS_CONTROL_MANAGE')")
    public ResponseEntity<ApiResponse<Void>>
    updateUserRoles(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId,
            @Valid @RequestBody IdAssignmentRequest request
    ) {
        accessControlService.updateUserRoles(
                getCompanyId(jwt),
                getAppUserId(jwt),
                userId,
                request
        );

        return ResponseEntity.ok(ApiResponse.ok());
    }

    private Long getCompanyId(Jwt jwt) {
        Number companyId = jwt.getClaim("companyId");
        return companyId.longValue();
    }

    private Long getAppUserId(Jwt jwt) {
        Number appUserId = jwt.getClaim("appUserId");
        return appUserId.longValue();
    }
}
