import "../css/OutboundItemTable.css";

function formatQty(value) {
  const numericValue = Number(value);

  if (!Number.isFinite(numericValue)) {
    return "-";
  }

  return numericValue.toLocaleString("ko-KR");
}

function OutboundItemTable({ items }) {
  return (
    <section className="content-panel outbound-item-table-panel">
      <div className="outbound-item-table-header">
        <div>
          <h2>출고 품목</h2>
          <p>출고서에 포함된 상품 단위와 실제 출고 수량입니다.</p>
        </div>
      </div>

      <div className="outbound-item-table-wrap">
        <table className="outbound-item-table">
          <thead>
            <tr>
              <th>순번</th>
              <th>판매주문 품목 ID</th>
              <th>상품 단위 ID</th>
              <th>출고 수량</th>
              <th>환산 수량</th>
              <th>기준단위 출고 수량</th>
            </tr>
          </thead>

          <tbody>
            {items.length === 0 ? (
              <tr>
                <td colSpan="6" className="outbound-item-empty-row">
                  출고 품목이 없습니다.
                </td>
              </tr>
            ) : (
              items.map((item) => (
                <tr key={item.outboundItemId}>
                  <td>{item.lineNo}</td>
                  <td>{item.salesOrderItemId}</td>
                  <td>{item.productUnitId}</td>
                  <td>{formatQty(item.shippedQty)}</td>
                  <td>{formatQty(item.conversionQty)}</td>
                  <td>{formatQty(item.baseShippedQty)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export default OutboundItemTable;
