MERGE INTO app_permission target
    USING (
        SELECT
            'USER_READ' AS permission_code,
            '사용자 조회' AS permission_name,
            '회사 사용자 목록을 조회합니다.' AS description
        FROM dual

        UNION ALL

        SELECT
            'USER_CREATE',
            '사용자 등록',
            '회사 사용자를 등록합니다.'
        FROM dual

        UNION ALL

        SELECT
            'BUSINESS_PARTNER_READ',
            '거래처 조회',
            '거래처 정보를 조회합니다.'
        FROM dual

        UNION ALL

        SELECT
            'BUSINESS_PARTNER_CREATE',
            '거래처 등록',
            '거래처를 등록합니다.'
        FROM dual

        UNION ALL

        SELECT
            'BUSINESS_PARTNER_UPDATE',
            '거래처 수정',
            '거래처 정보를 수정합니다.'
        FROM dual

        UNION ALL

        SELECT
            'BUSINESS_PARTNER_DEACTIVATE',
            '거래처 비활성화',
            '거래처를 비활성화합니다.'
        FROM dual

        UNION ALL

        SELECT
            'WAREHOUSE_READ',
            '창고 조회',
            '창고 정보를 조회합니다.'
        FROM dual

        UNION ALL

        SELECT
            'WAREHOUSE_CREATE',
            '창고 등록',
            '창고를 등록합니다.'
        FROM dual

        UNION ALL

        SELECT
            'WAREHOUSE_UPDATE',
            '창고 수정',
            '창고 정보를 수정합니다.'
        FROM dual

        UNION ALL

        SELECT
            'WAREHOUSE_DEACTIVATE',
            '창고 비활성화',
            '창고를 비활성화합니다.'
        FROM dual

        UNION ALL

        SELECT
            'UNIT_READ',
            '단위 조회',
            '공통 단위 정보를 조회합니다.'
        FROM dual

        UNION ALL

        SELECT
            'UNIT_CREATE',
            '단위 등록',
            '공통 단위를 등록합니다.'
        FROM dual

        UNION ALL

        SELECT
            'UNIT_UPDATE',
            '단위 수정',
            '공통 단위 정보를 수정합니다.'
        FROM dual

        UNION ALL

        SELECT
            'UNIT_DEACTIVATE',
            '단위 비활성화',
            '공통 단위를 비활성화합니다.'
        FROM dual

        UNION ALL

        SELECT
            'PRODUCT_READ',
            '상품 조회',
            '상품 및 상품 단위를 조회합니다.'
        FROM dual

        UNION ALL

        SELECT
            'PRODUCT_CREATE',
            '상품 등록',
            '상품 및 상품 단위를 등록합니다.'
        FROM dual

        UNION ALL

        SELECT
            'PRODUCT_UPDATE',
            '상품 수정',
            '상품 및 상품 단위를 수정합니다.'
        FROM dual

        UNION ALL

        SELECT
            'PRODUCT_DEACTIVATE',
            '상품 비활성화',
            '상품 및 상품 단위를 비활성화합니다.'
        FROM dual
    ) source
    ON (
        LOWER(TRIM(target.permission_code))
            = LOWER(TRIM(source.permission_code))
        )
    WHEN MATCHED THEN
        UPDATE SET
            target.permission_name =
                    source.permission_name,
            target.description =
                    source.description
    WHEN NOT MATCHED THEN
        INSERT (
                permission_code,
                permission_name,
                description
            )
            VALUES (
                       source.permission_code,
                       source.permission_name,
                       source.description
                   );