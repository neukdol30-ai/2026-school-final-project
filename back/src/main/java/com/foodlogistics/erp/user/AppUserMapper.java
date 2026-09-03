package com.foodlogistics.erp.user;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}