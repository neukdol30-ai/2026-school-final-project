import { useState } from "react";

function SalesOrderCreateForm({
  onCreate,
  createLoading,
  validationErrors = [],
}) {
  const [customerId, setCustomerId] = useState("");

  const [warehouseId, setWarehouseId] = useState("");

  const [items, setItems] = useState([
    {
      productUnitId: "",
      orderedQty: "",
      unitPrice: "",
    },
  ]);

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

    const created = await onCreate({
      customerId: Number(customerId),
      warehouseId: Number(warehouseId),
      items: items.map((item) => ({
        productUnitId: Number(item.productUnitId),
        orderedQty: Number(item.orderedQty),
        unitPrice: Number(item.unitPrice),
      })),
    });

    if (created) {
      setCustomerId("");
      setWarehouseId("");

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
  const warehouseIdError = getValidationErrorMessage("warehouseId");

  return (
    <div className="content-panel">
      <h2>판매주문 등록</h2>

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="customerId">고객 거래처 ID</label>
          <input
            id="customerId"
            type="number"
            min="1"
            required
            value={customerId}
            onChange={(event) => setCustomerId(event.target.value)}
          />

          {customerIdError && <p className="field-error">{customerIdError}</p>}
        </div>

        <div>
          <label htmlFor="warehouseId">출고 창고 ID</label>
          <input
            id="warehouseId"
            type="number"
            min="1"
            required
            value={warehouseId}
            onChange={(event) => setWarehouseId(event.target.value)}
          />

          {warehouseIdError && (
            <p className="field-error">{warehouseIdError}</p>
          )}
        </div>

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
            <div className="sales-order-item-row" key={index}>
              <h3>주문 품목 {index + 1}</h3>

              <div>
                <label htmlFor={`productUnitId-${index}`}>상품 단위 ID</label>
                <input
                  id={`productUnitId-${index}`}
                  type="number"
                  min="1"
                  required
                  value={item.productUnitId}
                  onChange={(event) =>
                    handleItemChange(index, "productUnitId", event.target.value)
                  }
                />

                {productUnitIdError && (
                  <p className="field-error">{productUnitIdError}</p>
                )}
              </div>

              <div>
                <label htmlFor={`orderedQty-${index}`}>주문 수량</label>
                <input
                  id={`orderedQty-${index}`}
                  type="number"
                  min="0.001"
                  step="0.001"
                  required
                  value={item.orderedQty}
                  onChange={(event) =>
                    handleItemChange(index, "orderedQty", event.target.value)
                  }
                />

                {orderedQtyError && (
                  <p className="field-error">{orderedQtyError}</p>
                )}
              </div>

              <div>
                <label htmlFor={`unitPrice-${index}`}>판매 단가</label>
                <input
                  id={`unitPrice-${index}`}
                  type="number"
                  min="1"
                  step="1"
                  required
                  value={item.unitPrice}
                  onChange={(event) =>
                    handleItemChange(index, "unitPrice", event.target.value)
                  }
                />

                {unitPriceError && (
                  <p className="field-error">{unitPriceError}</p>
                )}
              </div>

              {items.length > 1 && (
                <button type="button" onClick={() => handleRemoveItem(index)}>
                  품목 삭제
                </button>
              )}
            </div>
          );
        })}

        <button type="button" onClick={handleAddItem}>
          품목 추가
        </button>

        <button type="submit" disabled={createLoading}>
          {createLoading ? "등록 중..." : "판매주문 등록"}
        </button>
      </form>
    </div>
  );
}

export default SalesOrderCreateForm;
