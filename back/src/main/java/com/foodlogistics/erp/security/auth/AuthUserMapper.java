package com.foodlogistics.erp.security.auth;

import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface AuthUserMapper {

    @Select("""
            SELECT 
                app_user_id,
                company_id,
                login_id,
                password,
                user_name,
                use_yn
            FROM app_user
            WHERE LOWER(TRIM(login_id))
            = LOWER(TRIM(#{loginId}))
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
