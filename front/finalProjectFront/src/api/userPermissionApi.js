import {
    getAccessToken,
} from "../storage/authStorage.js";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ??
    "http://localhost:8080";

export class ApiRequestError extends Error {
    constructor(message, status, code) {
        super(message);
        this.name = "ApiRequestError";
        this.status = status;
        this.code = code;
    }
}

export function isAccessDeniedError(error) {
    return error instanceof ApiRequestError &&
        (error.status === 403 ||
            error.code === "ACCESS_DENIED");
}

async function request(path, options = {}) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const response = await fetch(
        `${API_BASE_URL}${path}`,
        {
            ...options,
            headers: {
                ...(options.body
                    ? {
                        "Content-Type":
                            "application/json",
                    }
                    : {}),
                ...options.headers,
                Authorization:
                    `Bearer ${accessToken}`,
            },
        },
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        throw new ApiRequestError(
            body?.error?.message ??
            "요청을 처리하지 못했습니다.",
            response.status,
            body?.error?.code,
        );
    }

    return body.data;
}

export function requestUsers() {
    return request("/api/users");
}

export function requestCreateUser(userData) {
    return request("/api/users", {
        method: "POST",
        body: JSON.stringify(userData),
    });
}

export function requestRoles(filters = {}) {
    const params = new URLSearchParams();

    if (filters.keyword?.trim()) {
        params.set(
            "keyword",
            filters.keyword.trim(),
        );
    }

    if (filters.useYn) {
        params.set("useYn", filters.useYn);
    }

    const query = params.toString();

    return request(
        query
            ? `/api/access-control/roles?${query}`
            : "/api/access-control/roles",
    );
}

export function requestCreateRole(roleData) {
    return request(
        "/api/access-control/roles",
        {
            method: "POST",
            body: JSON.stringify(roleData),
        },
    );
}

export function requestPermissions() {
    return request(
        "/api/access-control/permissions",
    );
}

export function requestRolePermissions(roleId) {
    return request(
        `/api/access-control/roles/${roleId}/permissions`,
    );
}

export function requestUpdateRolePermissions(
    roleId,
    permissionIds,
) {
    return request(
        `/api/access-control/roles/${roleId}/permissions`,
        {
            method: "PUT",
            body: JSON.stringify({
                ids: permissionIds,
            }),
        },
    );
}

export function requestUserRoles(userId) {
    return request(
        `/api/access-control/users/${userId}/roles`,
    );
}

export function requestUpdateUserRoles(
    userId,
    roleIds,
) {
    return request(
        `/api/access-control/users/${userId}/roles`,
        {
            method: "PUT",
            body: JSON.stringify({
                ids: roleIds,
            }),
        },
    );
}
