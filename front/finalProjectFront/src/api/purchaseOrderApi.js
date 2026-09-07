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

  if (filters.supplierId) {
    params.set("supplierId", filters.supplierId);
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
