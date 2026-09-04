import { useState } from "react";

function SalesOrderCreateForm({
  onCreate,
  createLoading,
  validationErrors = [],
}) {
  const [customerId, setCustomerId] = useState("");

  const [items, setItems] = useState([
    {
      productUnitId: "",
      orderedQty: "",
      unitPrice: "",
    },
  ]);

  const [formError, setFormError] = useState("");

  function getValidationErrorMessage(fieldName) {
    const validationError = validationErrors.find(
      (error) => error.field === fieldName,
    );

    return validationError?.message;
  }

  function handleItemChange(index, fieldName, value) {
    setItems((currentItems) =>
      currentItems.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [fieldName]: value } : item,
      ),
    );
  }

  function handleAddItem() {
    setItems((currentItems) => [
      ...currentItems,
      {
        productUnitId: "",
        orderedQty: "",
        unitPrice: "",
      },
    ]);
  }

  function handleRemoveItem(index) {
    setItems((currentItems) =>
      currentItems.filter((item, itemIndex) => itemIndex !== index),
    );
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setFormError("");

    const enteredProductUnitIds = items
      .map((item) => Number(item.productUnitId))
      .filter((productUnitId) => productUnitId > 0);

    if (new Set(enteredProductUnitIds).size !== enteredProductUnitIds.length) {
      setFormError("같은 상품 단위는 주문 품목에 한 번만 등록할 수 있습니다.");
      return;
    }

    const created = await onCreate({
      customerId: Number(customerId),
      items: items.map((item) => ({
        productUnitId: Number(item.productUnitId),
        orderedQty: Number(item.orderedQty),
        unitPrice: Number(item.unitPrice),
      })),
    });

    if (created) {
      setCustomerId("");

      setItems([
        {
          productUnitId: "",
          orderedQty: "",
          unitPrice: "",
        },
      ]);
    }
  }

  const customerIdError = getValidationErrorMessage("customerId");

  return (
    <div className="content-panel sales-order-entry-panel">
      <div className="sales-order-entry-title">
        <h2>판매주문 등록</h2>
        <p>
          고객과 주문 품목을 입력하세요. 출고 창고는 출고 단계에서 정합니다.
        </p>
      </div>

      <form onSubmit={handleSubmit}>
        {/* 주문 전체에 공통으로 적용되는 정보 */}
        <section className="sales-order-basic-section">
          <h3>기본 정보</h3>

          <div className="sales-order-basic-grid">
            <div className="sales-order-field">
              <label htmlFor="customerId">고객 거래처 ID</label>
              <input
                id="customerId"
                type="number"
                min="1"
                placeholder="예: 1"
                required
                value={customerId}
                onChange={(event) => setCustomerId(event.target.value)}
              />
              {customerIdError && (
                <p className="field-error">{customerIdError}</p>
              )}
            </div>
          </div>
        </section>

        <section className="sales-order-items-section">
          <div className="sales-order-items-title">
            <div>
              <h3>주문 품목</h3>
              <p>한 줄이 주문 품목 한 건입니다.</p>
            </div>

            <button
              className="sales-order-add-button"
              type="button"
              onClick={handleAddItem}
            >
              + 품목 추가
            </button>
          </div>

          {formError && (
            <p className="sales-order-form-error" role="alert">
              {formError}
            </p>
          )}

          {/* 품목이 늘어나도 카드 대신 표의 행 한 줄만 추가된다. */}
          <div className="sales-order-table-wrap">
            <table className="sales-order-entry-table">
              <thead>
                <tr>
                  <th>번호</th>
                  <th>상품 단위 ID</th>
                  <th>주문 수량</th>
                  <th>판매 단가</th>
                  <th>관리</th>
                </tr>
              </thead>

              <tbody>
                {items.map((item, index) => {
                  const productUnitIdError = getValidationErrorMessage(
                    `items[${index}].productUnitId`,
                  );
                  const orderedQtyError = getValidationErrorMessage(
                    `items[${index}].orderedQty`,
                  );
                  const unitPriceError = getValidationErrorMessage(
                    `items[${index}].unitPrice`,
                  );

                  return (
                    <tr key={index}>
                      <td className="sales-order-row-number">{index + 1}</td>

                      <td>
                        <input
                          aria-label={`품목 ${index + 1} 상품 단위 ID`}
                          type="number"
                          min="1"
                          placeholder="예: 2"
                          required
                          value={item.productUnitId}
                          onChange={(event) =>
                            handleItemChange(
                              index,
                              "productUnitId",
                              event.target.value,
                            )
                          }
                        />
                        {productUnitIdError && (
                          <p className="field-error">{productUnitIdError}</p>
                        )}
                      </td>

                      <td>
                        <input
                          aria-label={`품목 ${index + 1} 주문 수량`}
                          type="number"
                          min="0.001"
                          step="0.001"
                          placeholder="예: 3"
                          required
                          value={item.orderedQty}
                          onChange={(event) =>
                            handleItemChange(
                              index,
                              "orderedQty",
                              event.target.value,
                            )
                          }
                        />
                        {orderedQtyError && (
                          <p className="field-error">{orderedQtyError}</p>
                        )}
                      </td>

                      <td>
                        <input
                          aria-label={`품목 ${index + 1} 판매 단가`}
                          type="number"
                          min="1"
                          step="1"
                          placeholder="예: 25000"
                          required
                          value={item.unitPrice}
                          onChange={(event) =>
                            handleItemChange(
                              index,
                              "unitPrice",
                              event.target.value,
                            )
                          }
                        />
                        {unitPriceError && (
                          <p className="field-error">{unitPriceError}</p>
                        )}
                      </td>

                      <td>
                        {items.length > 1 ? (
                          <button
                            className="sales-order-delete-button"
                            type="button"
                            onClick={() => handleRemoveItem(index)}
                          >
                            삭제
                          </button>
                        ) : (
                          <span className="sales-order-row-fixed">기본 행</span>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </section>

        <div className="sales-order-submit-area">
          <p>판매 단가는 주문 당시 값으로 저장됩니다.</p>

          <button
            className="sales-order-submit-button"
            type="submit"
            disabled={createLoading}
          >
            {createLoading ? "등록 중..." : "판매주문 등록"}
          </button>
        </div>
      </form>
    </div>
  );
}

export default SalesOrderCreateForm;
