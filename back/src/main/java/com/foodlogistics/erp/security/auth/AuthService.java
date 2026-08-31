package com.foodlogistics.erp.security.auth;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.security.auth.dto.LoginRequest;
import com.foodlogistics.erp.security.auth.dto.LoginResponse;
import com.foodlogistics.erp.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    public LoginResponse login(LoginRequest request) {
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken
                            .unauthenticated(
                                    request.getLoginId(),
                                    request.getPassword()
                            )
            );
        } catch (AuthenticationException exception) {
            throw new BusinessException(
                    ErrorCode.AUTHENTICATION_FAILED
            );
        }

        ErpUserDetails userDetails = (ErpUserDetails) authentication.getPrincipal();

        String accessToken =
                jwtTokenProvider.createAccessToken(userDetails);

        return new LoginResponse(
                accessToken,
                jwtTokenProvider
                        .getAccessTokenValiditySeconds(),
                userDetails.getAppUserId(),
                userDetails.getCompanyId(),
                userDetails.getUsername(),
                userDetails.getDisplayName()
        );
    }
}
