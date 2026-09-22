import { getAccessToken } from "../storage/authStorage.js";

// Vite 환경변수에 API 주소가 있으면 그 값을 쓰고, 없으면 로컬 Spring Boot 주소를 사용한다.
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

// 보호 API 호출에 필요한 JWT Bearer Token Header를 만든다.
function getAuthenticatedHeaders(includeJsonContentType = false) {
  const accessToken = getAccessToken();

  if (!accessToken) {
    throw new Error("로그인 정보가 없습니다. 다시 로그인해주세요.");
  }

  const headers = {
    Authorization: `Bearer ${accessToken}`,
  };

  if (includeJsonContentType) {
    headers["Content-Type"] = "application/json";
  }

  return headers;
}

// 서버의 오류 설명에도 내부 상태코드가 포함되므로 화면용 용어로 바꾼다.
// API로 보내는 필드명과 상태값은 바꾸지 않는다.
function formatInboundErrorMessage(message) {
  const statusLabels = {
    DRAFT: "작성중",
    CONFIRMED: "확정",
    CANCELLED: "취소",
    NOT_RECEIVED: "미입고",
    PARTIAL: "부분입고",
    RECEIVED: "입고완료",
    APPROVED: "승인완료",
  };

  return String(message)
    .replace(
      /\b(DRAFT|CONFIRMED|CANCELLED|NOT_RECEIVED|PARTIAL|RECEIVED|APPROVED)\b(?:\((?:작성중|확정|취소|미입고|부분입고|입고완료|승인완료)\))?/g,
      (_, status) => statusLabels[status],
    )
    .replace(/유통기한/g, "소비기한");
}

// 공통 응답 형식의 success/error를 확인하고 실제 data만 화면으로 돌려준다.
async function parseApiResponse(response, fallbackMessage) {
  const body = await response.json().catch(() => null);

  if (!response.ok || !body?.success) {
    const message = body?.error?.message ?? fallbackMessage;
    throw new Error(formatInboundErrorMessage(message));
  }

  return body.data;
}

export async function requestInboundPurchaseOrders() {
  const response = await fetch(`${API_BASE_URL}/api/inbounds/purchase-orders`, {
    method: "GET",
    headers: getAuthenticatedHeaders(),
  });

  const data = await parseApiResponse(
    response,
    "입고 대상 발주 목록을 불러오지 못했습니다.",
  );

  return data ?? [];
}

export async function requestInboundPurchaseOrderItems(purchaseOrderId) {
  const numericPurchaseOrderId = Number(purchaseOrderId);

  if (!Number.isInteger(numericPurchaseOrderId) || numericPurchaseOrderId <= 0) {
    throw new Error("올바르지 않은 발주 ID입니다.");
  }

  const response = await fetch(
    `${API_BASE_URL}/api/inbounds/purchase-orders/${numericPurchaseOrderId}/items`,
    {
      method: "GET",
      headers: getAuthenticatedHeaders(),
    },
  );

  const data = await parseApiResponse(
    response,
    "입고 대상 발주품목을 불러오지 못했습니다.",
  );

  return data ?? [];
}

export async function requestCreateInbound(inboundData) {
  const response = await fetch(`${API_BASE_URL}/api/inbounds`, {
    method: "POST",
    headers: getAuthenticatedHeaders(true),
    body: JSON.stringify(inboundData),
  });

  return parseApiResponse(response, "입고서 생성에 실패했습니다.");
}

export async function requestInboundDetail(inboundId) {
  const numericInboundId = Number(inboundId);

  if (!Number.isInteger(numericInboundId) || numericInboundId <= 0) {
    throw new Error("올바르지 않은 입고 ID입니다.");
  }

  const response = await fetch(`${API_BASE_URL}/api/inbounds/${numericInboundId}`, {
    method: "GET",
    headers: getAuthenticatedHeaders(),
  });

  return parseApiResponse(response, "입고 상세정보를 불러오지 못했습니다.");
}

export async function requestUpdateInboundItems(inboundId, items) {
  const numericInboundId = Number(inboundId);

  if (!Number.isInteger(numericInboundId) || numericInboundId <= 0) {
    throw new Error("올바르지 않은 입고 ID입니다.");
  }

  const response = await fetch(
    `${API_BASE_URL}/api/inbounds/${numericInboundId}/items`,
    {
      method: "PUT",
      headers: getAuthenticatedHeaders(true),
      body: JSON.stringify({ items }),
    },
  );

  return parseApiResponse(response, "입고 품목 저장에 실패했습니다.");
}

export async function requestCancelInbound(inboundId, cancelReason) {
  const numericInboundId = Number(inboundId);

  if (!Number.isInteger(numericInboundId) || numericInboundId <= 0) {
    throw new Error("올바르지 않은 입고 ID입니다.");
  }

  const response = await fetch(
    `${API_BASE_URL}/api/inbounds/${numericInboundId}/cancel`,
    {
      method: "POST",
      headers: getAuthenticatedHeaders(true),
      body: JSON.stringify({ cancelReason }),
    },
  );

  return parseApiResponse(response, "입고서 취소에 실패했습니다.");
}

export async function requestConfirmInbound(inboundId) {
  const numericInboundId = Number(inboundId);

  if (!Number.isInteger(numericInboundId) || numericInboundId <= 0) {
    throw new Error("올바르지 않은 입고 ID입니다.");
  }

  const response = await fetch(
    `${API_BASE_URL}/api/inbounds/${numericInboundId}/confirm`,
    {
      method: "POST",
      headers: getAuthenticatedHeaders(),
    },
  );

  return parseApiResponse(response, "입고 확정에 실패했습니다.");
}
