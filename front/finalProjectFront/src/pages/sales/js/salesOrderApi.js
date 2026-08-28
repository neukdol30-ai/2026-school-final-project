const SALES_ORDER_API_URL = "http://localhost:8080/api/sales-orders";

async function requestApi(url, options, defaultErrorMessage) {
  const response = await fetch(url, options);

  const result = await response.json();

  if (!response.ok || !result.success) {
    const error = new Error(result.error?.message || defaultErrorMessage);

    error.validationErrors = result.error?.fields || [];

    throw error;
  }

  return result.data;
}
// 판매주문 목록 조회
export async function getSalesOrders() {
  return requestApi(
    SALES_ORDER_API_URL,
    {},
    "판매주문 목록을 불러오지 못했습니다.",
  );
}
//판매주문 등록
export async function createSalesOrder(requestData) {
  return requestApi(
    SALES_ORDER_API_URL,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestData),
    },
    "판매주문 등록에 실패했습니다.",
  );
}
//판매주문 확정
export async function confirmSalesOrder(salesOrderId) {
  return requestApi(
    `${SALES_ORDER_API_URL}/${salesOrderId}/confirm`,
    {
      method: "POST",
    },
    "판매주문 확정에 실패했습니다.",
  );
}
