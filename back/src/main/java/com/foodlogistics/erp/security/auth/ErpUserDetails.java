package com.foodlogistics.erp.security.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class ErpUserDetails implements UserDetails {

    private final Long appUserId;
    private final Long companyId;
    private final String loginId;
    private final String password;
    private final String displayName;
    private final boolean enabled;

    public ErpUserDetails(AuthUser authUser) {
        this.appUserId = authUser.getAppUserId();
        this.companyId = authUser.getCompanyId();
        this.loginId = authUser.getLoginId();
        this.password = authUser.getPassword();
        this.displayName = authUser.getUserName();
        this.enabled = "Y".equalsIgnoreCase(authUser.getUseYn());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
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
