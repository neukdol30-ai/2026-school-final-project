import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import SalesOrderTable from "./components/SalesOrderTable";
import SalesOrderDetail from "./components/SalesOrderDetail";
import {
  confirmSalesOrder,
  getSalesOrderDetail,
  getSalesOrders,
} from "./js/salesOrderApi";
import "./css/SalesOrderCommon.css";
import "./css/SalesOrderList.css";
import "./css/SalesOrderDetail.css";
import SalesOrderSearchFilter from "./components/SalesOrderSearchFilter";

function SalesOrderListPage() {
  const location = useLocation();
  const navigate = useNavigate();

  const [saleOrders, setSalesOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [keyword, setKeyword] = useState("");
  const [orderStatus, setOrderStatus] = useState("");
  const [appliedKeyword, setAppliedKeyword] = useState("");
  const [appliedOrderStatus, setAppliedOrderStatus] = useState("");

  const [successMessage, setSuccessMessage] = useState("");
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

  useEffect(() => {
    if (!location.state?.successMessage) {
      return;
    }

    setSuccessMessage(location.state.successMessage);

    navigate("/sales-orders", {
      replace: true,
      state: null,
    });
  }, [location.state, navigate]);

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

      setSalesOrderDetail((currentSalesOrderDetail) =>
        currentSalesOrderDetail?.salesOrderId === salesOrderId
          ? {
              ...currentSalesOrderDetail,
              orderStatus: confirmedSalesOrder.orderStatus,
              shipmentStatus: confirmedSalesOrder.shipmentStatus,
            }
          : currentSalesOrderDetail,
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

  function handleSearchSalesOrder() {
    setAppliedKeyword(keyword);
    setAppliedOrderStatus(orderStatus);
  }

  function handleResetSalesOrderSearch() {
    setKeyword("");
    setOrderStatus("");
    setAppliedKeyword("");
    setAppliedOrderStatus("");
  }

  const filteredSalesOrders = saleOrders.filter((salesOrder) => {
    const normalizedKeyword = appliedKeyword.trim().toLowerCase();

    const matchesKeyword =
      normalizedKeyword === "" ||
      salesOrder.orderNo.toLowerCase().includes(normalizedKeyword) ||
      salesOrder.customerName.toLowerCase().includes(normalizedKeyword);

    const matchesOrderStatus =
      appliedOrderStatus === "" ||
      salesOrder.orderStatus === appliedOrderStatus;

    return matchesKeyword && matchesOrderStatus;
  });

  const hasSearchCondition =
    appliedKeyword.trim() !== "" || appliedOrderStatus !== "";

  return (
    <section className="page sales-order-page">
      <div className="page-header sales-order-page-header">
        <div>
          <h1>판매주문</h1>
          <p>고객의 식자재 주문을 조회하고 확정합니다.</p>
        </div>

        <Link className="sales-order-create-link" to="/sales-orders/new">
          + 판매주문 등록
        </Link>
      </div>

      {successMessage && <p className="success-message">{successMessage}</p>}

      {error && (
        <p className="sales-order-api-error" role="alert">
          {error}
        </p>
      )}

      {salesOrderDetail && (
        <SalesOrderDetail
          salesOrderDetail={salesOrderDetail}
          onClose={() => setSalesOrderDetail(null)}
        />
      )}

      <SalesOrderSearchFilter
        keyword={keyword}
        onKeywordChange={setKeyword}
        orderStatus={orderStatus}
        onOrderStatusChange={setOrderStatus}
        onSearch={handleSearchSalesOrder}
        onReset={handleResetSalesOrderSearch}
      />

      {loading && (
        <p className="empty-message">판매주문을 불러오는 중입니다.</p>
      )}

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
