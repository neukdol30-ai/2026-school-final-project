package com.foodlogistics.erp.security.auth;


import com.foodlogistics.erp.common.exception.BusinessException;import com.foodlogistics.erp.common.exception.ErrorCode;import com.foodlogistics.erp.security.auth.dto.LoginRequest;
import com.foodlogistics.erp.security.auth.dto.LoginResponse;
import com.foodlogistics.erp.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;import static org.mockito.ArgumentMatchers.any;import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void 로그인에_성공하면_JWT와_사용자정보를_반환한다() {
        LoginRequest request = new LoginRequest();
        request.setLoginId("owner");
        request.setPassword("owner1234!");

        AuthUser authUser = new AuthUser();
        authUser.setAppUserId(1L);
        authUser.setCompanyId(1L);
        authUser.setLoginId("owner");
        authUser.setPassword("encoded-password");
        authUser.setUserName("임시 관리자");
        authUser.setUseYn("Y");

        ErpUserDetails userDetails =
                new ErpUserDetails(authUser);

        Authentication authentication =
                mock(Authentication.class);

        when(authenticationManager.authenticate(
                any(Authentication.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(jwtTokenProvider.createAccessToken(userDetails))
                .thenReturn("text-access-token");

        when(jwtTokenProvider.getAccessTokenValiditySeconds())
                .thenReturn(1800L);

        LoginResponse response =
                authService.login(request);

        assertAll(
                () -> assertEquals(
                        "text-access-token",
                        response.getAccessToken()
                ),
                () -> assertEquals(
                        "Bearer",
                        response.getTokenType()
                ),
                () -> assertEquals(
                        1800L,
                        response.getExpiresIn()
                ),
                () -> assertEquals(
                        1L,
                        response.getAppUserId()
                ),
                () -> assertEquals(
                        1L,
                        response.getCompanyId()
                ),
                () -> assertEquals(
                        "owner",
                        response.getLoginId()
                ),
                () -> assertEquals(
                        "임시 관리자",
                        response.getUserName()
                )
        );
    }

    @Test
    void 인증에_실패하면_AUTHENTICATION_FAILED가_발생한다() {
        LoginRequest request = new LoginRequest();
        request.setLoginId("owner");
        request.setPassword("wrong-password");

        when(authenticationManager.authenticate(
                any(Authentication.class)
        )).thenThrow(
                new BadCredentialsException(
                        "아이디 또는 비밀번호 불일치"
                )
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                ErrorCode.AUTHENTICATION_FAILED,
                exception.getErrorCode()
        );

        verifyNoInteractions(jwtTokenProvider);
    }


}
