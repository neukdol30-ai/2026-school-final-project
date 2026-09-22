import { getAccessToken } from "../storage/authStorage.js";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

const PRODUCT_API_URL =
    `${API_BASE_URL}/api/management/products`;

export async function requestManagementProducts(filters = {}) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const params = new URLSearchParams();

    if (filters.keyword?.trim()) {
        params.set("keyword", filters.keyword.trim());
    }

    if (filters.lotManagedYn) {
        params.set("lotManagedYn", filters.lotManagedYn);
    }

    if (filters.taxType) {
        params.set("taxType", filters.taxType);
    }

    if (filters.storageType) {
        params.set("storageType", filters.storageType);
    }

    if (filters.useYn) {
        params.set("useYn", filters.useYn);
    }

    const queryString = params.toString();

    const requestUrl = queryString
        ? `${PRODUCT_API_URL}?${queryString}`
        : PRODUCT_API_URL;

    const response = await fetch(requestUrl, {
        method: "GET",
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
    });

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "상품 목록을 불러오지 못했습니다.";

        throw new Error(message);
    }

    return body.data ?? [];
}

export async function requestCreateProduct(
    productData,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const response = await fetch(
        PRODUCT_API_URL,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization:
                    `Bearer ${accessToken}`,
            },
            body: JSON.stringify(productData),
        },
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "상품을 등록하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}

export async function requestUpdateProduct(
    productId,
    productData,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const response = await fetch(
        `${PRODUCT_API_URL}/${productId}`,
        {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                Authorization:
                    `Bearer ${accessToken}`,
            },
            body: JSON.stringify(productData),
        },
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "상품 정보를 수정하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}

export async function requestManagementProductUnits(
    productId,
    useYn = "",
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const numericProductId = Number(productId);

    if (
        !Number.isInteger(numericProductId) ||
        numericProductId <= 0
    ) {
        throw new Error(
            "올바르지 않은 상품 ID입니다.",
        );
    }

    const params = new URLSearchParams();

    if (useYn) {
        params.set("useYn", useYn);
    }

    const queryString = params.toString();

    const requestUrl = queryString
        ? `${PRODUCT_API_URL}/${numericProductId}/units?${queryString}`
        : `${PRODUCT_API_URL}/${numericProductId}/units`;

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
            "상품단위 목록을 불러오지 못했습니다.";

        throw new Error(message);
    }

    return body.data ?? [];
}

export async function requestAvailableUnits() {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const response = await fetch(
        `${API_BASE_URL}/api/units?useYn=Y`,
        {
            method: "GET",
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
            "단위 목록을 불러오지 못했습니다.";

        throw new Error(message);
    }

    return body.data ?? [];
}

export async function requestCreateProductUnit(
    productId,
    productUnitData,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const numericProductId = Number(productId);

    if (
        !Number.isInteger(numericProductId) ||
        numericProductId <= 0
    ) {
        throw new Error(
            "올바르지 않은 상품 ID입니다.",
        );
    }

    const response = await fetch(
        `${PRODUCT_API_URL}/${numericProductId}/units`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization:
                    `Bearer ${accessToken}`,
            },
            body: JSON.stringify(productUnitData),
        },
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "상품단위를 등록하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}

export async function requestDeactivateProduct(
    productId,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const response = await fetch(
        `${PRODUCT_API_URL}/${productId}/deactivate`,
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
            "상품을 비활성화하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}

export async function requestUpdateProductUnit(
    productId,
    productUnitId,
    productUnitData,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const numericProductId = Number(productId);
    const numericProductUnitId =
        Number(productUnitId);

    if (
        !Number.isInteger(numericProductId) ||
        numericProductId <= 0 ||
        !Number.isInteger(numericProductUnitId) ||
        numericProductUnitId <= 0
    ) {
        throw new Error(
            "올바르지 않은 상품단위 ID입니다.",
        );
    }

    const response = await fetch(
        `${PRODUCT_API_URL}/${numericProductId}/units/${numericProductUnitId}`,
        {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                Authorization:
                    `Bearer ${accessToken}`,
            },
            body: JSON.stringify(productUnitData),
        },
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "상품단위를 수정하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}

export async function requestDeactivateProductUnit(
    productId,
    productUnitId,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const numericProductId = Number(productId);
    const numericProductUnitId =
        Number(productUnitId);

    if (
        !Number.isInteger(numericProductId) ||
        numericProductId <= 0 ||
        !Number.isInteger(numericProductUnitId) ||
        numericProductUnitId <= 0
    ) {
        throw new Error(
            "올바르지 않은 상품단위 ID입니다.",
        );
    }

    const response = await fetch(
        `${PRODUCT_API_URL}/${numericProductId}/units/${numericProductUnitId}/deactivate`,
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
            "상품단위를 비활성화하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}