import {
    getAccessToken,
} from "../storage/authStorage.js";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ??
    "http://localhost:8080";

const WAREHOUSE_API_URL =
    `${API_BASE_URL}/api/management/warehouses`;

export async function requestWarehouses(
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
        ? `${WAREHOUSE_API_URL}?${queryString}`
        : WAREHOUSE_API_URL;

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
            "창고 목록을 불러오지 못했습니다.";

        throw new Error(message);
    }

    return body.data ?? [];
}

export async function requestCreateWarehouse(
    warehouseData,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const response = await fetch(
        WAREHOUSE_API_URL,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization:
                    `Bearer ${accessToken}`,
            },
            body: JSON.stringify(warehouseData),
        },
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "창고를 등록하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}

export async function requestUpdateWarehouse(
    warehouseId,
    warehouseData,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const numericWarehouseId = Number(warehouseId);

    if (
        !Number.isInteger(numericWarehouseId) ||
        numericWarehouseId <= 0
    ) {
        throw new Error(
            "올바르지 않은 창고 ID입니다.",
        );
    }

    const response = await fetch(
        `${WAREHOUSE_API_URL}/${numericWarehouseId}`,
        {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                Authorization:
                    `Bearer ${accessToken}`,
            },
            body: JSON.stringify(warehouseData),
        },
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "창고 정보를 수정하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}

export async function requestDeactivateWarehouse(
    warehouseId,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const numericWarehouseId = Number(warehouseId);

    if (
        !Number.isInteger(numericWarehouseId) ||
        numericWarehouseId <= 0
    ) {
        throw new Error(
            "올바르지 않은 창고 ID입니다.",
        );
    }

    const response = await fetch(
        `${WAREHOUSE_API_URL}/${numericWarehouseId}/deactivate`,
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
            "창고를 비활성화하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}