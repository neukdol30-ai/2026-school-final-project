import {
  getOrderStatusLabel,
  getShipmentStatusLabel,
} from "../js/salesOrderStatus";

function SalesOrderDetail({ salesOrderDetail, onClose }) {
  if (!salesOrderDetail) {
    return null;
  }

  return (
    <section className="content-panel sales-order-detail-panel">
      <div className="sales-order-detail-header">
        <div>
          <span className="sales-order-detail-eyebrow">SALES ORDER</span>
          <h2>판매주문 상세</h2>
          <p className="sales-order-detail-order-no">
            {salesOrderDetail.orderNo}
          </p>
        </div>

        <button
          className="sales-order-detail-close-button"
          type="button"
          onClick={onClose}
        >
          목록으로
        </button>
      </div>

      {/* 주문의 핵심 상태를 한눈에 보여 주는 영역 */}
      <div className="sales-order-detail-summary-grid">
        <div className="sales-order-summary-card">
          <span>거래처</span>
          <strong>{salesOrderDetail.customerName}</strong>
        </div>

        <div className="sales-order-summary-card">
          <span>주문 상태</span>
          <strong
            className={`sales-order-status-badge order-status-${salesOrderDetail.orderStatus.toLowerCase()}`}
          >
            {getOrderStatusLabel(salesOrderDetail.orderStatus)}
          </strong>
        </div>

        <div className="sales-order-summary-card">
          <span>출고 상태</span>
          <strong
            className={`sales-order-status-badge shipment-status-${salesOrderDetail.shipmentStatus.toLowerCase()}`}
          >
            {getShipmentStatusLabel(salesOrderDetail.shipmentStatus)}
          </strong>
        </div>

        <div className="sales-order-summary-card">
          <span>주문 품목</span>
          <strong>{salesOrderDetail.items.length}건</strong>
        </div>
      </div>

      <div className="sales-order-detail-items-header">
        <div>
          <h3>주문 품목</h3>
          <p>주문에 포함된 상품 단위와 수량입니다.</p>
        </div>
      </div>

      <div className="sales-order-detail-table-wrap">
        <table className="sales-order-detail-table">
          <thead>
            <tr>
              <th>번호</th>
              <th>상품 단위 ID</th>
              <th>주문 수량</th>
            </tr>
          </thead>

          <tbody>
            {salesOrderDetail.items.length === 0 ? (
              <tr>
                <td colSpan="3" className="empty-message">
                  등록된 주문 품목이 없습니다.
                </td>
              </tr>
            ) : (
              salesOrderDetail.items.map((item, index) => (
                <tr key={`${item.productUnitId}-${index}`}>
                  <td>{index + 1}</td>
                  <td>{item.productUnitId}</td>
                  <td>{Number(item.orderedQty).toLocaleString("ko-KR")}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export default SalesOrderDetail;
