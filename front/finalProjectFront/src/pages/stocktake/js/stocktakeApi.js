import { getAccessToken } from "../../../storage/authStorage";
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const STOCKTAKE_API_URL = `${API_BASE_URL}/api/stocktakes`;

async function requestApi(url, options = {}, defaultErrorMessage) {
  const accessToken = getAccessToken();

  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
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

export async function getStocktakes() {
  return requestApi(
    STOCKTAKE_API_URL,
    {},
    "재고실사 목록을 불러오지 못했습니다.",
  );
}

export async function getStocktakeWarehouses() {
  return requestApi(
    `${STOCKTAKE_API_URL}/options/warehouses`,
    {},
    "실사 창고 목록을 불러오지 못했습니다.",
  );
}

export async function getStocktakeProducts() {
  return requestApi(
    `${STOCKTAKE_API_URL}/options/products`,
    {},
    "실사 상품 목록을 불러오지 못했습니다.",
  );
}

export async function getStocktakeLots(warehouseId, productId) {
  const searchParams = new URLSearchParams({
    warehouseId: String(warehouseId),
    productId: String(productId),
  });

  return requestApi(
    `${STOCKTAKE_API_URL}/options/lots?${searchParams}`,
    {},
    "LOT 목록을 불러오지 못했습니다.",
  );
}

export async function getStocktakeStockQuantity(warehouseId, productId) {
  const searchParams = new URLSearchParams({
    warehouseId: String(warehouseId),
    productId: String(productId),
  });

  return requestApi(
    `${STOCKTAKE_API_URL}/options/stock-quantity?${searchParams}`,
    {},
    "전산 재고를 불러오지 못했습니다.",
  );
}

export async function getStocktakeDetail(stocktakeId) {
  return requestApi(
    `${STOCKTAKE_API_URL}/${stocktakeId}`,
    {},
    "재고실사 상세 정보를 불러오지 못했습니다.",
  );
}

export async function createStocktake(requestData) {
  return requestApi(
    STOCKTAKE_API_URL,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestData),
    },
    "재고실사 등록에 실패했습니다.",
  );
}

export async function updateStocktake(stocktakeId, requestData) {
  return requestApi(
    `${STOCKTAKE_API_URL}/${stocktakeId}`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestData),
    },
    "재고실사 수정에 실패했습니다.",
  );
}

export async function deleteStocktake(stocktakeId) {
  return requestApi(
    `${STOCKTAKE_API_URL}/${stocktakeId}`,
    {
      method: "DELETE",
    },
    "재고실사 삭제에 실패했습니다.",
  );
}

export async function confirmStocktake(stocktakeId) {
  return requestApi(
    `${STOCKTAKE_API_URL}/${stocktakeId}/confirm`,
    {
      method: "POST",
    },
    "재고실사 확정에 실패했습니다.",
  );
}
