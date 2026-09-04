import {
  getOrderStatusLabel,
  getShipmentStatusLabel,
} from "../js/salesOrderStatus";

function SalesOrderTable({
  saleOrders,
  onConfirm,
  onSelect,
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
                <td>
                  <button
                    className="sales-order-number-button"
                    type="button"
                    onClick={() => onSelect(salesOrder.salesOrderId)}
                  >
                    {salesOrder.orderNo}
                  </button>
                </td>
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
                  ) : salesOrder.orderStatus === "CONFIRMED" ? (
                    <span>확정 완료</span>
                  ) : (
                    <span>취소됨</span>
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
