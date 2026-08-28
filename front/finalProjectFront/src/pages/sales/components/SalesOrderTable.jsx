function SalesOrderTable({ saleOrders, onConfirm, confirmingSalesOrderId }) {
  return (
    <div className="content-panel">
      <h2>판매주문 등록</h2>

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
          {saleOrders.map((salesOrder) => (
            <tr key={salesOrder.salesOrderId}>
              <td>{salesOrder.orderNo}</td>
              <td>{salesOrder.customerName}</td>
              <td>{salesOrder.orderStatus}</td>
              <td>{salesOrder.shipmentStatus}</td>

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
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default SalesOrderTable;
