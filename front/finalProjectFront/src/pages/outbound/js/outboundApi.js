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

export async function getOutboundDetail(outboundId) {
  return requestApi(
    `${OUTBOUND_API_URL}/${outboundId}`,
    {},
    "출고서 상세 정보를 불러오지 못했습니다.",
  );
}
