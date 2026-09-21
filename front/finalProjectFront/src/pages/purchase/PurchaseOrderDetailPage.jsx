import { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { requestPurchaseOrderDetail } from "../../api/purchaseOrderApi.js";
import "./PurchaseOrderDetailPage.css";

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

const TAX_TYPE_LABELS = {
  TAXABLE: "과세",
  TAX_FREE: "면세",
};

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

function formatQuantity(quantity) {
  if (quantity === null || quantity === undefined) {
    return "-";
  }

  const numericQuantity = Number(quantity);

  if (!Number.isFinite(numericQuantity)) {
    return "-";
  }

  return numericQuantity.toLocaleString("ko-KR", {
    maximumFractionDigits: 3,
  });
}

function formatDateTime(dateTime) {
  if (!dateTime) {
    return "-";
  }

  return String(dateTime).replace("T", " ").slice(0, 16);
}

function PurchaseOrderDetailPage() {
  const { purchaseOrderId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [purchaseOrder, setPurchaseOrder] = useState(null);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  useEffect(() => {
    async function loadPurchaseOrderDetail() {
      setError("");
      setLoading(true);

      try {
        const data = await requestPurchaseOrderDetail(purchaseOrderId);
        setPurchaseOrder(data);
      } catch (requestError) {
        setPurchaseOrder(null);

        setError(
          requestError instanceof Error
            ? requestError.message
            : "발주 상세정보를 불러오지 못했습니다.",
        );
      } finally {
        setLoading(false);
      }
    }

    loadPurchaseOrderDetail();
  }, [purchaseOrderId]);

  function handleBackToList() {
    navigate(`/purchase-orders${location.search}`);
  }

  const items = purchaseOrder?.items ?? [];

  return (
    <div className="page purchase-order-detail-page">
      <div className="purchase-order-detail-header">
        <div>
          <h1>발주 상세</h1>

          <p>발주 ID: {purchaseOrderId}</p>
        </div>

        <button
          type="button"
          className="purchase-order-back-button"
          onClick={handleBackToList}
        >
          목록으로
        </button>
      </div>

      {loading && (
        <div className="content-panel purchase-order-detail-message">
          발주 상세정보를 불러오는 중입니다.
        </div>
      )}

      {error && (
        <div
          className="content-panel purchase-order-detail-message error"
          role="alert"
        >
          {error}
        </div>
      )}

      {!loading && !error && purchaseOrder && (
        <>
          <section className="content-panel purchase-order-detail-section">
            <h2>기본 정보</h2>

            <div className="purchase-order-detail-grid">
              <div>
                <span>발주번호</span>
                <strong>{purchaseOrder.orderNo}</strong>
              </div>

              <div>
                <span>공급업체</span>
                <strong>{purchaseOrder.supplierName}</strong>
              </div>

              <div>
                <span>입고창고</span>
                <strong>{purchaseOrder.warehouseName}</strong>
              </div>

              <div>
                <span>발주일</span>
                <strong>{purchaseOrder.orderDate}</strong>
              </div>

              <div>
                <span>납품희망일</span>
                <strong>{purchaseOrder.expectedDeliveryDate ?? "-"}</strong>
              </div>

              <div>
                <span>승인상태</span>
                <strong>
                  {APPROVAL_STATUS_LABELS[purchaseOrder.approvalStatus] ??
                    purchaseOrder.approvalStatus}
                </strong>
              </div>

              <div>
                <span>입고상태</span>
                <strong>
                  {RECEIPT_STATUS_LABELS[purchaseOrder.receiptStatus] ??
                    purchaseOrder.receiptStatus}
                </strong>
              </div>
            </div>
          </section>

          <section className="content-panel purchase-order-detail-section">
            <h2>요청사항 / 내부메모</h2>

            <div className="purchase-order-note-grid">
              <div>
                <span>공급업체 요청사항</span>
                <p>{purchaseOrder.requestNote || "-"}</p>
              </div>

              <div>
                <span>내부 메모</span>
                <p>{purchaseOrder.internalMemo || "-"}</p>
              </div>
            </div>
          </section>

          <section className="content-panel purchase-order-detail-section">
            <h2>금액 정보</h2>

            <div className="purchase-order-amount-grid">
              <div>
                <span>공급가액</span>
                <strong>{formatAmount(purchaseOrder.totalSupplyAmount)}</strong>
              </div>

              <div>
                <span>세액</span>
                <strong>{formatAmount(purchaseOrder.totalTaxAmount)}</strong>
              </div>

              <div>
                <span>총 금액</span>
                <strong>{formatAmount(purchaseOrder.totalAmount)}</strong>
              </div>
            </div>
          </section>

          <section className="content-panel purchase-order-detail-section">
            <h2>발주 품목</h2>

            <div className="purchase-order-detail-table-wrap">
              <table className="purchase-order-detail-table">
                <thead>
                  <tr>
                    <th>상품코드</th>
                    <th>상품명</th>
                    <th>단위</th>
                    <th>발주수량</th>
                    <th>환산수량</th>
                    <th>기준수량</th>
                    <th>입고수량</th>
                    <th>단가</th>
                    <th>과세</th>
                    <th>공급가액</th>
                    <th>세액</th>
                    <th>합계</th>
                  </tr>
                </thead>

                <tbody>
                  {items.length === 0 ? (
                    <tr>
                      <td colSpan="12" className="purchase-order-empty-items">
                        등록된 발주 품목이 없습니다.
                      </td>
                    </tr>
                  ) : (
                    items.map((item) => (
                      <tr key={item.purchaseOrderItemId}>
                        <td>{item.productCode}</td>
                        <td>{item.productName}</td>
                        <td>
                          {item.unitName} ({item.unitCode})
                        </td>
                        <td>{formatQuantity(item.orderedQty)}</td>
                        <td>{formatQuantity(item.conversionQty)}</td>
                        <td>{formatQuantity(item.baseOrderedQty)}</td>
                        <td>{formatQuantity(item.receivedQty)}</td>
                        <td>{formatAmount(item.unitPrice)}</td>
                        <td>{TAX_TYPE_LABELS[item.taxType] ?? item.taxType}</td>
                        <td>{formatAmount(item.supplyAmount)}</td>
                        <td>{formatAmount(item.taxAmount)}</td>
                        <td>{formatAmount(item.totalAmount)}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </section>

          <section className="content-panel purchase-order-detail-section">
            <h2>처리 정보</h2>

            <div className="purchase-order-detail-grid">
              <div>
                <span>등록일시</span>
                <strong>{formatDateTime(purchaseOrder.createdAt)}</strong>
              </div>

              <div>
                <span>수정일시</span>
                <strong>{formatDateTime(purchaseOrder.updatedAt)}</strong>
              </div>

              <div>
                <span>승인일시</span>
                <strong>{formatDateTime(purchaseOrder.approvedAt)}</strong>
              </div>
            </div>
          </section>
        </>
      )}
    </div>
  );
}

export default PurchaseOrderDetailPage;
