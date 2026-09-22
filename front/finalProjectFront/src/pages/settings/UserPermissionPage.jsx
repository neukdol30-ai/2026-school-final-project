import {
    useEffect,
    useMemo,
    useState,
} from "react";
import ManagementModal
    from "../../components/common/ManagementModal.jsx";
import ManagementAccessDenied
    from "../../components/common/ManagementAccessDenied.jsx";
import { hasAuthority }
    from "../../storage/authStorage.js";
import {
    isAccessDeniedError,
    requestCreateRole,
    requestCreateUser,
    requestPermissions,
    requestRolePermissions,
    requestRoles,
    requestUpdateRolePermissions,
    requestUpdateUserRoles,
    requestUserRoles,
    requestUsers,
} from "../../api/userPermissionApi.js";
import "./UserPermissionPage.css";

const INITIAL_USER_FILTERS = {
    keyword: "",
    useYn: "",
};

const INITIAL_USER_FORM = {
    loginId: "",
    initialPassword: "",
    userName: "",
    email: "",
    phone: "",
    positionName: "",
};

const INITIAL_ROLE_FILTERS = {
    keyword: "",
    useYn: "",
};

const INITIAL_ROLE_FORM = {
    roleCode: "",
    roleName: "",
    description: "",
};

const PERMISSION_GROUPS = [
    {
        key: "ACCESS_CONTROL",
        title: "권한 관리",
        description:
            "권한 정보를 조회하거나 권한 그룹과 사용자별 권한을 부여·변경하는 권한입니다.",
        matches: (code) =>
            code.startsWith("ACCESS_CONTROL_"),
    },
    {
        key: "USER",
        title: "사용자 관리",
        description:
            "사내 사용자 계정을 조회하고 신규 사용자를 등록하는 권한입니다.",
        matches: (code) =>
            code.startsWith("USER_"),
    },
    {
        key: "PRODUCT",
        title: "상품 관리",
        description:
            "상품 기본정보와 상품별 단위를 조회·등록·수정·비활성화하는 권한입니다.",
        matches: (code) =>
            code.startsWith("PRODUCT_"),
    },
    {
        key: "BUSINESS_PARTNER",
        title: "거래처 관리",
        description:
            "공급업체와 판매처 정보를 조회·등록·수정·비활성화하는 권한입니다.",
        matches: (code) =>
            code.startsWith(
                "BUSINESS_PARTNER_",
            ),
    },
    {
        key: "WAREHOUSE",
        title: "창고 관리",
        description:
            "회사 창고 정보를 조회·등록·수정·비활성화하는 권한입니다.",
        matches: (code) =>
            code.startsWith("WAREHOUSE_"),
    },
    {
        key: "UNIT",
        title: "단위 관리",
        description:
            "상품에서 사용하는 공통 단위를 조회·등록·수정·비활성화하는 권한입니다.",
        matches: (code) =>
            code.startsWith("UNIT_"),
    },
];

const PERMISSION_ACTION_ORDER = [
    "_READ",
    "_CREATE",
    "_UPDATE",
    "_DEACTIVATE",
    "_MANAGE",
];

function permissionActionOrder(permissionCode) {
    const index = PERMISSION_ACTION_ORDER.findIndex(
        (suffix) => permissionCode.endsWith(suffix),
    );

    return index === -1
        ? PERMISSION_ACTION_ORDER.length
        : index;
}

function includeRequiredReadIds(
    permissions,
    selectedIds,
) {
    const completedIds = new Set(selectedIds);

    PERMISSION_GROUPS.forEach((group) => {
        const groupPermissions = permissions.filter(
            (permission) =>
                group.matches(
                    permission.permissionCode,
                ),
        );
        const readPermission = groupPermissions.find(
            (permission) =>
                permission.permissionCode.endsWith(
                    "_READ",
                ),
        );
        const hasDependentPermission =
            groupPermissions.some(
                (permission) =>
                    !permission.permissionCode.endsWith(
                        "_READ",
                    ) &&
                    completedIds.has(
                        permission.appPermissionId,
                    ),
            );

        if (readPermission && hasDependentPermission) {
            completedIds.add(
                readPermission.appPermissionId,
            );
        }
    });

    return [...completedIds];
}

function PermissionGroups({
                              permissions,
                              selectedIds,
                              saving,
                              onToggle,
                          }) {
    const groupedPermissionIds = new Set();

    const groups = PERMISSION_GROUPS
        .map((group) => {
            const items = permissions
                .filter((permission) =>
                    group.matches(
                        permission.permissionCode,
                    ),
                )
                .sort(
                    (left, right) =>
                        permissionActionOrder(
                            left.permissionCode,
                        ) -
                        permissionActionOrder(
                            right.permissionCode,
                        ),
                );

            items.forEach((permission) =>
                groupedPermissionIds.add(
                    permission.appPermissionId,
                ),
            );

            return {
                ...group,
                items,
            };
        })
        .filter((group) => group.items.length > 0);

    const ungrouped = permissions.filter(
        (permission) =>
            !groupedPermissionIds.has(
                permission.appPermissionId,
            ),
    );

    if (ungrouped.length > 0) {
        groups.push({
            key: "OTHER",
            title: "기타",
            description:
                "기존 업무 분류에 포함되지 않은 추가 권한입니다.",
            items: ungrouped,
        });
    }

    return (
        <div className="user-permission-groups">
            {groups.map((group) => {
                const readPermission = group.items.find(
                    (permission) =>
                        permission.permissionCode.endsWith(
                            "_READ",
                        ),
                );
                const readId =
                    readPermission?.appPermissionId;
                const readSelected = !readId ||
                    selectedIds.includes(readId);
                const groupIds = group.items.map(
                    (permission) =>
                        permission.appPermissionId,
                );

                return (
                    <section
                        key={group.key}
                        className="user-permission-group"
                    >
                        <div className="user-permission-group-heading">
                            <h4>{group.title}</h4>
                            <p>{group.description}</p>
                            {readId && !readSelected && (
                                <p className="user-permission-read-guide">
                                    조회 권한을 먼저 선택해야 하위 권한을 설정할 수 있습니다.
                                </p>
                            )}
                        </div>

                        <div className="user-permission-option-list">
                            {group.items.map(
                                (permission) => {
                                    const isRead =
                                        permission.appPermissionId ===
                                        readId;
                                    const dependencyDisabled =
                                        !isRead &&
                                        !readSelected;

                                    return (
                                        <label
                                            key={permission.appPermissionId}
                                            className={
                                                dependencyDisabled
                                                    ? "user-permission-option dependency-disabled"
                                                    : isRead
                                                        ? "user-permission-option read-permission"
                                                        : "user-permission-option"
                                            }
                                        >
                                            <input
                                                type="checkbox"
                                                checked={
                                                    selectedIds.includes(
                                                        permission.appPermissionId,
                                                    )
                                                }
                                                onChange={() =>
                                                    onToggle(
                                                        permission.appPermissionId,
                                                        groupIds,
                                                        readId,
                                                    )
                                                }
                                                disabled={
                                                    saving ||
                                                    dependencyDisabled
                                                }
                                            />
                                            <span>
                                                <strong>
                                                    {permission.permissionName}
                                                    {isRead && (
                                                        <em>상위 권한</em>
                                                    )}
                                                </strong>
                                                <small>
                                                    {permission.permissionCode}
                                                    {permission.description
                                                        ? ` · ${permission.description}`
                                                        : ""}
                                                </small>
                                            </span>
                                        </label>
                                    );
                                },
                            )}
                        </div>
                    </section>
                );
            })}
        </div>
    );
}

function AssignmentModal({
                             title,
                             description,
                             options,
                             selectedIds,
                             saving,
                             emptyMessage,
                             notice,
                             grouped = false,
                             onToggle,
                             onClose,
                             onSave,
                         }) {
    return (
        <ManagementModal
            onClose={onClose}
            closeDisabled={saving}
        >
            <form
                className="user-permission-assignment-card"
                onSubmit={onSave}
            >
                <div className="user-permission-card-heading">
                    <h2>{title}</h2>
                    <p>{description}</p>
                </div>

                {options.length === 0 ? (
                    <div className="user-permission-option-empty">
                        {emptyMessage}
                    </div>
                ) : grouped ? (
                    <PermissionGroups
                        permissions={options}
                        selectedIds={selectedIds}
                        saving={saving}
                        onToggle={onToggle}
                    />
                ) : (
                    <div className="user-permission-option-list">
                        {options.map((option) => {
                            const optionId =
                                option.appRoleId ??
                                option.appPermissionId;
                            const optionCode =
                                option.roleCode ??
                                option.permissionCode;
                            const optionName =
                                option.roleName ??
                                option.permissionName;

                            return (
                                <label
                                    key={optionId}
                                    className="user-permission-option"
                                >
                                    <input
                                        type="checkbox"
                                        checked={
                                            selectedIds.includes(
                                                optionId,
                                            )
                                        }
                                        onChange={() =>
                                            onToggle(optionId)
                                        }
                                        disabled={saving}
                                    />

                                    <span>
                                        <strong>
                                            {optionName}
                                        </strong>
                                        <small>
                                            {optionCode}
                                            {option.description
                                                ? ` · ${option.description}`
                                                : ""}
                                        </small>
                                    </span>
                                </label>
                            );
                        })}
                    </div>
                )}

                {notice && (
                    <div className="user-permission-form-notice">
                        {notice}
                    </div>
                )}

                <div className="user-permission-form-actions">
                    <button
                        type="button"
                        onClick={onClose}
                        disabled={saving}
                    >
                        취소
                    </button>
                    <button
                        type="submit"
                        className="primary"
                        disabled={saving}
                    >
                        {saving ? "저장 중..." : "저장"}
                    </button>
                </div>
            </form>
        </ManagementModal>
    );
}

function UserPermissionPage() {
    const canReadUsers = hasAuthority("USER_READ");
    const canCreateUsers = hasAuthority("USER_CREATE");
    const canReadAccessControl = hasAuthority(
        "ACCESS_CONTROL_READ",
    );
    const canManageAccessControl = hasAuthority(
        "ACCESS_CONTROL_MANAGE",
    );
    const canReadPage =
        canReadUsers && canReadAccessControl;

    const [activeTab, setActiveTab] =
        useState("users");
    const [userFilters, setUserFilters] =
        useState(INITIAL_USER_FILTERS);
    const [roleFilters, setRoleFilters] =
        useState(INITIAL_ROLE_FILTERS);
    const [users, setUsers] = useState([]);
    const [roles, setRoles] = useState([]);
    const [
        availablePermissions,
        setAvailablePermissions,
    ] = useState([]);
    const [
        createPermissionIds,
        setCreatePermissionIds,
    ] = useState([]);
    const [loadingUsers, setLoadingUsers] =
        useState(true);
    const [loadingRoles, setLoadingRoles] =
        useState(true);
    const [errorMessage, setErrorMessage] =
        useState("");
    const [successMessage, setSuccessMessage] =
        useState("");
    const [accessDenied, setAccessDenied] =
        useState(false);

    const [userForm, setUserForm] =
        useState(INITIAL_USER_FORM);
    const [roleForm, setRoleForm] =
        useState(INITIAL_ROLE_FORM);
    const [userFormOpen, setUserFormOpen] =
        useState(false);
    const [roleFormOpen, setRoleFormOpen] =
        useState(false);
    const [saving, setSaving] = useState(false);

    const [selectedUser, setSelectedUser] =
        useState(null);
    const [userRoleOptions, setUserRoleOptions] =
        useState([]);
    const [selectedRoleIds, setSelectedRoleIds] =
        useState([]);
    const [userRolesSaving, setUserRolesSaving] =
        useState(false);

    const [selectedRole, setSelectedRole] =
        useState(null);
    const [
        permissionOptions,
        setPermissionOptions,
    ] = useState([]);
    const [
        selectedPermissionIds,
        setSelectedPermissionIds,
    ] = useState([]);
    const [
        permissionsSaving,
        setPermissionsSaving,
    ] = useState(false);

    useEffect(() => {
        if (!canReadPage) {
            return undefined;
        }

        let cancelled = false;

        Promise.allSettled([
            requestUsers(),
            requestRoles(INITIAL_ROLE_FILTERS),
            requestPermissions(),
        ]).then(([
                     userResult,
                     roleResult,
                     permissionResult,
                 ]) => {
            if (cancelled) {
                return;
            }

            if (
                [
                    userResult,
                    roleResult,
                    permissionResult,
                ].some(
                    (result) =>
                        result.status === "rejected" &&
                        isAccessDeniedError(result.reason),
                )
            ) {
                setAccessDenied(true);
                setLoadingUsers(false);
                setLoadingRoles(false);
                return;
            }

            if (userResult.status === "fulfilled") {
                setUsers(userResult.value ?? []);
            } else {
                setErrorMessage(
                    userResult.reason instanceof Error
                        ? userResult.reason.message
                        : "사용자 목록을 불러오지 못했습니다.",
                );
            }

            if (roleResult.status === "fulfilled") {
                setRoles(roleResult.value ?? []);
            } else {
                setErrorMessage(
                    roleResult.reason instanceof Error
                        ? roleResult.reason.message
                        : "권한 그룹을 불러오지 못했습니다.",
                );
            }

            if (
                permissionResult.status ===
                "fulfilled"
            ) {
                setAvailablePermissions(
                    permissionResult.value ?? [],
                );
            } else {
                setErrorMessage(
                    permissionResult.reason
                    instanceof Error
                        ? permissionResult.reason.message
                        : "세부 권한을 불러오지 못했습니다.",
                );
            }

            setLoadingUsers(false);
            setLoadingRoles(false);
        });

        return () => {
            cancelled = true;
        };
    }, [canReadPage]);

    const filteredUsers = useMemo(() => {
        const keyword =
            userFilters.keyword.trim().toLowerCase();

        return users.filter((user) => {
            const keywordMatches =
                !keyword ||
                user.loginId
                    ?.toLowerCase()
                    .includes(keyword) ||
                user.userName
                    ?.toLowerCase()
                    .includes(keyword);
            const useYnMatches =
                !userFilters.useYn ||
                user.useYn === userFilters.useYn;

            return keywordMatches && useYnMatches;
        });
    }, [userFilters, users]);

    async function loadUsers() {
        setLoadingUsers(true);
        setErrorMessage("");

        try {
            setUsers(await requestUsers() ?? []);
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "사용자 목록을 불러오지 못했습니다.",
            );
        } finally {
            setLoadingUsers(false);
        }
    }

    async function loadRoles(filters = roleFilters) {
        setLoadingRoles(true);
        setErrorMessage("");

        try {
            setRoles(await requestRoles(filters) ?? []);
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "권한 그룹을 불러오지 못했습니다.",
            );
        } finally {
            setLoadingRoles(false);
        }
    }

    function changeObject(setter) {
        return (event) => {
            const { name, value } = event.target;

            setter((previous) => ({
                ...previous,
                [name]: value,
            }));
        };
    }

    function selectTab(tab) {
        setActiveTab(tab);
        setUserFormOpen(false);
        setRoleFormOpen(false);
        setErrorMessage("");
        setSuccessMessage("");
    }

    async function handleCreateUser(event) {
        event.preventDefault();
        setSaving(true);
        setErrorMessage("");
        setSuccessMessage("");

        try {
            await requestCreateUser({
                loginId: userForm.loginId.trim(),
                initialPassword:
                    userForm.initialPassword,
                userName: userForm.userName.trim(),
                email:
                    userForm.email.trim() || null,
                phone:
                    userForm.phone.trim() || null,
                positionName:
                    userForm.positionName.trim() ||
                    null,
            });

            setUserForm(INITIAL_USER_FORM);
            setUserFormOpen(false);
            setSuccessMessage(
                "사용자가 등록되었습니다.",
            );
            await loadUsers();
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "사용자를 등록하지 못했습니다.",
            );
        } finally {
            setSaving(false);
        }
    }

    async function handleCreateRole(event) {
        event.preventDefault();
        setSaving(true);
        setErrorMessage("");
        setSuccessMessage("");

        try {
            await requestCreateRole({
                roleCode: roleForm.roleCode.trim(),
                roleName: roleForm.roleName.trim(),
                description:
                    roleForm.description.trim() ||
                    null,
                permissionIds:
                    createPermissionIds,
            });

            setRoleForm(INITIAL_ROLE_FORM);
            setCreatePermissionIds([]);
            setRoleFormOpen(false);
            setSuccessMessage(
                "권한 그룹이 등록되었습니다.",
            );
            await loadRoles();
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "권한 그룹을 등록하지 못했습니다.",
            );
        } finally {
            setSaving(false);
        }
    }

    async function openUserRoles(user) {
        setErrorMessage("");

        try {
            const options =
                await requestUserRoles(
                    user.appUserId,
                ) ?? [];

            setUserRoleOptions(options);
            setSelectedRoleIds(
                options
                    .filter(
                        (role) =>
                            role.assignedYn === "Y",
                    )
                    .map((role) => role.appRoleId),
            );
            setSelectedUser(user);
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "사용자 권한을 불러오지 못했습니다.",
            );
        }
    }

    async function openRolePermissions(role) {
        if (role.roleType === "OWNER") {
            return;
        }

        setErrorMessage("");

        try {
            const options =
                await requestRolePermissions(
                    role.appRoleId,
                ) ?? [];

            setPermissionOptions(options);
            setSelectedPermissionIds(
                includeRequiredReadIds(
                    options,
                    options
                    .filter(
                        (permission) =>
                            permission.assignedYn === "Y",
                    )
                    .map(
                        (permission) =>
                            permission.appPermissionId,
                    ),
                ),
            );
            setSelectedRole(role);
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "세부 권한을 불러오지 못했습니다.",
            );
        }
    }

    function toggleId(setter, id) {
        setter((previousIds) =>
            previousIds.includes(id)
                ? previousIds.filter(
                    (previousId) =>
                        previousId !== id,
                )
                : [...previousIds, id],
        );
    }

    function togglePermissionId(
        setter,
        id,
        groupIds,
        readId,
    ) {
        setter((previousIds) => {
            if (previousIds.includes(id)) {
                if (id === readId) {
                    return previousIds.filter(
                        (previousId) =>
                            !groupIds.includes(previousId),
                    );
                }

                return previousIds.filter(
                    (previousId) =>
                        previousId !== id,
                );
            }

            if (readId && id !== readId &&
                !previousIds.includes(readId)) {
                return previousIds;
            }

            return [...previousIds, id];
        });
    }

    async function saveUserRoles(event) {
        event.preventDefault();
        setUserRolesSaving(true);

        try {
            await requestUpdateUserRoles(
                selectedUser.appUserId,
                selectedRoleIds,
            );
            setSelectedUser(null);
            setSuccessMessage(
                "사용자 권한 그룹이 저장되었습니다.",
            );
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "사용자 권한을 저장하지 못했습니다.",
            );
        } finally {
            setUserRolesSaving(false);
        }
    }

    async function saveRolePermissions(event) {
        event.preventDefault();
        setPermissionsSaving(true);

        try {
            await requestUpdateRolePermissions(
                selectedRole.appRoleId,
                selectedPermissionIds,
            );
            setSelectedRole(null);
            setSuccessMessage(
                "권한 그룹의 세부 권한이 저장되었습니다.",
            );
            await loadRoles();
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "세부 권한을 저장하지 못했습니다.",
            );
        } finally {
            setPermissionsSaving(false);
        }
    }

    if (!canReadPage || accessDenied) {
        return (
            <div className="page user-permission-page">
                <ManagementAccessDenied resourceName="사용자 및 권한" />
            </div>
        );
    }

    return (
        <div className="page user-permission-page">
            <div className="user-permission-header">
                <div>
                    <h1>사용자 / 권한 관리</h1>
                    <p>
                        사내 사용자 계정과 업무별 접근 권한을 관리합니다.
                    </p>
                </div>

                <button
                    type="button"
                    className={
                        (activeTab === "users"
                            ? canCreateUsers
                            : canManageAccessControl)
                            ? "user-permission-primary-button"
                            : "user-permission-primary-button permission-disabled"
                    }
                    disabled={
                        activeTab === "users"
                            ? !canCreateUsers
                            : !canManageAccessControl
                    }
                    onClick={() => {
                        setErrorMessage("");
                        setSuccessMessage("");

                        if (activeTab === "users") {
                            setUserForm(
                                INITIAL_USER_FORM,
                            );
                            setUserFormOpen(true);
                        } else {
                            setRoleForm(
                                INITIAL_ROLE_FORM,
                            );
                            setCreatePermissionIds([]);
                            setRoleFormOpen(true);
                        }
                    }}
                >
                    {activeTab === "users"
                        ? "사용자 등록"
                        : "권한 그룹 등록"}
                </button>
            </div>

            <div className="user-permission-tabs">
                <button
                    type="button"
                    className={
                        activeTab === "users"
                            ? "active"
                            : ""
                    }
                    onClick={() => selectTab("users")}
                >
                    사용자 관리
                </button>
                <button
                    type="button"
                    className={
                        activeTab === "roles"
                            ? "active"
                            : ""
                    }
                    onClick={() => selectTab("roles")}
                >
                    권한 그룹 관리
                </button>
            </div>

            {successMessage && (
                <p className="user-permission-message success">
                    {successMessage}
                </p>
            )}
            {errorMessage && (
                <p
                    className="user-permission-message error"
                    role="alert"
                >
                    {errorMessage}
                </p>
            )}

            {activeTab === "users" ? (
                <>
                    <form
                        className="user-permission-search-form"
                        onSubmit={(event) => {
                            event.preventDefault();
                            loadUsers();
                        }}
                    >
                        <div className="user-permission-search-fields">
                            <label>
                                <span>사용자 검색</span>
                                <input
                                    name="keyword"
                                    value={userFilters.keyword}
                                    onChange={changeObject(
                                        setUserFilters,
                                    )}
                                    placeholder="로그인 ID 또는 사용자명"
                                />
                            </label>
                            <label>
                                <span>사용상태</span>
                                <select
                                    name="useYn"
                                    value={userFilters.useYn}
                                    onChange={changeObject(
                                        setUserFilters,
                                    )}
                                >
                                    <option value="">전체</option>
                                    <option value="Y">사용</option>
                                    <option value="N">미사용</option>
                                </select>
                            </label>
                        </div>
                        <div className="user-permission-search-actions">
                            <button type="submit">
                                조회
                            </button>
                            <button
                                type="button"
                                onClick={() =>
                                    setUserFilters(
                                        INITIAL_USER_FILTERS,
                                    )
                                }
                            >
                                초기화
                            </button>
                        </div>
                    </form>

                    <div className="user-permission-result-summary">
                        조회 결과 {filteredUsers.length}건
                    </div>
                    <div className="user-permission-table-wrap">
                        <table className="user-permission-table">
                            <thead>
                            <tr>
                                <th>로그인 ID</th>
                                <th>사용자명</th>
                                <th>직급</th>
                                <th>이메일</th>
                                <th>전화번호</th>
                                <th>사용상태</th>
                                <th>관리</th>
                            </tr>
                            </thead>
                            <tbody>
                            {loadingUsers ? (
                                <tr>
                                    <td
                                        colSpan="7"
                                        className="user-permission-empty-row"
                                    >
                                        사용자 목록을 불러오는 중입니다.
                                    </td>
                                </tr>
                            ) : filteredUsers.length === 0 ? (
                                <tr>
                                    <td
                                        colSpan="7"
                                        className="user-permission-empty-row"
                                    >
                                        조회된 사용자가 없습니다.
                                    </td>
                                </tr>
                            ) : (
                                filteredUsers.map((user) => (
                                    <tr key={user.appUserId}>
                                        <td>{user.loginId}</td>
                                        <td>{user.userName}</td>
                                        <td>
                                            {user.positionName ?? "-"}
                                        </td>
                                        <td>{user.email ?? "-"}</td>
                                        <td>{user.phone ?? "-"}</td>
                                        <td>
                                            <span
                                                className={
                                                    user.useYn === "Y"
                                                        ? "user-permission-status active"
                                                        : "user-permission-status inactive"
                                                }
                                            >
                                                {user.useYn === "Y"
                                                    ? "사용"
                                                    : "미사용"}
                                            </span>
                                        </td>
                                        <td>
                                            <button
                                                type="button"
                                                className="user-permission-manage-button"
                                                disabled={
                                                    user.ownerYn === "Y" ||
                                                    !canManageAccessControl
                                                }
                                                onClick={() =>
                                                    openUserRoles(
                                                        user,
                                                    )
                                                }
                                            >
                                                {user.ownerYn === "Y"
                                                    ? "OWNER 보호"
                                                    : canManageAccessControl
                                                        ? "권한 설정"
                                                        : "권한 없음"}
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>
                </>
            ) : (
                <>
                    <form
                        className="user-permission-search-form"
                        onSubmit={(event) => {
                            event.preventDefault();
                            loadRoles(roleFilters);
                        }}
                    >
                        <div className="user-permission-search-fields">
                            <label>
                                <span>권한 그룹 검색</span>
                                <input
                                    name="keyword"
                                    value={roleFilters.keyword}
                                    onChange={changeObject(
                                        setRoleFilters,
                                    )}
                                    placeholder="권한 그룹 코드 또는 그룹명"
                                />
                            </label>
                            <label>
                                <span>사용상태</span>
                                <select
                                    name="useYn"
                                    value={roleFilters.useYn}
                                    onChange={changeObject(
                                        setRoleFilters,
                                    )}
                                >
                                    <option value="">전체</option>
                                    <option value="Y">사용</option>
                                    <option value="N">미사용</option>
                                </select>
                            </label>
                        </div>
                        <div className="user-permission-search-actions">
                            <button type="submit">
                                조회
                            </button>
                            <button
                                type="button"
                                onClick={() => {
                                    setRoleFilters(
                                        INITIAL_ROLE_FILTERS,
                                    );
                                    loadRoles(
                                        INITIAL_ROLE_FILTERS,
                                    );
                                }}
                            >
                                초기화
                            </button>
                        </div>
                    </form>

                    <div className="user-permission-result-summary">
                        조회 결과 {roles.length}건
                    </div>
                    <div className="user-permission-table-wrap">
                        <table className="user-permission-table role-table">
                            <thead>
                            <tr>
                                <th>권한 그룹 코드</th>
                                <th>권한 그룹명</th>
                                <th>구분</th>
                                <th>권한 수</th>
                                <th>사용상태</th>
                                <th>관리</th>
                            </tr>
                            </thead>
                            <tbody>
                            {loadingRoles ? (
                                <tr>
                                    <td
                                        colSpan="6"
                                        className="user-permission-empty-row"
                                    >
                                        권한 그룹을 불러오는 중입니다.
                                    </td>
                                </tr>
                            ) : roles.length === 0 ? (
                                <tr>
                                    <td
                                        colSpan="6"
                                        className="user-permission-empty-row"
                                    >
                                        조회된 권한 그룹이 없습니다.
                                    </td>
                                </tr>
                            ) : (
                                roles.map((role) => (
                                    <tr key={role.appRoleId}>
                                        <td>{role.roleCode}</td>
                                        <td>{role.roleName}</td>
                                        <td>
                                            {role.roleType === "OWNER"
                                                ? "소유자"
                                                : "사용자 정의"}
                                        </td>
                                        <td>
                                            {role.roleType === "OWNER"
                                                ? "전체"
                                                : role.permissionCount}
                                        </td>
                                        <td>
                                            <span
                                                className={
                                                    role.useYn === "Y"
                                                        ? "user-permission-status active"
                                                        : "user-permission-status inactive"
                                                }
                                            >
                                                {role.useYn === "Y"
                                                    ? "사용"
                                                    : "미사용"}
                                            </span>
                                        </td>
                                        <td>
                                            <button
                                                type="button"
                                                className="user-permission-manage-button"
                                                disabled={
                                                    role.roleType ===
                                                    "OWNER" ||
                                                    !canManageAccessControl
                                                }
                                                onClick={() =>
                                                    openRolePermissions(
                                                        role,
                                                    )
                                                }
                                            >
                                                {role.roleType === "OWNER"
                                                    ? "전체 권한"
                                                    : canManageAccessControl
                                                        ? "권한 설정"
                                                        : "권한 없음"}
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>
                </>
            )}

            {userFormOpen && (
                <ManagementModal
                    onClose={() =>
                        setUserFormOpen(false)
                    }
                    closeDisabled={saving}
                >
                    <form
                        className="user-permission-edit-card"
                        onSubmit={handleCreateUser}
                    >
                        <div className="user-permission-card-heading">
                            <h2>사용자 등록</h2>
                            <p>
                                신규 사원이 사용할 계정 정보를 입력합니다.
                            </p>
                        </div>
                        <div className="user-permission-form-grid">
                            {[
                                ["loginId", "로그인 ID *", "로그인 ID", "text"],
                                ["userName", "사용자명 *", "사용자명", "text"],
                                ["initialPassword", "초기 비밀번호 *", "8자 이상 입력", "password"],
                                ["positionName", "직급", "직급명", "text"],
                                ["email", "이메일", "name@company.com", "email"],
                                ["phone", "전화번호", "010-0000-0000", "text"],
                            ].map(
                                ([
                                     name,
                                     label,
                                     placeholder,
                                     type,
                                 ]) => (
                                    <label key={name}>
                                        <span>{label}</span>
                                        <input
                                            type={type}
                                            name={name}
                                            value={userForm[name]}
                                            onChange={changeObject(
                                                setUserForm,
                                            )}
                                            placeholder={
                                                placeholder
                                            }
                                            disabled={saving}
                                        />
                                    </label>
                                ),
                            )}
                        </div>
                        <div className="user-permission-form-actions">
                            <button
                                type="button"
                                onClick={() =>
                                    setUserFormOpen(false)
                                }
                                disabled={saving}
                            >
                                취소
                            </button>
                            <button
                                type="submit"
                                className="primary"
                                disabled={saving}
                            >
                                {saving
                                    ? "등록 중..."
                                    : "등록"}
                            </button>
                        </div>
                    </form>
                </ManagementModal>
            )}

            {roleFormOpen && (
                <ManagementModal
                    onClose={() =>
                        setRoleFormOpen(false)
                    }
                    closeDisabled={saving}
                >
                    <form
                        className="user-permission-edit-card"
                        onSubmit={handleCreateRole}
                    >
                        <div className="user-permission-card-heading">
                            <h2>권한 그룹 등록</h2>
                            <p>
                                그룹에 포함할 세부 권한을 선택합니다.
                            </p>
                        </div>
                        <div className="user-permission-form-grid user-permission-role-form-grid">
                            <label>
                                <span>권한 그룹 코드 *</span>
                                <input
                                    name="roleCode"
                                    value={roleForm.roleCode}
                                    onChange={changeObject(
                                        setRoleForm,
                                    )}
                                    placeholder="예: INVENTORY_MANAGER"
                                    disabled={saving}
                                />
                            </label>
                            <label>
                                <span>권한 그룹명 *</span>
                                <input
                                    name="roleName"
                                    value={roleForm.roleName}
                                    onChange={changeObject(
                                        setRoleForm,
                                    )}
                                    placeholder="예: 재고 관리자"
                                    disabled={saving}
                                />
                            </label>
                            <label className="wide">
                                <span>설명</span>
                                <input
                                    name="description"
                                    value={roleForm.description}
                                    onChange={changeObject(
                                        setRoleForm,
                                    )}
                                    placeholder="권한 그룹의 사용 목적"
                                    disabled={saving}
                                />
                            </label>
                        </div>
                        <div className="user-permission-create-permissions">
                            <h3>세부 권한</h3>

                            {availablePermissions.length === 0 ? (
                                <div className="user-permission-option-empty">
                                    등록된 세부 권한이 없습니다.
                                </div>
                            ) : (
                                <PermissionGroups
                                    permissions={
                                        availablePermissions
                                    }
                                    selectedIds={
                                        createPermissionIds
                                    }
                                    saving={saving}
                                    onToggle={(
                                        id,
                                        groupIds,
                                        readId,
                                    ) =>
                                        togglePermissionId(
                                            setCreatePermissionIds,
                                            id,
                                            groupIds,
                                            readId,
                                        )
                                    }
                                />
                            )}
                        </div>
                        <div className="user-permission-form-actions">
                            <button
                                type="button"
                                onClick={() =>
                                    setRoleFormOpen(false)
                                }
                                disabled={saving}
                            >
                                취소
                            </button>
                            <button
                                type="submit"
                                className="primary"
                                disabled={saving}
                            >
                                {saving
                                    ? "등록 중..."
                                    : "등록"}
                            </button>
                        </div>
                    </form>
                </ManagementModal>
            )}

            {selectedUser && (
                <AssignmentModal
                    title={`${selectedUser.userName} 권한 설정`}
                    description="사용자에게 적용할 권한 그룹을 선택합니다."
                    options={userRoleOptions}
                    selectedIds={selectedRoleIds}
                    saving={userRolesSaving}
                    emptyMessage="배정 가능한 사용자 정의 권한 그룹이 없습니다."
                    notice="변경된 권한은 해당 사용자가 다음에 로그인할 때 JWT에 반영됩니다."
                    onToggle={(id) =>
                        toggleId(
                            setSelectedRoleIds,
                            id,
                        )
                    }
                    onClose={() =>
                        setSelectedUser(null)
                    }
                    onSave={saveUserRoles}
                />
            )}

            {selectedRole && (
                <AssignmentModal
                    title={`${selectedRole.roleName} 세부 권한`}
                    description="이 권한 그룹에서 허용할 업무를 선택합니다."
                    options={permissionOptions}
                    selectedIds={selectedPermissionIds}
                    saving={permissionsSaving}
                    emptyMessage="등록된 세부 권한이 없습니다."
                    grouped
                    onToggle={(
                        id,
                        groupIds,
                        readId,
                    ) =>
                        togglePermissionId(
                            setSelectedPermissionIds,
                            id,
                            groupIds,
                            readId,
                        )
                    }
                    onClose={() =>
                        setSelectedRole(null)
                    }
                    onSave={saveRolePermissions}
                />
            )}
        </div>
    );
}

export default UserPermissionPage;
