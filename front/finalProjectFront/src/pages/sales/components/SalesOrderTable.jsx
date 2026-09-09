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
    <section className="sales-order-list-result">
      <p className="sales-order-result-summary">
        조회 결과 {saleOrders.length}건
      </p>

      <div className="sales-order-table-wrap">
        <table className="sales-order-table">
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
                <td colSpan="5" className="sales-order-empty-row">
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

                  <td>
                    <span
                      className={`sales-order-status sales-order-order-status-${salesOrder.orderStatus.toLowerCase()}`}
                    >
                      {getOrderStatusLabel(salesOrder.orderStatus)}
                    </span>
                  </td>

                  <td>
                    <span
                      className={`sales-order-status sales-order-shipment-status-${salesOrder.shipmentStatus.toLowerCase()}`}
                    >
                      {getShipmentStatusLabel(salesOrder.shipmentStatus)}
                    </span>
                  </td>

                  <td>
                    {salesOrder.orderStatus === "DRAFT" ? (
                      <button
                        type="button"
                        className="sales-order-confirm-button"
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
                      "-"
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export default SalesOrderTable;
