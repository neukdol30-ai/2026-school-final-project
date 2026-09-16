function OutboundBasicInfoFields({
  salesOrderId,
  warehouseId,
  salesOrderOptions,
  warehouseOptions,
  optionsLoading,
  onSalesOrderChange,
  onWarehouseIdChange,
  salesOrderLoading,
  loadedSalesOrder,
}) {
  return (
    <>
      <div className="outbound-form-heading">
        <h2>출고 기본 정보</h2>
        <p>확정 판매주문과 출고 창고를 선택하면 출고 품목이 자동으로 입력됩니다.</p>
      </div>

      <div className="outbound-basic-fields">
        <div className="outbound-field">
          <label htmlFor="salesOrderId">판매주문</label>
          <select
              id="salesOrderId"
              value={salesOrderId}
              onChange={(event) => onSalesOrderChange(event.target.value)}
              disabled={optionsLoading || salesOrderLoading}
            >
              <option value="">
                {optionsLoading
                  ? "판매주문을 불러오는 중입니다."
                  : "확정된 판매주문을 선택하세요"}
              </option>
              {salesOrderOptions.map((salesOrder) => (
                <option
                  key={salesOrder.salesOrderId}
                  value={salesOrder.salesOrderId}
                >
                  {salesOrder.orderNo} · {salesOrder.customerName}
                </option>
              ))}
            </select>
        </div>

        <div className="outbound-field">
          <label htmlFor="warehouseId">출고 창고</label>
          <select
            id="warehouseId"
            value={warehouseId}
            onChange={(event) => onWarehouseIdChange(event.target.value)}
            disabled={optionsLoading}
          >
            <option value="">
              {optionsLoading
                ? "창고를 불러오는 중입니다."
                : "출고 창고를 선택하세요"}
            </option>
            {warehouseOptions.map((warehouse) => (
              <option key={warehouse.warehouseId} value={warehouse.warehouseId}>
                {warehouse.warehouseName}
              </option>
            ))}
          </select>
        </div>
      </div>

      {loadedSalesOrder && (
        <p className="outbound-loaded-order-message">
          <strong>{loadedSalesOrder.orderNo}</strong> 주문의 품목{" "}
          {loadedSalesOrder.items.length}건을 불러왔습니다. 이번에 출고할 품목을
          남기고 출고 수량만 입력해 주세요.
        </p>
      )}
    </>
  );
}

export default OutboundBasicInfoFields;
