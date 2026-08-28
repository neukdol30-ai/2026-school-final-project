import { useEffect, useState } from "react";
import SalesOrderCreateForm from "./components/SalesOrderCreateForm";
import SalesOrderTable from "./components/SalesOrderTable";
import {
  confirmSalesOrder,
  createSalesOrder,
  getSalesOrders,
} from "./salesOrderApi";

function SalesOrderListPage() {
  const [saleOrders, setSalesOrders] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [createLoading, setCreateLoading] = useState(false);

  const [confirmingSalesOrderId, setConfirmingSalesOrderId] = useState(null);

  useEffect(() => {
    async function fetchSalesOrders() {
      try {
        const salesOrderData = await getSalesOrders();

        setSalesOrders(salesOrderData);
      } catch (error) {
        setError(error.message);
      } finally {
        setLoading(false);
      }
    }

    fetchSalesOrders();
  }, []);

  async function handleCreateSalesOrder(requestData) {
    setError("");
    setCreateLoading(true);

    try {
      const createdSalesOrder = await createSalesOrder(requestData);

      setSalesOrders((currentSalesOrders) => [
        ...currentSalesOrders,
        createdSalesOrder,
      ]);

      return true;
    } catch (error) {
      setError(error.message);

      return false;
    } finally {
      setCreateLoading(false);
    }
  }

  async function handleConfirmSalesOrder(salesOrderId) {
    setError("");
    setConfirmingSalesOrderId(salesOrderId);

    try {
      const confirmedSalesOrder = await confirmSalesOrder(salesOrderId);

      setSalesOrders((currentSalesOrders) =>
        currentSalesOrders.map((salesOrder) =>
          salesOrder.salesOrderId === salesOrderId
            ? confirmedSalesOrder
            : salesOrder,
        ),
      );
    } catch (error) {
      setError(error.message);
    } finally {
      setConfirmingSalesOrderId(null);
    }
  }

  return (
    <section className="page">
      <div className="page-header">
        <div>
          <h1>판매주문</h1>
          <p>고객의 식자재 주문을 조회하고 등록·확정합니다.</p>
        </div>
      </div>

      <SalesOrderCreateForm
        onCreate={handleCreateSalesOrder}
        createLoading={createLoading}
      />

      {loading && (
        <p className="empty-message">판매주문을 불러오는 중입니다.</p>
      )}

      {error && <p className="empty-message">{error}</p>}

      {!loading && !error && (
        <SalesOrderTable
          saleOrders={saleOrders}
          onConfirm={handleConfirmSalesOrder}
          confirmingSalesOrderId={confirmingSalesOrderId}
        />
      )}
    </section>
  );
}

export default SalesOrderListPage;
