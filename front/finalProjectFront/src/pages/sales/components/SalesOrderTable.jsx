function getOrderStatusLabel(orderStatus) {
  const getOrderStatusLabels = {
    DRAFT: "작성중",
    CONFIRMED: "주문확정",
    CANCELLED: "취소",
  };

  return getOrderStatusLabels[orderStatus] || orderStatus;
}

function getShipmentStatusLabel(shipmentStatus) {
  const shipmentStatusLabels = {
    NOT_SHIPPED: "미출고",
    PARTIAL: "부분출고",
    SHIPPED: "출고완료",
    CLOSED: "출고종료",
  };

  return shipmentStatusLabels[shipmentStatus] || shipmentStatus;
}

function SalesOrderTable({
  saleOrders,
  onConfirm,
  confirmingSalesOrderId,
  hasSearchCondition,
}) {
  return (
    <div className="content-panel">
      <h2>판매주문 목록 ({saleOrders.length}건)</h2>

      <table>
        <thead>
          <tr>
            <th>주문번호</th>
            <th>거래처</th>
            <th>주문상태</th>
            <th>출고상태</th>
            <th>관리</th>
          </tr>
        </thead>

        <tbody>
          {saleOrders.length === 0 ? (
            <tr>
              <td colSpan="5" className="empty-message">
                {hasSearchCondition
                  ? "검색 조건에 맞는 판매주문이 없습니다."
                  : "등록된 판매주문이 없습니다."}
              </td>
            </tr>
          ) : (
            saleOrders.map((salesOrder) => (
              <tr key={salesOrder.salesOrderId}>
                <td>{salesOrder.orderNo}</td>
                <td>{salesOrder.customerName}</td>
                <td>{getOrderStatusLabel(salesOrder.orderStatus)}</td>
                <td>{getShipmentStatusLabel(salesOrder.shipmentStatus)}</td>

                <td>
                  {salesOrder.orderStatus === "DRAFT" ? (
                    <button
                      type="button"
                      disabled={
                        confirmingSalesOrderId === salesOrder.salesOrderId
                      }
                      onClick={() => onConfirm(salesOrder.salesOrderId)}
                    >
                      {confirmingSalesOrderId === salesOrder.salesOrderId
                        ? "확정 중..."
                        : "확정"}
                    </button>
                  ) : (
                    <span>확정 완료</span>
                  )}
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

export default SalesOrderTable;
