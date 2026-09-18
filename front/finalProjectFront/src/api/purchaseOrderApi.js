import { getAccessToken } from "../storage/authStorage.js";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function requestPurchaseOrders(filters = {}) {
  const accessToken = getAccessToken();

  if (!accessToken) {
    throw new Error("로그인 정보가 없습니다. 다시 로그인해주세요.");
  }

  const params = new URLSearchParams();

  if (filters.orderNo?.trim()) {
    params.set("orderNo", filters.orderNo.trim());
  }

  // 공급업체명이 실제로 입력된 경우에만 URL 검색조건에 추가한다.
  if (filters.supplierName?.trim()) {
    // 앞뒤 공백은 제거하고 supplierName이라는 Query Parameter로 Backend에 보낸다.
    params.set("supplierName", filters.supplierName.trim());
  }

  if (filters.orderDateFrom) {
    params.set("orderDateFrom", filters.orderDateFrom);
  }

  if (filters.orderDateTo) {
    params.set("orderDateTo", filters.orderDateTo);
  }

  if (filters.approvalStatus) {
    params.set("approvalStatus", filters.approvalStatus);
  }

  if (filters.receiptStatus) {
    params.set("receiptStatus", filters.receiptStatus);
  }

  const queryString = params.toString();

  const requestUrl = queryString
    ? `${API_BASE_URL}/api/purchase-orders?${queryString}`
    : `${API_BASE_URL}/api/purchase-orders`;

  const response = await fetch(requestUrl, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  const body = await response.json();

  if (!response.ok || !body.success) {
    const message = body.error?.message ?? "발주 목록을 불러오지 못했습니다.";

    throw new Error(message);
  }

  return body.data ?? [];
}

export async function requestPurchaseOrderDetail(purchaseOrderId) {
  const accessToken = getAccessToken();

  if (!accessToken) {
    throw new Error("로그인 정보가 없습니다. 다시 로그인해주세요.");
  }

  const numericPurchaseOrderId = Number(purchaseOrderId);

  if (
    !Number.isInteger(numericPurchaseOrderId) ||
    numericPurchaseOrderId <= 0
  ) {
    throw new Error("올바르지 않은 발주 ID입니다.");
  }

  const requestUrl = `${API_BASE_URL}/api/purchase-orders/${numericPurchaseOrderId}`;

  const response = await fetch(requestUrl, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  const body = await response.json();

  if (!response.ok || !body.success) {
    const message =
      body.error?.message ?? "발주 상세정보를 불러오지 못했습니다.";

    throw new Error(message);
  }

  return body.data;
}

// 새 발주를 Backend에 등록하는 함수
export async function requestCreatePurchaseOrder(purchaseOrderData) {
  const accessToken = getAccessToken();

  if (!accessToken) {
    throw new Error("로그인 정보가 없습니다. 다시 로그인해주세요.");
  }

  const requestUrl = `${API_BASE_URL}/api/purchase-orders`;

  const response = await fetch(requestUrl, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },

    body: JSON.stringify(purchaseOrderData),
  });

  const body = await response.json();

  if (!response.ok || !body.success) {
    const message = body.error?.message ?? "발주 등록에 실패했습니다.";

    throw new Error(message);
  }

  return body.data;
}
