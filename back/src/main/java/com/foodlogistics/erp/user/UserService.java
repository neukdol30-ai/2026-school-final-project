package com.foodlogistics.erp.user;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.user.dto.UserCreateRequest;
import com.foodlogistics.erp.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserMapper appUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(
            Long companyId,
            Long createdBy,
            UserCreateRequest request
    ) {
        String normalizedLoginId =
                normalizeLoginId(request.getLoginId());

        if (appUserMapper.countByLoginId(
                normalizedLoginId
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_LOGIN_ID
            );
        }

        AppUser appUser = new AppUser();

        appUser.setCompanyId(companyId);
        appUser.setLoginId(normalizedLoginId);
        appUser.setPassword(
                passwordEncoder.encode(
                        request.getInitialPassword()
                )
        );
        appUser.setUserName(
                request.getUserName().trim()
        );
        appUser.setEmail(
                trimToNull(request.getEmail())
        );
        appUser.setPhone(
                trimToNull(request.getPhone())
        );
        appUser.setPositionName(
                trimToNull(request.getPositionName())
        );
        appUser.setUseYn("Y");
        appUser.setCreatedBy(createdBy);
        appUser.setUpdatedBy(createdBy);

        try {
            appUserMapper.insert(appUser);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_LOGIN_ID
            );
        }

        return toResponse(appUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(
            Long companyId
    ) {
        return appUserMapper
                .findAllByCompanyId(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(
            AppUser appUser
    ) {
        return new UserResponse(
                appUser.getAppUserId(),
                appUser.getCompanyId(),
                appUser.getLoginId(),
                appUser.getUserName(),
                appUser.getEmail(),
                appUser.getPhone(),
                appUser.getPositionName(),
                appUser.getUseYn()
        );
    }

    private String normalizeLoginId(String loginId) {
        return loginId
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}