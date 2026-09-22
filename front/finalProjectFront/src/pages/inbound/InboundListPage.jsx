import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { requestInboundPurchaseOrders } from "../../api/inboundApi.js";
import "./InboundListPage.css";

// DB의 발주 입고상태 영문 코드를 화면용 한국어로 바꾼다.
const RECEIPT_STATUS_LABELS = {
  NOT_RECEIVED: "미입고",
  PARTIAL: "부분입고",
  RECEIVED: "입고완료",
  CLOSED: "잔량마감",
};

function InboundListPage() {
  const navigate = useNavigate();
  const location = useLocation();
  // 취소·확정 화면에서 전달한 안내는 목록에서 보여주고, 방문 기록에서는 지운다.
  // 그래야 나중에 뒤로 가기로 돌아왔을 때 예전 성공 안내가 다시 나타나지 않는다.
  const [successMessage] = useState(location.state?.inboundSuccessMessage ?? "");

  const [purchaseOrders, setPurchaseOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    if (location.state?.inboundSuccessMessage) {
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.pathname, location.state, navigate]);

  useEffect(() => {
    let cancelled = false;

    async function loadInboundPurchaseOrders() {
      setLoading(true);
      setErrorMessage("");

      try {
        const data = await requestInboundPurchaseOrders();

        if (!cancelled) {
          setPurchaseOrders(data);
        }
      } catch (error) {
        if (!cancelled) {
          setPurchaseOrders([]);
          setErrorMessage(
            error instanceof Error
              ? error.message
              : "입고 대상 발주 목록을 불러오지 못했습니다.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadInboundPurchaseOrders();

    return () => {
      cancelled = true;
    };
  }, []);

  // 작성중 입고서가 있으면 이어서 작성하고, 없으면 해당 발주를 미리 선택한 신규 등록 화면으로 간다.
  function openInbound(order) {
    if (order.draftInboundId) {
      navigate(`/inbounds/${order.draftInboundId}`);
      return;
    }

    navigate(`/inbounds/new?purchaseOrderId=${order.purchaseOrderId}`);
  }

  return (
    <div className="page inbound-list-page">
      <div className="inbound-list-header">
        <div>
          <h1>입고 관리</h1>
          <p>입고할 발주의 입고서를 작성하거나 이어서 작성하세요.</p>
        </div>
      </div>

      {successMessage && (
        <div className="content-panel inbound-list-message success" role="status">
          {successMessage}
        </div>
      )}

      {loading && (
        <div className="content-panel inbound-list-message">
          입고 대상 발주를 불러오는 중입니다.
        </div>
      )}

      {errorMessage && (
        <div className="content-panel inbound-list-message error" role="alert">
          {errorMessage}
        </div>
      )}

      {!loading && !errorMessage && purchaseOrders.length === 0 && (
        <div className="content-panel inbound-list-message">
          현재 입고 처리할 수 있는 발주가 없습니다.
        </div>
      )}

      {!loading && !errorMessage && purchaseOrders.length > 0 && (
        <section className="content-panel inbound-list-panel">
          <div className="inbound-list-summary-row">
            <strong>입고 대상 발주 {purchaseOrders.length}건</strong>
          </div>

          <div className="inbound-list-table-wrap">
            <table className="inbound-list-table">
              <thead>
                <tr>
                  <th>발주번호</th>
                  <th>공급업체</th>
                  <th>입고창고</th>
                  <th>발주일</th>
                  <th>납품희망일</th>
                  <th>입고상태</th>
                  <th>진행상태</th>
                  <th>작업</th>
                </tr>
              </thead>

              <tbody>
                {purchaseOrders.map((order) => {
                  const hasDraft = Boolean(order.draftInboundId);

                  return (
                    <tr key={order.purchaseOrderId}>
                      <td>{order.orderNo}</td>
                      <td>{order.supplierName}</td>
                      <td>{order.warehouseName}</td>
                      <td>{order.orderDate}</td>
                      <td>{order.expectedDeliveryDate ?? "-"}</td>
                      <td>
                        {RECEIPT_STATUS_LABELS[order.receiptStatus] ??
                          "-"}
                      </td>
                      <td>
                        <span
                          className={
                            hasDraft
                              ? "inbound-work-status active"
                              : "inbound-work-status"
                          }
                        >
                          {hasDraft ? "작성중" : "작성 가능"}
                        </span>
                      </td>
                      <td>
                        <button
                          type="button"
                          className="inbound-list-primary-button"
                          onClick={() => openInbound(order)}
                          aria-label={`${order.orderNo} ${hasDraft ? "이어서 작성" : "입고서 작성"}`}
                        >
                          {hasDraft ? "이어서 작성" : "입고서 작성"}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </div>
  );
}

export default InboundListPage;
