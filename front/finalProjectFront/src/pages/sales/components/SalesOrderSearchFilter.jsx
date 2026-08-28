function SalesOrderSearchFilter({
  keyword,
  onKeywordChange,
  orderStatus,
  onOrderStatusChange,
}) {
  function handleReset() {
    onKeywordChange("");
    onOrderStatusChange("");
  }
  return (
    <div className="content-panel">
      <h2>판매주문 검색</h2>

      <div className="sales-order-filter">
        <div>
          <label htmlFor="keyword">주문번호 / 거래처 </label>
          <input
            id="keyword"
            type="text"
            value={keyword}
            onChange={(event) => onKeywordChange(event.target.value)}
            placeholder="주문번호 또는 거래처명을 입력하세요."
          />
        </div>

        <div>
          <label htmlFor="orderStatus">주문상태</label>
          <select
            id="orderStatus"
            value={orderStatus}
            onChange={(event) => onOrderStatusChange(event.target.value)}
          >
            <option value="">전체</option>
            <option value="DRAFT">작성중</option>
            <option value="CONFIRMED">주문확정</option>
            <option value="CANCELLED">취소</option>
          </select>
        </div>
        <button type="button" onClick={handleReset}>
          검색 초기화
        </button>
      </div>
    </div>
  );
}

export default SalesOrderSearchFilter;
