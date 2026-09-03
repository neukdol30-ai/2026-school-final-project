package com.foodlogistics.erp.user;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.user.dto.UserCreateRequest;
import com.foodlogistics.erp.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserMapper appUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void 사용자를_생성하면_정규화된_정보를_반환한다() {
        UserCreateRequest request = new UserCreateRequest();
        request.setLoginId("  NewUser  ");
        request.setInitialPassword("password1234!");
        request.setUserName(" 신규 사원 ");
        request.setEmail(" user@example.com ");
        request.setPhone("   ");
        request.setPositionName(" 사원 ");

        when(appUserMapper.countByLoginId("newuser"))
                .thenReturn(0);

        when(passwordEncoder.encode("password1234!"))
                .thenReturn("encoded-password");

        when(appUserMapper.insert(any(AppUser.class)))
                .thenAnswer(invocation -> {
                    AppUser appUser =
                            invocation.getArgument(0);

                    appUser.setAppUserId(2L);
                    return 1;
                });

        UserResponse response = userService.createUser(
                1L,
                1L,
                request
        );

        assertAll(
                () -> assertEquals(
                        2L,
                        response.getAppUserId()
                ),
                () -> assertEquals(
                        1L,
                        response.getCompanyId()
                ),
                () -> assertEquals(
                        "newuser",
                        response.getLoginId()
                ),
                () -> assertEquals(
                        "신규 사원",
                        response.getUserName()
                ),
                () -> assertEquals(
                        "user@example.com",
                        response.getEmail()
                ),
                () -> assertNull(response.getPhone()),
                () -> assertEquals(
                        "사원",
                        response.getPositionName()
                ),
                () -> assertEquals(
                        "Y",
                        response.getUseYn()
                )
        );

        verify(passwordEncoder)
                .encode("password1234!");
    }

    @Test
    void 중복된_아이디이면_사용자를_생성하지_않는다() {
        UserCreateRequest request = new UserCreateRequest();
        request.setLoginId("  ExistingUser  ");
        request.setInitialPassword("password1234!");
        request.setUserName("기존 사용자");

        when(appUserMapper.countByLoginId("existinguser"))
                .thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createUser(
                        1L,
                        1L,
                        request
                )
        );

        assertEquals(
                ErrorCode.DUPLICATE_LOGIN_ID,
                exception.getErrorCode()
        );

        verifyNoInteractions(passwordEncoder);

        verify(appUserMapper, never())
                .insert(any(AppUser.class));
    }
}