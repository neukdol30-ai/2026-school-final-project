import { useEffect, useState } from "react";

function SalesOrderListPage() {
  const [saleOrders, setSalesOrders] = useState([]);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  const [customerId, setCustomerId] = useState("");
  const [productUnitId, setProductUnitId] = useState("");
  const [orderedQty, setOrderedQty] = useState("");

  const [createLoading, setCreateLoading] = useState(false);

  useEffect(() => {
    async function fetchSalesOrders() {
      try {
        const response = await fetch("http://localhost:8080/api/sales-orders");

        if (!response.ok) {
          throw new Error("판매주문 목록을 불러오지 못했습니다.");
        }

        const result = await response.json();

        if (!result.success) {
          throw new Error(
            result.error?.message || "판매주문 목록을 불러오지 못했습니다.",
          );
        }

        setSalesOrders(result.data);
      } catch (error) {
        setError(error.message);
      } finally {
        setLoading(false);
      }
    }

    fetchSalesOrders();
  }, []);

  async function handleCreateSalesOrder(event) {
    event.preventDefault();

    setError("");

    setCreateLoading(true);

    try {
      const requestData = {
        customerId: Number(customerId),

        items: [
          {
            productUnitId: Number(productUnitId),
            orderedQty: Number(orderedQty),
          },
        ],
      };

      const response = await fetch("http://localhost:8080/api/sales-orders", {
        method: "POST",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify(requestData),
      });

      if (!response.ok) {
        throw new Error("판매주문 등록에 실패했습니다.");
      }

      const result = await response.json();

      if (!result.success) {
        throw new Error(
          result.error?.message || "판매주문 등록에 실패했습니다.",
        );
      }

      setSalesOrders((currentSalesOrders) => [
        ...currentSalesOrders,
        result.data,
      ]);

      setCustomerId("");
      setProductUnitId("");
      setOrderedQty("");
    } catch (error) {
      setError(error.message);
    } finally {
      setCreateLoading(false);
    }
  }

  return (
    <section className="page">
      <div className="page-header">
        <div>
          <h1>판매주문</h1>
          <p>고객의 식자재 주문을 조회하고 등록합니다.</p>
        </div>
      </div>

      <div className="content-panel">
        <h2>판매주문 등록</h2>

        <form onSubmit={handleCreateSalesOrder}>
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

      <div className="content-panel">
        <h2>판매주문 목록</h2>

        {loading && (
          <p className="empty-message">판매주문을 불러오는 중입니다.</p>
        )}

        {error && <p className="empty-message">{error}</p>}

        {!loading && !error && (
          <table>
            <thead>
              <tr>
                <th>주문번호</th>
                <th>거래처</th>
                <th>주문상태</th>
                <th>출고상태</th>
              </tr>
            </thead>

            <tbody>
              {saleOrders.map((salesOrder) => (
                <tr key={salesOrder.salesOrderId}>
                  <td>{salesOrder.orderNo}</td>
                  <td>{salesOrder.customerName}</td>
                  <td>{salesOrder.orderStatus}</td>
                  <td>{salesOrder.shipmentStatus}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </section>
  );
}

export default SalesOrderListPage;
