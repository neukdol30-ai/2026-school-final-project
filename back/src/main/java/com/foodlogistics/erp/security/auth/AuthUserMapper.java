package com.foodlogistics.erp.security.auth;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AuthUserMapper {

    @Select("""
        SELECT
            au.app_user_id,
            au.company_id,
            au.login_id,
            au.password,
            au.user_name,
            au.use_yn
        FROM app_user au
        JOIN company c
          ON c.company_id = au.company_id
        WHERE LOWER(TRIM(au.login_id))
            = LOWER(TRIM(#{loginId}))
          AND c.active_yn = 'Y'
        """)
    @Results(id = "authUserResult",
    value =

    {
        @Result(column = "app_user_id", property = "appUserId", id = true),
        @Result(column = "company_id", property = "companyId"),
        @Result(column = "login_id", property = "loginId"),
        @Result(column = "password", property = "password"),
        @Result(column = "user_name", property = "userName"),
        @Result(column = "use_yn", property = "useYn")
    }
    )

    Optional<AuthUser> findByLoginId(@Param("loginId") String loginId);

    @Select("""
        SELECT DISTINCT
            UPPER(TRIM(p.permission_code))
        FROM app_permission p
        WHERE EXISTS (
            SELECT 1
            FROM app_user_role ur
            JOIN app_role r
              ON r.app_role_id = ur.app_role_id
            JOIN app_user u
              ON u.app_user_id = ur.app_user_id
             AND u.company_id = r.company_id
            JOIN company c
              ON c.company_id = u.company_id
            WHERE ur.app_user_id = #{appUserId}
              AND u.company_id = #{companyId}
              AND u.use_yn = 'Y'
              AND c.active_yn = 'Y'
              AND ur.active_yn = 'Y'
              AND r.use_yn = 'Y'
              AND (
                    r.role_type = 'OWNER'
                    OR EXISTS (
                        SELECT 1
                        FROM app_role_permission rp
                        WHERE rp.app_role_id = r.app_role_id
                          AND rp.app_permission_id
                              = p.app_permission_id
                          AND rp.active_yn = 'Y'
                    )
              )
        )
        ORDER BY UPPER(TRIM(p.permission_code))
        """)
    List<String> findPermissionCodes(
            @Param("appUserId") Long appUserId,
            @Param("companyId") Long companyId
    );
}
