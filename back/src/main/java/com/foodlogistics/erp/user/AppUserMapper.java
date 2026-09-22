package com.foodlogistics.erp.user;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AppUserMapper {

    @Select("""
            SELECT COUNT(*)
            FROM app_user
            WHERE LOWER(TRIM(login_id))
                = LOWER(TRIM(#{loginId}))
            """)
    int countByLoginId(
            @Param("loginId") String loginId
    );

    @Insert("""
            INSERT INTO app_user (
                company_id,
                login_id,
                password,
                user_name,
                email,
                phone,
                position_name,
                use_yn,
                created_by,
                updated_by
            ) VALUES (
                #{companyId},
                #{loginId},
                #{password},
                #{userName},
                #{email},
                #{phone},
                #{positionName},
                #{useYn},
                #{createdBy},
                #{updatedBy}
            )
            """)
    @Options(
            useGeneratedKeys = true,
            keyProperty = "appUserId",
            keyColumn = "app_user_id"
    )
    int insert(AppUser appUser);

    @Select("""
        SELECT
            app_user_id,
            company_id,
            login_id,
            user_name,
            email,
            phone,
            position_name,
            use_yn,
            CASE
                WHEN EXISTS (
                    SELECT 1
                    FROM app_user_role ur
                    JOIN app_role r
                      ON r.app_role_id = ur.app_role_id
                    WHERE ur.app_user_id = u.app_user_id
                      AND ur.active_yn = 'Y'
                      AND r.company_id = u.company_id
                      AND r.role_type = 'OWNER'
                      AND r.use_yn = 'Y'
                ) THEN 'Y'
                ELSE 'N'
            END AS owner_yn
        FROM app_user u
        WHERE company_id = #{companyId}
        ORDER BY
            use_yn DESC,
            user_name ASC,
            app_user_id ASC
        """)
    @Results(
            id = "appUserListResult",
            value = {
                    @Result(
                            column = "app_user_id",
                            property = "appUserId",
                            id = true
                    ),
                    @Result(
                            column = "company_id",
                            property = "companyId"
                    ),
                    @Result(
                            column = "login_id",
                            property = "loginId"
                    ),
                    @Result(
                            column = "user_name",
                            property = "userName"
                    ),
                    @Result(
                            column = "email",
                            property = "email"
                    ),
                    @Result(
                            column = "phone",
                            property = "phone"
                    ),
                    @Result(
                            column = "position_name",
                            property = "positionName"
                    ),
                    @Result(
                            column = "use_yn",
                            property = "useYn"
                    ),
                    @Result(
                            column = "owner_yn",
                            property = "ownerYn"
                    )
            }
    )
    List<AppUser> findAllByCompanyId(
            @Param("companyId") Long companyId
    );

    @Select("""
        SELECT
            app_user_id,
            company_id,
            login_id,
            user_name,
            email,
            phone,
            position_name,
            use_yn,
            CASE
                WHEN EXISTS (
                    SELECT 1
                    FROM app_user_role ur
                    JOIN app_role r
                      ON r.app_role_id = ur.app_role_id
                    WHERE ur.app_user_id = u.app_user_id
                      AND ur.active_yn = 'Y'
                      AND r.company_id = u.company_id
                      AND r.role_type = 'OWNER'
                      AND r.use_yn = 'Y'
                ) THEN 'Y'
                ELSE 'N'
            END AS owner_yn
        FROM app_user u
        WHERE company_id = #{companyId}
          AND app_user_id = #{appUserId}
        """)
    @ResultMap("appUserListResult")
    AppUser findById(
            @Param("companyId") Long companyId,
            @Param("appUserId") Long appUserId
    );
}
