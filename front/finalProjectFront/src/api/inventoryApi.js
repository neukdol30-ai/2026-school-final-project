import { getAccessToken } from "../storage/authStorage.js";

// 기존 입고 API와 같은 서버 주소·인증 저장소를 사용한다. 실제 환경변수 값은 출력하지 않는다.
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

// 세 탭의 GET 요청은 공통 응답 형식을 공유한다. 재고를 변경하는 API는 호출하지 않는다.
async function requestInventory(path, params, signal) {
  const accessToken = getAccessToken();
  if (!accessToken) {
    throw new Error("로그인 정보가 없습니다. 다시 로그인해주세요.");
  }

  // URLSearchParams가 검색어를 인코딩한다. companyId는 보내지 않고 서버의 로그인 정보를 사용한다.
  const response = await fetch(`${API_BASE_URL}/api/inventory/${path}?${params}`, {
    method: "GET",
    headers: { Authorization: `Bearer ${accessToken}` },
    signal,
  });
  const body = await response.json().catch(() => null);

  // 기존 화면처럼 서버의 한국어 오류를 우선 사용한다. 인증 실패 시에도 새 권한 검사는 만들지 않는다.
  if (!response.ok || !body?.success) {
    const fallback = response.status === 401
      ? "로그인이 만료되었습니다. 다시 로그인해 주세요."
      : response.status === 403
        ? "조회 권한을 확인해 주세요."
        : "재고 정보를 불러오지 못했습니다.";
    throw new Error(body?.error?.message ?? fallback);
  }

  return body.data ?? [];
}

// 상품·창고는 ID를 외우지 않아도 코드나 이름으로 검색할 수 있다.
function createCommonParams(filters) {
  const params = new URLSearchParams();
  for (const key of ["warehouseKeyword", "productKeyword"]) {
    if (filters[key]?.trim()) {
      params.set(key, filters[key].trim());
    }
  }
  return params;
}

export function requestInventoryStocks(filters, signal) {
  const params = createCommonParams(filters);
  // 재고 0 숨김은 Frontend에서 행을 지우는 대신 Backend SQL 조건으로 처리한다.
  params.set("includeZero", String(filters.includeZero));
  return requestInventory("stocks", params, signal);
}

export function requestInventoryLots(filters, signal) {
  const params = createCommonParams(filters);
  params.set("includeZero", String(filters.includeZero));
  if (filters.lotNo?.trim()) {
    params.set("lotNo", filters.lotNo.trim());
  }
  return requestInventory("lots", params, signal);
}

export function requestInventoryHistory(filters, signal) {
  const params = createCommonParams(filters);
  // 화면에서 선택한 날짜 문자열을 그대로 보낸다. UTC 날짜로 변환하지 않는다.
  params.set("startDate", filters.startDate);
  params.set("endDate", filters.endDate);
  if (filters.lotNo?.trim()) {
    params.set("lotNo", filters.lotNo.trim());
  }
  if (filters.movementType) {
    params.set("movementType", filters.movementType);
  }
  return requestInventory("history", params, signal);
}
