function SalesOrderDetail({ salesOrderDetail, onClose }) {
  if (!SalesOrderDetail) {
    return null;
  }
  return (
    <div className="content-panel">
      <div>
        <h2>판매주문 상세</h2>

        <button type="button" onClick={onClose}>
          상세 닫기
        </button>
      </div>

      <div>
        <p>주문번호: {salesOrderDetail.orderNo}</p>
        <p>거래처: {salesOrderDetail.customerName}</p>
        <p>주문상태: {salesOrderDetail.orderStatus}</p>
        <p>출고상태: {salesOrderDetail.shipmentStatus}</p>
      </div>

      <h3>주문 품목</h3>

      <table>
        <thead>
          <tr>
            <th>상품 단위 ID</th>
            <th>주문 수량</th>
          </tr>
        </thead>

        <tbody>
          {salesOrderDetail.items.length === 0 ? (
            <tr>
              <td colSpan="2" className="empty-message">
                등록된 주문 품목이 없습니다.
              </td>
            </tr>
          ) : (
            salesOrderDetail.items.map((item, index) => (
              <tr key={`${item.productUnitId}-${index}`}>
                <td>{item.productUnitId}</td>
                <td>{item.orderedQty}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

export default SalesOrderDetail;
