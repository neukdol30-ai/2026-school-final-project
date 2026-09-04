package com.foodlogistics.erp.security.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Getter
public class ErpUserDetails implements UserDetails {

    private final Long appUserId;
    private final Long companyId;
    private final String loginId;
    private final String password;
    private final String displayName;
    private final boolean enabled;

    private final List<SimpleGrantedAuthority> authorities;

    public ErpUserDetails(AuthUser authUser) {
        this(authUser, Collections.emptyList());
    }

    public ErpUserDetails(
            AuthUser authUser,
            Collection<String> permissionCodes
    ) {
        this.appUserId = authUser.getAppUserId();
        this.companyId = authUser.getCompanyId();
        this.loginId = authUser.getLoginId();
        this.password = authUser.getPassword();
        this.displayName = authUser.getUserName();
        this.enabled =
                "Y".equalsIgnoreCase(authUser.getUseYn());

        this.authorities = permissionCodes.stream()
                .filter(code ->
                        code != null && !code.isBlank()
                )
                .map(code ->
                        code.trim().toUpperCase(Locale.ROOT)
                )
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }


    @Override
    public Collection<? extends GrantedAuthority>
    getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
