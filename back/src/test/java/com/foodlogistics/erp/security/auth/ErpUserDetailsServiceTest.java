package com.foodlogistics.erp.security.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErpUserDetailsServiceTest {

    @Mock
    private AuthUserMapper authUserMapper;

    @InjectMocks
    private ErpUserDetailsService userDetailsService;

    @Test
    void 사용자를_조회하면_권한도_함께_불러온다() {
        AuthUser authUser = new AuthUser();
        authUser.setAppUserId(1L);
        authUser.setCompanyId(10L);
        authUser.setLoginId("owner");
        authUser.setPassword("encoded-password");
        authUser.setUserName("관리자");
        authUser.setUseYn("Y");

        when(authUserMapper.findByLoginId("owner"))
                .thenReturn(Optional.of(authUser));

        when(authUserMapper.findPermissionCodes(
                1L,
                10L
        )).thenReturn(List.of(
                " user_read ",
                "USER_CREATE",
                "user_read"
        ));

        UserDetails result =
                userDetailsService.loadUserByUsername(
                        "  OWNER  "
                );

        assertAll(
                () -> assertEquals(
                        "owner",
                        result.getUsername()
                ),
                () -> assertTrue(result.isEnabled()),
                () -> assertEquals(
                        2,
                        result.getAuthorities().size()
                ),
                () -> assertTrue(
                        result.getAuthorities()
                                .stream()
                                .anyMatch(authority ->
                                        authority
                                                .getAuthority()
                                                .equals("USER_READ")
                                )
                ),
                () -> assertTrue(
                        result.getAuthorities()
                                .stream()
                                .anyMatch(authority ->
                                        authority
                                                .getAuthority()
                                                .equals("USER_CREATE")
                                )
                )
        );

        verify(authUserMapper)
                .findByLoginId("owner");

        verify(authUserMapper)
                .findPermissionCodes(
                        1L,
                        10L
                );
    }

    @Test
    void 사용자가_없으면_권한을_조회하지_않는다() {
        when(authUserMapper.findByLoginId("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService
                        .loadUserByUsername(
                                "  UNKNOWN  "
                        )
        );

        verify(authUserMapper)
                .findByLoginId("unknown");

        verify(authUserMapper, never())
                .findPermissionCodes(
                        anyLong(),
                        anyLong()
                );
    }
}