import {
    getAccessToken,
} from "../storage/authStorage.js";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ??
    "http://localhost:8080";

const UNIT_API_URL =
    `${API_BASE_URL}/api/units`;

export async function requestUnits(
    filters = {},
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

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

    const queryString = params.toString();

    const requestUrl = queryString
        ? `${UNIT_API_URL}?${queryString}`
        : UNIT_API_URL;

    const response = await fetch(requestUrl, {
        method: "GET",
        headers: {
            Authorization:
                `Bearer ${accessToken}`,
        },
    });

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "단위 목록을 불러오지 못했습니다.";

        throw new Error(message);
    }

    return body.data ?? [];
}

export async function requestCreateUnit(
    unitData,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const response = await fetch(
        UNIT_API_URL,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization:
                    `Bearer ${accessToken}`,
            },
            body: JSON.stringify(unitData),
        },
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "단위를 등록하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}

export async function requestUpdateUnit(
    unitId,
    unitData,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const numericUnitId = Number(unitId);

    if (
        !Number.isInteger(numericUnitId) ||
        numericUnitId <= 0
    ) {
        throw new Error(
            "올바르지 않은 단위 ID입니다.",
        );
    }

    const response = await fetch(
        `${UNIT_API_URL}/${numericUnitId}`,
        {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                Authorization:
                    `Bearer ${accessToken}`,
            },
            body: JSON.stringify(unitData),
        },
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "단위 정보를 수정하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}

export async function requestDeactivateUnit(
    unitId,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const numericUnitId = Number(unitId);

    if (
        !Number.isInteger(numericUnitId) ||
        numericUnitId <= 0
    ) {
        throw new Error(
            "올바르지 않은 단위 ID입니다.",
        );
    }

    const response = await fetch(
        `${UNIT_API_URL}/${numericUnitId}/deactivate`,
        {
            method: "PATCH",
            headers: {
                Authorization:
                    `Bearer ${accessToken}`,
            },
        },
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "단위를 비활성화하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}