package com.foodlogistics.erp.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void JWT의_authorities를_접두사없이_권한으로_변환한다() {
        SecurityConfig securityConfig =
                new SecurityConfig();

        JwtAuthenticationConverter converter =
                securityConfig
                        .jwtAuthenticationConverter();

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("owner")
                .claim(
                        "authorities",
                        List.of(
                                "USER_READ",
                                "USER_CREATE"
                        )
                )
                .build();

        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken)
                        converter.convert(jwt);

        assertNotNull(authentication);

        Set<String> authorities =
                authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

        assertTrue(
                authorities.containsAll(
                        Set.of(
                                "USER_READ",
                                "USER_CREATE"
                        )
                )
        );

        assertFalse(
                authorities.contains(
                        "SCOPE_USER_READ"
                )
        );

        assertFalse(
                authorities.contains(
                        "SCOPE_USER_CREATE"
                )
        );
    }
}
