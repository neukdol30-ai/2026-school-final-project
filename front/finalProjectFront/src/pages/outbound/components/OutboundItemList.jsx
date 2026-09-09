function OutboundItemList({
  items,
  onAddItem,
  onRemoveItem,
  onItemChange,
  isSalesOrderLoaded,
}) {
  return (
    <section className="outbound-item-section">
      <div className="outbound-item-section-header">
        <div>
          <h2>출고 품목</h2>
          <p>
            {isSalesOrderLoaded
              ? "판매주문 품목을 불러왔습니다. 출고 수량만 입력해 주세요."
              : "한 줄이 출고 품목 한 건입니다."}
          </p>
        </div>

        <button
          type="button"
          className="outbound-add-button"
          onClick={onAddItem}
          disabled={isSalesOrderLoaded}
        >
          + 품목 추가
        </button>
      </div>

      <div className="outbound-item-list">
        {items.map((item, index) => (
          <div className="outbound-item-card" key={index}>
            <div className="outbound-item-card-header">
              <strong>출고 품목 {index + 1}</strong>

              <button
                type="button"
                className="outbound-remove-button"
                onClick={() => onRemoveItem(index)}
              >
                삭제
              </button>
            </div>

            <div className="outbound-item-fields">
              <div className="outbound-field">
                <label htmlFor={`salesOrderItemId-${index}`}>
                  판매주문 품목 ID
                </label>
                <input
                  id={`salesOrderItemId-${index}`}
                  type="number"
                  min="1"
                  value={item.salesOrderItemId}
                  readOnly={isSalesOrderLoaded}
                  className={isSalesOrderLoaded ? "outbound-item-readonly" : ""}
                  onChange={(event) =>
                    onItemChange(index, "salesOrderItemId", event.target.value)
                  }
                />
              </div>

              <div className="outbound-field">
                <label htmlFor={`productUnitId-${index}`}>상품 단위 ID</label>
                <input
                  id={`productUnitId-${index}`}
                  type="number"
                  min="1"
                  value={item.productUnitId}
                  readOnly={isSalesOrderLoaded}
                  className={isSalesOrderLoaded ? "outbound-item-readonly" : ""}
                  onChange={(event) =>
                    onItemChange(index, "productUnitId", event.target.value)
                  }
                />
              </div>

              <div className="outbound-field">
                <label htmlFor={`shippedQty-${index}`}>
                  출고 수량
                  {isSalesOrderLoaded && (
                    <span className="outbound-remaining-qty">
                      남은 수량: {item.remainingQty}
                    </span>
                  )}
                </label>
                <input
                  id={`shippedQty-${index}`}
                  type="number"
                  min="0.001"
                  max={isSalesOrderLoaded ? item.remainingQty : undefined}
                  step="0.001"
                  value={item.shippedQty}
                  onChange={(event) =>
                    onItemChange(index, "shippedQty", event.target.value)
                  }
                  placeholder="예: 1"
                />
              </div>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

export default OutboundItemList;
