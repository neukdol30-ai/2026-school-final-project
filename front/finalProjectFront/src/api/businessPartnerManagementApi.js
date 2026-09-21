import {
    getAccessToken,
} from "../storage/authStorage.js";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ??
    "http://localhost:8080";

const BUSINESS_PARTNER_API_URL =
    `${API_BASE_URL}/api/business-partners`;

export async function requestBusinessPartners(
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

    if (filters.partnerType) {
        params.set(
            "partnerType",
            filters.partnerType,
        );
    }

    if (filters.useYn) {
        params.set("useYn", filters.useYn);
    }

    const queryString = params.toString();

    const requestUrl = queryString
        ? `${BUSINESS_PARTNER_API_URL}?${queryString}`
        : BUSINESS_PARTNER_API_URL;

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
            "거래처 목록을 불러오지 못했습니다.";

        throw new Error(message);
    }

    return body.data ?? [];
}

export async function requestCreateBusinessPartner(
    partnerData,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const response = await fetch(
        BUSINESS_PARTNER_API_URL,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization:
                    `Bearer ${accessToken}`,
            },
            body: JSON.stringify(partnerData),
        },
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "거래처를 등록하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}

export async function requestUpdateBusinessPartner(
    partnerId,
    partnerData,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const numericPartnerId = Number(partnerId);

    if (
        !Number.isInteger(numericPartnerId) ||
        numericPartnerId <= 0
    ) {
        throw new Error(
            "올바르지 않은 거래처 ID입니다.",
        );
    }

    const response = await fetch(
        `${BUSINESS_PARTNER_API_URL}/${numericPartnerId}`,
        {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                Authorization:
                    `Bearer ${accessToken}`,
            },
            body: JSON.stringify(partnerData),
        },
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message ??
            "거래처 정보를 수정하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}

export async function requestDeactivateBusinessPartner(
    partnerId,
) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        throw new Error(
            "로그인 정보가 없습니다. 다시 로그인해주세요.",
        );
    }

    const numericPartnerId = Number(partnerId);

    if (
        !Number.isInteger(numericPartnerId) ||
        numericPartnerId <= 0
    ) {
        throw new Error(
            "올바르지 않은 거래처 ID입니다.",
        );
    }

    const response = await fetch(
        `${BUSINESS_PARTNER_API_URL}/${numericPartnerId}/deactivate`,
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
            "거래처를 비활성화하지 못했습니다.";

        throw new Error(message);
    }

    return body.data;
}