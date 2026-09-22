import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import {
  requestApprovePurchaseOrder,
  requestPurchaseOrderApproval,
  requestPurchaseOrderDetail,
  requestRejectPurchaseOrder,
} from "../../api/purchaseOrderApi.js";
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
  const [actionError, setActionError] = useState("");
  const [busyAction, setBusyAction] = useState("");
  const [needsRefresh, setNeedsRefresh] = useState(false);
  const [rejectOpen, setRejectOpen] = useState(false);
  const [rejectionReason, setRejectionReason] = useState("");
  const actionInProgress = useRef(false);

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

  async function refreshDetail() {
    const data = await requestPurchaseOrderDetail(purchaseOrderId);
    setPurchaseOrder(data);
    setNeedsRefresh(false);
    setActionError("");
  }

  async function handleAction(action, request) {
    if (actionInProgress.current || needsRefresh) {
      return;
    }

    actionInProgress.current = true;
    setBusyAction(action);
    setActionError("");

    try {
      await request();
      setRejectOpen(false);
      setRejectionReason("");

      try {
        await refreshDetail();
      } catch (refreshError) {
        setNeedsRefresh(true);
        setActionError(
          `처리 후 상세정보를 다시 조회하지 못했습니다. 다시 조회해 주세요. ${refreshError.message}`,
        );
      }
    } catch (requestError) {
      setActionError(requestError.message);
    } finally {
      actionInProgress.current = false;
      setBusyAction("");
    }
  }

  async function handleRetryRefresh() {
    if (actionInProgress.current) {
      return;
    }

    actionInProgress.current = true;
    setBusyAction("refresh");

    try {
      await refreshDetail();
    } catch (refreshError) {
      setActionError(refreshError.message);
    } finally {
      actionInProgress.current = false;
      setBusyAction("");
    }
  }

  function handleReject(event) {
    event.preventDefault();
    const trimmedReason = rejectionReason.trim();

    if (!trimmedReason || trimmedReason.length > 500) {
      setActionError("반려사유는 공백을 제외하고 1자 이상, 500자 이하로 입력해 주세요.");
      return;
    }

    handleAction("reject", () =>
      requestRejectPurchaseOrder(purchaseOrderId, trimmedReason),
    );
  }

  const items = purchaseOrder?.items ?? [];
  const approvalStatus = purchaseOrder?.approvalStatus;
  const canEdit = ["DRAFT", "REJECTED"].includes(approvalStatus)
    && purchaseOrder?.receiptStatus === "NOT_RECEIVED"
    && !items.some((item) => item.receivedQty != null && Number(item.receivedQty) > 0);
  const actionsDisabled = Boolean(busyAction) || needsRefresh;

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

      {actionError && (
        <div className="content-panel purchase-order-detail-message error" role="alert">
          {actionError}
          {needsRefresh && (
            <button type="button" onClick={handleRetryRefresh} disabled={Boolean(busyAction)}>
              {busyAction === "refresh" ? "조회 중..." : "다시 조회"}
            </button>
          )}
        </div>
      )}

      {!loading && !error && purchaseOrder && (
        <>
          <div className="purchase-order-detail-actions">
            {canEdit && (
              <button
                type="button"
                disabled={actionsDisabled}
                onClick={() => navigate(`/purchase-orders/${purchaseOrderId}/edit${location.search}`)}
              >
                발주 수정
              </button>
            )}
            {approvalStatus === "DRAFT" && (
              <button
                type="button"
                disabled={actionsDisabled}
                onClick={() => handleAction("request", () => requestPurchaseOrderApproval(purchaseOrderId))}
              >
                {busyAction === "request" ? "요청 중..." : "승인 요청"}
              </button>
            )}
            {approvalStatus === "PENDING" && (
              <>
                <button
                  type="button"
                  disabled={actionsDisabled}
                  onClick={() => handleAction("approve", () => requestApprovePurchaseOrder(purchaseOrderId))}
                >
                  {busyAction === "approve" ? "승인 중..." : "승인"}
                </button>
                <button
                  type="button"
                  className="purchase-order-reject-button"
                  disabled={actionsDisabled}
                  onClick={() => {
                    setActionError("");
                    setRejectOpen(true);
                  }}
                >
                  반려
                </button>
              </>
            )}
          </div>

          {approvalStatus === "REJECTED" && (
            <section className="content-panel purchase-order-detail-section">
              <h2>반려 정보</h2>
              <div className="purchase-order-note-grid">
                <div>
                  <span>반려사유</span>
                  <p>{purchaseOrder.rejectionReason || "-"}</p>
                </div>
                <div>
                  <span>반려일시</span>
                  <p>{formatDateTime(purchaseOrder.rejectedAt)}</p>
                </div>
              </div>
            </section>
          )}

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

          {rejectOpen && approvalStatus === "PENDING" && (
            <div className="purchase-order-reject-overlay">
              <form
                className="purchase-order-reject-dialog"
                role="dialog"
                aria-modal="true"
                aria-label="발주 반려"
                onSubmit={handleReject}
              >
                <h2>발주 반려</h2>
                <label htmlFor="purchase-rejection-reason">반려사유</label>
                <textarea
                  id="purchase-rejection-reason"
                  autoFocus
                  maxLength={500}
                  value={rejectionReason}
                  onChange={(event) => {
                    setRejectionReason(event.target.value);
                    setActionError("");
                  }}
                  rows={5}
                />
                <p>{rejectionReason.length}/500자</p>
                {actionError && <p role="alert" className="purchase-order-reject-error">{actionError}</p>}
                <div>
                  <button
                    type="button"
                    disabled={Boolean(busyAction)}
                    onClick={() => {
                      setRejectOpen(false);
                      setRejectionReason("");
                      setActionError("");
                    }}
                  >
                    취소
                  </button>
                  <button type="submit" disabled={Boolean(busyAction)}>
                    {busyAction === "reject" ? "반려 중..." : "반려 확정"}
                  </button>
                </div>
              </form>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default PurchaseOrderDetailPage;
