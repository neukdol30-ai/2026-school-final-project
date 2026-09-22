export function getOrderStatusLabel(orderStatus) {
  const orderStatusLabels = {
    DRAFT: "작성중",
    CONFIRMED: "주문확정",
    CANCELLED: "취소",
  };

  return orderStatusLabels[orderStatus] || orderStatus;
}

export function getShipmentStatusLabel(shipmentStatus) {
  const shipmentStatusLabels = {
    NOT_SHIPPED: "미출고",
    PARTIAL: "부분출고",
    SHIPPED: "출고완료",
    CLOSED: "출고종료",
  };
  return shipmentStatusLabels[shipmentStatus] || shipmentStatus;
}
