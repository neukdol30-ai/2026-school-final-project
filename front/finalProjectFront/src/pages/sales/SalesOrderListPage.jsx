import { useEffect, useState } from "react";
import SalesOrderCreateForm from "./components/SalesOrderCreateForm";
import SalesOrderTable from "./components/SalesOrderTable";
import SalesOrderDetail from "./components/SalesOrderDetail";
import {
  confirmSalesOrder,
  createSalesOrder,
  getSalesOrderDetail,
  getSalesOrders,
} from "./js/salesOrderApi";
import "./css/SalesOrder.css";
import SalesOrderSearchFilter from "./components/SalesOrderSearchFilter";

function SalesOrderListPage() {
  const [saleOrders, setSalesOrders] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [keyword, setKeyword] = useState("");
  const [orderStatus, setOrderStatus] = useState("");

  const [successMessage, setSuccessMessage] = useState("");

  const [createValidationErrors, setCreateValidationErrors] = useState([]);

  const [createLoading, setCreateLoading] = useState(false);

  const [confirmingSalesOrderId, setConfirmingSalesOrderId] = useState(null);

  const [salesOrderDetail, setSalesOrderDetail] = useState(null);

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
    setSuccessMessage("");
    setCreateValidationErrors([]);
    setCreateLoading(true);

    try {
      const createdSalesOrder = await createSalesOrder(requestData);

      setSalesOrders((currentSalesOrders) => [
        ...currentSalesOrders,
        createdSalesOrder,
      ]);

      setSuccessMessage("판매주문이 등록되었습니다.");

      return true;
    } catch (error) {
      const validationErrors = error.validationErrors || [];

      setCreateValidationErrors(validationErrors);

      if (validationErrors.length === 0) {
        setError(error.message);
      }

      return false;
    } finally {
      setCreateLoading(false);
    }
  }

  async function handleConfirmSalesOrder(salesOrderId) {
    setError("");
    setSuccessMessage("");
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

      setSuccessMessage("판매주문이 확정되었습니다.");
    } catch (error) {
      setError(error.message);
    } finally {
      setConfirmingSalesOrderId(null);
    }
  }

  async function handleSelectSalesOrder(salesOrderId) {
    setError("");

    try {
      const selectedSalesOrderDetail = await getSalesOrderDetail(salesOrderId);

      setSalesOrderDetail(selectedSalesOrderDetail);
    } catch (error) {
      setError(error.message);
    }
  }

  const filteredSalesOrders = saleOrders.filter((salesOrder) => {
    const normalizedKeyword = keyword.trim().toLowerCase();

    const matchesKeyword =
      normalizedKeyword === "" ||
      salesOrder.orderNo.toLowerCase().includes(normalizedKeyword) ||
      salesOrder.customerName.toLowerCase().includes(normalizedKeyword);

    const matchesOrderStatus =
      orderStatus === "" || salesOrder.orderStatus === orderStatus;

    return matchesKeyword && matchesOrderStatus;
  });

  const hasSearchCondition = keyword.trim() !== "" || orderStatus !== "";

  return (
    <section className="page sales-order-page">
      <div className="page-header">
        <div>
          <h1>판매주문</h1>
          <p>고객의 식자재 주문을 조회하고 등록·확정합니다.</p>
        </div>
      </div>

      {successMessage && <p className="success-message">{successMessage}</p>}

      {salesOrderDetail && (
        <SalesOrderDetail
          salesOrderDetail={salesOrderDetail}
          onClose={() => setSalesOrderDetail(null)}
        />
      )}

      <SalesOrderCreateForm
        onCreate={handleCreateSalesOrder}
        createLoading={createLoading}
        validationErrors={createValidationErrors}
      />

      <SalesOrderSearchFilter
        keyword={keyword}
        onKeywordChange={setKeyword}
        orderStatus={orderStatus}
        onOrderStatusChange={setOrderStatus}
      />

      {loading && (
        <p className="empty-message">판매주문을 불러오는 중입니다.</p>
      )}

      {error && <p className="empty-message">{error}</p>}

      {!loading && (
        <SalesOrderTable
          saleOrders={filteredSalesOrders}
          onConfirm={handleConfirmSalesOrder}
          onSelect={handleSelectSalesOrder}
          confirmingSalesOrderId={confirmingSalesOrderId}
          hasSearchCondition={hasSearchCondition}
        />
      )}
    </section>
  );
}

export default SalesOrderListPage;
