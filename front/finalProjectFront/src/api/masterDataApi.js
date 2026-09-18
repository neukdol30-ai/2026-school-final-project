import { getAccessToken } from "../storage/authStorage.js";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function requestSuppliers(keyword = "") {
  const accessToken = getAccessToken();

  if (!accessToken) {
    throw new Error("로그인 정보가 없습니다. 다시 로그인해주세요.");
  }

  const params = new URLSearchParams();

  if (keyword.trim()) {
    params.set("keyword", keyword.trim());
  }

  const queryString = params.toString();

  const requestUrl = queryString
    ? `${API_BASE_URL}/api/suppliers?${queryString}`
    : `${API_BASE_URL}/api/suppliers`;

  const response = await fetch(requestUrl, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  const body = await response.json();

  if (!response.ok || !body.success) {
    const message =
      body.error?.message ?? "공급업체 정보를 불러오지 못했습니다.";

    throw new Error(message);
  }

  return body.data ?? [];
}

// 현재 회사에서 사용할 수 있는 활성 창고 목록을 조회하는 함수
export async function requestWarehouses() {
  const accessToken = getAccessToken();

  if (!accessToken) {
    throw new Error("로그인 정보가 없습니다. 다시 로그인해주세요.");
  }

  const requestUrl = `${API_BASE_URL}/api/warehouses`;

  const response = await fetch(requestUrl, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  const body = await response.json();

  if (!response.ok || !body.success) {
    const message = body.error?.message ?? "창고 정보를 불러오지 못했습니다.";

    throw new Error(message);
  }

  return body.data ?? [];
}

export async function requestProducts(keyword = "") {
  const accessToken = getAccessToken();

  if (!accessToken) {
    throw new Error("로그인 정보가 없습니다. 다시 로그인해주세요.");
  }

  const params = new URLSearchParams();

  // 검색어 앞뒤 공백 제거했을 때 글자가 있는지 확인
  if (keyword.trim()) {
    // 검색어가 있을 때만 keyword를 URL에 추가
    params.set("keyword", keyword.trim());
  }

  const queryString = params.toString();

  const requestUrl = queryString
    ? `${API_BASE_URL}/api/products?${queryString}`
    : `${API_BASE_URL}/api/products`;

  const response = await fetch(requestUrl, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  const body = await response.json();

  if (!response.ok || !body.success) {
    const message = body.error?.message ?? "상품 정보를 불러오지 못했습니다.";

    throw new Error(message);
  }

  return body.data ?? [];
}

export async function requestProductUnits(productId) {
  const accessToken = getAccessToken();

  if (!accessToken) {
    throw new Error("로그인 정보가 없습니다. 다시 로그인해주세요.");
  }

  const numericProductId = Number(productId);

  if (!Number.isInteger(numericProductId) || numericProductId <= 0) {
    throw new Error("올바르지 않은 상품 ID입니다.");
  }

  const requestUrl = `${API_BASE_URL}/api/products/${numericProductId}/units`;

  const response = await fetch(requestUrl, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  const body = await response.json();

  if (!response.ok || !body.success) {
    const message =
      body.error?.message ?? "상품단위 정보를 불러오지 못했습니다.";

    throw new Error(message);
  }

  return body.data ?? [];
}
