import { getAccessToken } from "../../../storage/authStorage";
const OUTBOUND_API_URL = "http://localhost:8080/api/outbounds";

async function requestApi(url, options = {}, defaultErrorMessage) {
  const accessToken = getAccessToken(); // 로그인 후 저장된 JWT 토큰

  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      ...(accessToken
        ? { Authorization: `Bearer ${accessToken}` } // 토큰이 있으면 요청에 포함
        : {}),
    },
  });

  const result = await response.json();

  if (!response.ok || !result.success) {
    const error = new Error(result.error?.message || defaultErrorMessage);

    error.validationErrors = result.error?.fields || [];

    throw error;
  }

  return result.data;
}
//출고서 초안 등록
export async function createOutbound(requestData) {
  return requestApi(
    OUTBOUND_API_URL,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestData),
    },
    "출고서 등록에 실패했습니다.",
  );
}
//출고서 확정
export async function confirmOutbound(outboundId) {
  return requestApi(
    `${OUTBOUND_API_URL}/${outboundId}/confirm`,
    {
      method: "POST",
    },
    "출고서 확정에 실패했습니다.",
  );
}

export async function getOutbounds() {
  return requestApi(OUTBOUND_API_URL, {}, "출고 목록을 불러오지 못했습니다.");
}

// 출고서를 작성할 때 선택할 사용 중인 창고 목록
export async function getOutboundWarehouses() {
  return requestApi(
    `${OUTBOUND_API_URL}/options/warehouses`,
    {},
    "출고 창고 목록을 불러오지 못했습니다.",
  );
}

// 확정 출고를 취소하고 재고·판매주문 출고수량을 원복한다.
export async function cancelOutbound(outboundId, cancelReason) {
  return requestApi(
    `${OUTBOUND_API_URL}/${outboundId}/cancel`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ cancelReason }),
    },
    "출고서 취소에 실패했습니다.",
  );
}

// 선택한 창고·상품에 실제 재고가 남아 있는 LOT 목록
export async function getOutboundLots(warehouseId, productId) {
  const searchParams = new URLSearchParams({
    warehouseId: String(warehouseId),
    productId: String(productId),
  });

  return requestApi(
    `${OUTBOUND_API_URL}/options/lots?${searchParams}`,
    {},
    "출고 가능한 LOT 목록을 불러오지 못했습니다.",
  );
}

export async function getOutboundDetail(outboundId) {
  return requestApi(
    `${OUTBOUND_API_URL}/${outboundId}`,
    {},
    "출고서 상세 정보를 불러오지 못했습니다.",
  );
}
