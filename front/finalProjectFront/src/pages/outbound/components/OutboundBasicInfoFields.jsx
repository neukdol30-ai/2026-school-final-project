function OutboundBasicInfoFields({
  salesOrderId,
  warehouseId,
  onSalesOrderIdChange,
  onWarehouseIdChange,
  onLoadSalesOrder,
  salesOrderLoading,
  loadedSalesOrder,
}) {
  return (
    <>
      <div className="outbound-form-heading">
        <h2>출고 기본 정보</h2>
        <p>판매주문을 조회하면 출고 품목이 자동으로 입력됩니다.</p>
      </div>

      <div className="outbound-basic-fields">
        <div className="outbound-field">
          <label htmlFor="salesOrderId">판매주문 ID</label>

          <div className="outbound-sales-order-input">
            <input
              id="salesOrderId"
              type="number"
              min="1"
              value={salesOrderId}
              onChange={(event) => onSalesOrderIdChange(event.target.value)}
              placeholder="예: 9"
            />

            <button
              type="button"
              className="outbound-order-load-button"
              onClick={onLoadSalesOrder}
              disabled={!salesOrderId || salesOrderLoading}
            >
              {salesOrderLoading ? "조회 중..." : "판매주문 조회"}
            </button>
          </div>
        </div>

        <div className="outbound-field">
          <label htmlFor="warehouseId">출고 창고 ID</label>
          <input
            id="warehouseId"
            type="number"
            min="1"
            value={warehouseId}
            onChange={(event) => onWarehouseIdChange(event.target.value)}
            placeholder="예: 1"
          />
        </div>
      </div>

      {loadedSalesOrder && (
        <p className="outbound-loaded-order-message">
          <strong>{loadedSalesOrder.orderNo}</strong> 주문의 품목{" "}
          {loadedSalesOrder.items.length}건을 불러왔습니다. 출고 수량만 입력해
          주세요.
        </p>
      )}
    </>
  );
}

export default OutboundBasicInfoFields;
