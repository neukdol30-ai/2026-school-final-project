function SalesOrderSearchFilter({
  keyword,
  onKeywordChange,
  orderStatus,
  onOrderStatusChange,
  onSearch,
  onReset,
}) {
  function handleSubmit(event) {
    event.preventDefault();

    onSearch();
  }

  return (
    <form className="sales-order-search-form" onSubmit={handleSubmit}>
      <div className="sales-order-search-fields">
        <label>
          <span>주문번호 / 거래처</span>
          <input
            type="text"
            value={keyword}
            onChange={(event) => onKeywordChange(event.target.value)}
            placeholder="주문번호 또는 거래처명 입력"
          />
        </label>

        <label>
          <span>주문상태</span>
          <select
            value={orderStatus}
            onChange={(event) => onOrderStatusChange(event.target.value)}
          >
            <option value="">전체</option>
            <option value="DRAFT">작성중</option>
            <option value="CONFIRMED">주문확정</option>
            <option value="CANCELLED">취소</option>
          </select>
        </label>
      </div>

      <div className="sales-order-search-actions">
        <button type="submit">조회</button>

        <button type="button" onClick={onReset}>
          초기화
        </button>
      </div>
    </form>
  );
}

export default SalesOrderSearchFilter;
