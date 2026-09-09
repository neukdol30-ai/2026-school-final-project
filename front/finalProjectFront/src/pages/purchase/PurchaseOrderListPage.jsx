import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { requestPurchaseOrders } from "../../api/purchaseOrderApi.js";
import "./PurchaseOrderListPage.css";

const INITIAL_FILTERS = {
  orderNo: "",
  // 공급업체의 DB 내부 숫자 ID가 아니라 사람이 보는 업체명으로 검색한다.
  supplierName: "",
  orderDateFrom: "",
  orderDateTo: "",
  approvalStatus: "",
  receiptStatus: "",
};

const APPROVAL_STATUS_LABELS = {
  DRAFT: "작성중",
  PENDING: "승인대기",
  APPROVED: "승인",
  REJECTED: "반려",
};

const RECEIPT_STATUS_LABELS = {
  NOT_RECEIVED: "미입고",
  PARTIAL: "부분입고",
  RECEIVED: "입고완료",
  CLOSED: "잔량마감",
};

function createFiltersFromSearchParams(searchParams) {
  return {
    orderNo: searchParams.get("orderNo") ?? "",
    supplierName: searchParams.get("supplierName") ?? "",
    orderDateFrom: searchParams.get("orderDateFrom") ?? "",
    orderDateTo: searchParams.get("orderDateTo") ?? "",
    approvalStatus: searchParams.get("approvalStatus") ?? "",
    receiptStatus: searchParams.get("receiptStatus") ?? "",
  };
}

function createSearchParamsFromFilters(filters) {
  const params = new URLSearchParams();

  if (filters.orderNo?.trim()) {
    params.set("orderNo", filters.orderNo.trim());
  }

  if (filters.supplierName?.trim()) {
    params.set("supplierName", filters.supplierName.trim());
  }

  if (filters.orderDateFrom) {
    params.set("orderDateFrom", filters.orderDateFrom);
  }

  if (filters.orderDateTo) {
    params.set("orderDateTo", filters.orderDateTo);
  }

  if (filters.approvalStatus) {
    params.set("approvalStatus", filters.approvalStatus);
  }

  if (filters.receiptStatus) {
    params.set("receiptStatus", filters.receiptStatus);
  }

  return params;
}

function hasSearchCondition(filters) {
  return Object.values(filters).some((value) => String(value).trim() !== "");
}

function formatAmount(amount) {
  if (amount === null || amount === undefined) {
    return "-";
  }

  const numericAmount = Number(amount);

  if (!Number.isFinite(numericAmount)) {
    return "-";
  }

  return `${numericAmount.toLocaleString("ko-KR")}원`;
}

function PurchaseOrderListPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [filters, setFilters] = useState(() =>
    createFiltersFromSearchParams(searchParams),
  );

  const [purchaseOrders, setPurchaseOrders] = useState([]);

  const [loading, setLoading] = useState(false);

  const [error, setError] = useState("");

  const currentUrlFilters = createFiltersFromSearchParams(searchParams);

  const isAllSearch = searchParams.get("searchMode") === "all";

  const hasSearchRequest = hasSearchCondition(currentUrlFilters) || isAllSearch;

  useEffect(() => {
    async function loadPurchaseOrders() {
      const currentFilters = createFiltersFromSearchParams(searchParams);
      const currentIsAllSearch = searchParams.get("searchMode") === "all";

      setFilters(currentFilters);
      setError("");

      if (!hasSearchCondition(currentFilters) && !currentIsAllSearch) {
        setPurchaseOrders([]);
        setLoading(false);
        return;
      }

      setLoading(true);

      try {
        const data = await requestPurchaseOrders(currentFilters);
        setPurchaseOrders(data);
      } catch (requestError) {
        setPurchaseOrders([]);

        setError(
          requestError instanceof Error
            ? requestError.message
            : "발주 목록을 불러오지 못했습니다.",
        );
      } finally {
        setLoading(false);
      }
    }

    loadPurchaseOrders();
  }, [searchParams]);

  function handleFilterChange(event) {
    const { name, value } = event.target;

    setFilters((previousFilters) => ({
      ...previousFilters,
      [name]: value,
    }));
  }

  function handleSearch(event) {
    event.preventDefault();

    const nextSearchParams = createSearchParamsFromFilters(filters);

    if (!hasSearchCondition(filters)) {
      nextSearchParams.set("searchMode", "all");
    }

    setSearchParams(nextSearchParams);
  }

  function handleReset() {
    setFilters(INITIAL_FILTERS);
    setPurchaseOrders([]);
    setError("");
    setLoading(false);
    setSearchParams(new URLSearchParams());
  }

  function handleDetail(purchaseOrderId) {
    const queryString = searchParams.toString();

    if (queryString) {
      navigate(`/purchase-orders/${purchaseOrderId}?${queryString}`);
      return;
    }

    navigate(`/purchase-orders/${purchaseOrderId}`);
  }

  return (
    <div className="page purchase-order-list-page">
      <h1>발주 목록</h1>

      <form className="purchase-order-search-form" onSubmit={handleSearch}>
        <div className="purchase-order-search-fields">
          <label>
            <span>발주번호</span>
            <input
              type="text"
              name="orderNo"
              value={filters.orderNo}
              onChange={handleFilterChange}
              placeholder="발주번호 입력"
            />
          </label>

          <label>
            {/* 사용자가 DB 내부 ID를 외울 필요가 없도록 공급업체명으로 검색한다. */}
            <span>공급업체명</span>

            {/* type="text"이므로 한글·영문 공급업체명을 입력할 수 있다. */}
            <input
              type="text"
              name="supplierName"
              value={filters.supplierName}
              onChange={handleFilterChange}
              placeholder="공급업체명 입력"
            />
          </label>

          <label>
            <span>발주일 시작</span>
            <input
              type="date"
              name="orderDateFrom"
              value={filters.orderDateFrom}
              onChange={handleFilterChange}
            />
          </label>

          <label>
            <span>발주일 종료</span>
            <input
              type="date"
              name="orderDateTo"
              value={filters.orderDateTo}
              onChange={handleFilterChange}
            />
          </label>

          <label>
            <span>승인상태</span>
            <select
              name="approvalStatus"
              value={filters.approvalStatus}
              onChange={handleFilterChange}
            >
              <option value="">전체</option>
              <option value="DRAFT">작성중</option>
              <option value="PENDING">승인대기</option>
              <option value="APPROVED">승인</option>
              <option value="REJECTED">반려</option>
            </select>
          </label>

          <label>
            <span>입고상태</span>
            <select
              name="receiptStatus"
              value={filters.receiptStatus}
              onChange={handleFilterChange}
            >
              <option value="">전체</option>
              <option value="NOT_RECEIVED">미입고</option>
              <option value="PARTIAL">부분입고</option>
              <option value="RECEIVED">입고완료</option>
              <option value="CLOSED">잔량마감</option>
            </select>
          </label>
        </div>

        <div className="purchase-order-search-actions">
          <button type="submit" disabled={loading}>
            {loading ? "조회 중..." : "조회"}
          </button>

          <button type="button" onClick={handleReset} disabled={loading}>
            초기화
          </button>
        </div>
      </form>

      {!loading && !error && hasSearchRequest && (
        <div className="purchase-order-result-summary">
          조회 결과 {purchaseOrders.length}건
        </div>
      )}

      {!loading && !error && !hasSearchRequest && (
        <p>검색조건을 입력하거나 조회 버튼을 눌러 전체 발주를 조회해 주세요.</p>
      )}

      {loading && <p>발주 목록을 불러오는 중입니다.</p>}

      {error && <p role="alert">{error}</p>}

      {!loading && !error && hasSearchRequest && (
        <>
          {purchaseOrders.length === 0 ? (
            <p>조회된 발주가 없습니다.</p>
          ) : (
            <table className="purchase-order-table">
              <thead>
                <tr>
                  <th>발주번호</th>
                  <th>공급업체명</th>
                  <th>창고명</th>
                  <th>발주일</th>
                  <th>납품희망일</th>
                  <th>승인상태</th>
                  <th>입고상태</th>
                  <th>총액</th>
                  <th>상세</th>
                </tr>
              </thead>

              <tbody>
                {purchaseOrders.map((order) => (
                  <tr key={order.purchaseOrderId}>
                    <td>{order.orderNo}</td>
                    <td>{order.supplierName}</td>
                    <td>{order.warehouseName}</td>
                    <td>{order.orderDate}</td>
                    <td>{order.expectedDeliveryDate ?? "-"}</td>
                    <td>
                      {APPROVAL_STATUS_LABELS[order.approvalStatus] ??
                        order.approvalStatus}
                    </td>
                    <td>
                      {RECEIPT_STATUS_LABELS[order.receiptStatus] ??
                        order.receiptStatus}
                    </td>
                    <td>{formatAmount(order.totalAmount)}</td>
                    <td>
                      <button
                        type="button"
                        className="purchase-order-detail-button"
                        onClick={() => handleDetail(order.purchaseOrderId)}
                      >
                        상세
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </div>
  );
}

export default PurchaseOrderListPage;
