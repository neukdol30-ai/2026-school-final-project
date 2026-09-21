function formatQuantity(value) {
  const numberValue = Number(value);

  if (!Number.isFinite(numberValue)) {
    return "-";
  }

  return numberValue.toLocaleString("ko-KR", {
    maximumFractionDigits: 3,
  });
}

function getBaseShippedQty(item) {
  const shippedQty = Number(item.shippedQty);
  const conversionQty = Number(item.conversionQty);

  if (!Number.isFinite(shippedQty) || !Number.isFinite(conversionQty)) {
    return null;
  }

  return shippedQty * conversionQty;
}

function OutboundItemList({
  items,
  onRemoveItem,
  onItemChange,
  onLotAssignmentChange,
  onAddLotAssignment,
  onRemoveLotAssignment,
  isSalesOrderLoaded,
  warehouseSelected,
  lotOptionsLoading,
}) {
  return (
    <section className="outbound-item-section">
      <div className="outbound-item-section-header">
        <div>
          <h2>출고 품목</h2>
          <p>
            {isSalesOrderLoaded
              ? "출고 수량을 입력하고, LOT 관리 상품은 출고할 LOT를 배정해 주세요."
              : "먼저 확정 판매주문을 선택해 주세요."}
          </p>
        </div>
      </div>

      <div className="outbound-item-list">
        {!isSalesOrderLoaded && (
          <p className="outbound-item-empty-message">
            판매주문을 선택하면 남은 출고 가능 품목이 자동으로 표시됩니다.
          </p>
        )}

        {isSalesOrderLoaded &&
          items.map((item, index) => {
            const expectedBaseQty = getBaseShippedQty(item);

            return (
              <div className="outbound-item-card" key={item.salesOrderItemId}>
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
                  <div className="outbound-item-product">
                    <span>상품</span>
                    <strong>{item.productName || `주문 품목 ${index + 1}`}</strong>
                    <small>
                      {item.lotManagedYn === "Y"
                        ? "LOT 관리 상품"
                        : "LOT 비관리 상품"}
                    </small>
                  </div>

                  <div className="outbound-field">
                    <label htmlFor={`shippedQty-${index}`}>
                      출고 수량
                      <span className="outbound-remaining-qty">
                        남은 수량: {item.remainingQty}
                      </span>
                    </label>
                    <input
                      id={`shippedQty-${index}`}
                      type="number"
                      min="0.001"
                      max={item.remainingQty}
                      step="0.001"
                      value={item.shippedQty}
                      onChange={(event) =>
                        onItemChange(index, "shippedQty", event.target.value)
                      }
                      placeholder="예: 1"
                    />
                    {item.lotManagedYn === "Y" && (
                      <small className="outbound-base-qty-guide">
                        기준단위 출고 수량: {formatQuantity(expectedBaseQty)}
                      </small>
                    )}
                  </div>
                </div>

                {item.lotManagedYn === "Y" && (
                  <section className="outbound-lot-assignment-section">
                    <div className="outbound-lot-assignment-header">
                      <div>
                        <h3>LOT 배정</h3>
                        <p>
                          LOT 수량 합계는 기준단위 출고 수량과 같아야 합니다.
                        </p>
                      </div>

                      <button
                        type="button"
                        className="outbound-add-lot-button"
                        onClick={() => onAddLotAssignment(index)}
                        disabled={
                          !warehouseSelected ||
                          lotOptionsLoading ||
                          item.lotOptions.length === 0
                        }
                      >
                        + LOT 추가
                      </button>
                    </div>

                    {!warehouseSelected && (
                      <p className="outbound-lot-guide-message">
                        출고 창고를 먼저 선택하면 해당 창고의 LOT 재고를 조회합니다.
                      </p>
                    )}

                    {warehouseSelected && lotOptionsLoading && (
                      <p className="outbound-lot-guide-message">
                        출고 가능한 LOT를 불러오는 중입니다.
                      </p>
                    )}

                    {warehouseSelected &&
                      !lotOptionsLoading &&
                      item.lotOptions.length === 0 && (
                        <p className="outbound-lot-guide-message error">
                          선택한 창고에 출고 가능한 LOT 재고가 없습니다.
                        </p>
                      )}

                    {item.lotAssignments.map((lot, lotIndex) => (
                      <div
                        className="outbound-lot-assignment-row"
                        key={`${item.salesOrderItemId}-${lotIndex}`}
                      >
                        <div className="outbound-field">
                          <label htmlFor={`lotId-${index}-${lotIndex}`}>
                            출고 LOT
                          </label>
                          <select
                            id={`lotId-${index}-${lotIndex}`}
                            value={lot.lotId}
                            disabled={!warehouseSelected || lotOptionsLoading}
                            onChange={(event) =>
                              onLotAssignmentChange(
                                index,
                                lotIndex,
                                "lotId",
                                event.target.value,
                              )
                            }
                          >
                            <option value="">LOT를 선택하세요</option>
                            {item.lotOptions.map((lotOption) => (
                              <option
                                key={lotOption.lotId}
                                value={lotOption.lotId}
                              >
                                {lotOption.lotNo} · 재고 {formatQuantity(lotOption.quantity)}
                              </option>
                            ))}
                          </select>
                        </div>

                        <div className="outbound-field">
                          <label htmlFor={`baseLotQty-${index}-${lotIndex}`}>
                            LOT 출고 수량 (기준단위)
                          </label>
                          <input
                            id={`baseLotQty-${index}-${lotIndex}`}
                            type="number"
                            min="0.001"
                            step="0.001"
                            value={lot.baseLotQty}
                            onChange={(event) =>
                              onLotAssignmentChange(
                                index,
                                lotIndex,
                                "baseLotQty",
                                event.target.value,
                              )
                            }
                            placeholder="예: 1"
                          />
                        </div>

                        <button
                          type="button"
                          className="outbound-remove-lot-button"
                          onClick={() => onRemoveLotAssignment(index, lotIndex)}
                        >
                          삭제
                        </button>
                      </div>
                    ))}
                  </section>
                )}
              </div>
            );
          })}
      </div>
    </section>
  );
}

export default OutboundItemList;
