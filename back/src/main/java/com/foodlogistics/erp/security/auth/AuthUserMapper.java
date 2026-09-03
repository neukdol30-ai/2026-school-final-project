package com.foodlogistics.erp.security.auth;

import org.apache.ibatis.annotations.*;

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
}
