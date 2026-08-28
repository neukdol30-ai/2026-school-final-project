package com.foodlogistics.erp.security.auth;

import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.constraintvalidators.hv.NormalizedValidator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ErpUserDetailsService implements UserDetailsService {

    private final AuthUserMapper authUserMapper;
    private final NormalizedValidator normalizedValidator;

    @Override
    public UserDetails loadUserByUsername(String loginId)
            throws UsernameNotFoundException {
        String normalizedLoginId = normalizedLoginId(loginId);

        AuthUser authUser = authUserMapper
                .findByLoginId(normalizedLoginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return new ErpUserDetails(authUser);
    }

    private String normalizedLoginId(String loginId) {
        if (loginId == null) {
            return "";
        }

        return loginId.trim().toLowerCase(Locale.ROOT);
    }
}
