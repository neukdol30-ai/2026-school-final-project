package com.foodlogistics.erp.security.auth;

import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.security.auth.dto.CurrentUserResponse;
import com.foodlogistics.erp.security.auth.dto.LoginRequest;
import com.foodlogistics.erp.security.auth.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
            ) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>>
    getCurrentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Number appUserId = jwt.getClaim("appUserId");
        Number companyId = jwt.getClaim("companyId");

        List<String> authorities =
                jwt.getClaimAsStringList(
                        "authorities"
                );

        if (authorities == null) {
            authorities = List.of();
        }

        CurrentUserResponse response =
                new CurrentUserResponse(
                        appUserId.longValue(),
                        companyId.longValue(),
                        jwt.getSubject(),
                        jwt.getClaimAsString("userName"),
                        authorities
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }
}
