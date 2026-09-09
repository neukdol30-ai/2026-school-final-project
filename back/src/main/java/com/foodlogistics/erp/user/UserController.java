package com.foodlogistics.erp.user;

import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.user.dto.UserCreateRequest;
import com.foodlogistics.erp.user.dto.UserResponse;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<
            ApiResponse<List<UserResponse>>
            > getUsers(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Number companyId =
                jwt.getClaim("companyId");

        List<UserResponse> response =
                userService.getUsers(
                        companyId.longValue()
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<ApiResponse<UserResponse>>
    createUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            UserCreateRequest request
    ) {
        Number appUserId =
                jwt.getClaim("appUserId");

        Number companyId =
                jwt.getClaim("companyId");

        UserResponse response =
                userService.createUser(
                        companyId.longValue(),
                        appUserId.longValue(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }
}