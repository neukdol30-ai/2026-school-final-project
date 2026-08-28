import { useState } from "react";

function SalesOrderCreateForm({ onCreate, createLoading }) {
  const [customerId, setCustomerId] = useState("");
  const [productUnitId, setProductUnitId] = useState("");
  const [orderedQty, setOrderedQty] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();

    const created = await onCreate({
      customerId: Number(customerId),
      items: [
        {
          productUnitId: Number(productUnitId),
          orderedQty: Number(orderedQty),
        },
      ],
    });

    if (created) {
      setCustomerId("");
      setProductUnitId("");
      setOrderedQty("");
    }
  }
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
        </div>

        <div>
          <label htmlFor="productUnitId">상품 단위 ID</label>
          <input
            id="productUnitId"
            type="number"
            min="1"
            required
            value={productUnitId}
            onChange={(event) => setProductUnitId(event.target.value)}
          />
        </div>

        <div>
          <label htmlFor="orderedQty">주문 수량</label>
          <input
            id="orderedQty"
            type="number"
            min="0.001"
            step="0.001"
            required
            value={orderedQty}
            onChange={(event) => setOrderedQty(event.target.value)}
          />
        </div>

        <button type="submit" disabled={createLoading}>
          {createLoading ? "등록 중..." : "판매주문 등록"}
        </button>
      </form>
    </div>
  );
}

export default SalesOrderCreateForm;
