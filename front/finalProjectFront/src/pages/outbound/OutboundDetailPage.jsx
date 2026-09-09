import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { confirmOutbound, getOutboundDetail } from "./js/outboundApi";
import "./css/OutboundDetailPage.css";
import OutboundConfirmButton from "./components/OutboundConfirmButton";
import OutboundSummary from "./components/OutboundSummary";
import OutboundItemTable from "./components/OutboundItemTable";

function OutboundDetailPage() {
  const { outboundId } = useParams();

  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [confirming, setConfirming] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    loadOutboundDetail();
  }, [outboundId]);

  async function handleConfirm() {
    const shouldConfirm = window.confirm("이 출고서를 확정하시겠습니까?");

    if (!shouldConfirm) {
      return;
    }

    try {
      setConfirming(true);
      setError("");
      setSuccessMessage("");

      await confirmOutbound(outboundId);

      setSuccessMessage("출고서가 확정되었습니다.");

      await loadOutboundDetail();
    } catch (error) {
      setError(error.message);
    } finally {
      setConfirming(false);
    }
  }

  async function loadOutboundDetail() {
    try {
      setLoading(true);
      setError("");

      const result = await getOutboundDetail(outboundId);

      setDetail(result);
    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="page outbound-detail-page">
      <div className="page-header outbound-detail-page-header">
        <div>
          <h1>출고서 상세</h1>
          <p>출고서 기본 정보와 출고 품목을 확인합니다.</p>
        </div>

        <div className="outbound-detail-actions">
          {detail?.outbound.status === "DRAFT" && (
            <OutboundConfirmButton
              onConfirm={handleConfirm}
              confirming={confirming}
            />
          )}

          <Link className="outbound-list-link" to="/outbounds">
            목록으로
          </Link>
        </div>
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

      {loading && (
        <section className="content-panel">
          <p className="empty-message">출고서 정보를 불러오는 중입니다.</p>
        </section>
      )}

      {!loading && detail && (
        <>
          <OutboundSummary outbound={detail.outbound} />

          <OutboundItemTable items={detail.items} />
        </>
      )}
    </section>
  );
}

export default OutboundDetailPage;
