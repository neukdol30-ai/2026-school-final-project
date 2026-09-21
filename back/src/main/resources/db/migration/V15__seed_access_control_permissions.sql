MERGE INTO app_permission target
USING (
    SELECT
        'ACCESS_CONTROL_READ' AS permission_code,
        '권한 조회' AS permission_name,
        '권한 그룹과 세부 권한 및 사용자별 권한 배정 상태를 조회합니다.' AS description
    FROM dual

    UNION ALL

    SELECT
        'ACCESS_CONTROL_MANAGE',
        '권한 부여 및 관리',
        '권한 그룹을 등록하고 세부 권한 또는 사용자 권한 그룹을 변경합니다.'
    FROM dual
) source
ON (
    LOWER(TRIM(target.permission_code))
        = LOWER(TRIM(source.permission_code))
)
WHEN MATCHED THEN
    UPDATE SET
        target.permission_name = source.permission_name,
        target.description = source.description
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
