import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { confirmOutbound, getOutbounds } from "./js/outboundApi";
import "./css/OutboundListPage.css";
import OutboundStatusBadge from "./components/OutboundStatusBadge";

function getStatusLabel(status) {
  if (status === "DRAFT") {
    return "작성중";
  }

  if (status === "CONFIRMED") {
    return "확정";
  }

  return status;
}

function OutboundListPage() {
  const [outbounds, setOutbounds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [confirmingId, setConfirmingId] = useState(null);
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    loadOutbounds();
  }, []);

  async function loadOutbounds() {
    try {
      setLoading(true);
      setError("");

      const result = await getOutbounds();

      setOutbounds(result);
    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleConfirm(outboundId) {
    const shouldConfirm = window.confirm("이 출고서를 확정하시겠습니까?");

    if (!shouldConfirm) {
      return;
    }

    try {
      setConfirmingId(outboundId);
      setError("");
      setSuccessMessage("");

      await confirmOutbound(outboundId);

      setSuccessMessage("출고서가 확정되었습니다.");

      await loadOutbounds();
    } catch (error) {
      setError(error.message);
    } finally {
      setConfirmingId(null);
    }
  }

  return (
    <section className="page outbound-list-page">
      <div className="page-header outbound-list-page-header">
        <div>
          <h1>출고관리</h1>
          <p>출고서 작성 및 확정 상태를 관리합니다.</p>
        </div>

        <Link className="outbound-create-link" to="/outbounds/new">
          + 출고 등록
        </Link>
      </div>

      {error && (
        <p className="outbound-api-error" role="alert">
          {error}
        </p>
      )}

      {successMessage && (
        <p className="outbound-success-message" role="status">
          {successMessage}
        </p>
      )}

      <section className="content-panel outbound-list-panel">
        <div className="outbound-list-title-row">
          <div>
            <h2>출고 목록</h2>
            <p>최근에 등록된 출고서부터 표시합니다.</p>
          </div>

          <button
            className="outbound-refresh-button"
            type="button"
            onClick={loadOutbounds}
            disabled={loading}
          >
            새로고침
          </button>
        </div>

        {loading && (
          <p className="empty-message">출고 목록을 불러오는 중입니다.</p>
        )}

        {!loading && outbounds.length === 0 && (
          <p className="empty-message">등록된 출고서가 없습니다.</p>
        )}

        {!loading && outbounds.length > 0 && (
          <div className="outbound-table-wrap">
            <table className="outbound-table">
              <thead>
                <tr>
                  <th>출고번호</th>
                  <th>판매주문번호</th>
                  <th>출고 창고</th>
                  <th>상태</th>
                  <th>관리</th>
                </tr>
              </thead>

              <tbody>
                {outbounds.map((outbound) => (
                  <tr key={outbound.outboundId}>
                    <td>
                      <Link
                        className="outbound-number-link"
                        to={`/outbounds/${outbound.outboundId}`}
                      >
                        {outbound.outboundNo}
                      </Link>
                    </td>
                    <td>{outbound.salesOrderNo}</td>
                    <td>{outbound.warehouseName}</td>
                    <td>
                      <OutboundStatusBadge status={outbound.status} />
                    </td>
                    <td>
                      {outbound.status === "DRAFT" ? (
                        <button
                          className="outbound-confirm-button"
                          type="button"
                          onClick={() => handleConfirm(outbound.outboundId)}
                          disabled={confirmingId === outbound.outboundId}
                        >
                          {confirmingId === outbound.outboundId
                            ? "확정 중..."
                            : "확정"}
                        </button>
                      ) : (
                        "-"
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </section>
  );
}

export default OutboundListPage;
