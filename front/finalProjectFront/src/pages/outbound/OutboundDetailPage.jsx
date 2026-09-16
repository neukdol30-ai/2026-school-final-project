import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  cancelOutbound,
  confirmOutbound,
  deleteOutbound,
  getOutboundDetail,
} from "./js/outboundApi";
import "./css/OutboundDetailPage.css";
import OutboundConfirmButton from "./components/OutboundConfirmButton";
import OutboundSummary from "./components/OutboundSummary";
import OutboundItemTable from "./components/OutboundItemTable";

function OutboundDetailPage() {
  const { outboundId } = useParams();
  const navigate = useNavigate();

  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [confirming, setConfirming] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [deleting, setDeleting] = useState(false);
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

  async function handleCancel() {
    const cancelReason = window.prompt("출고 취소 사유를 입력해 주세요.");

    if (cancelReason === null) {
      return;
    }

    if (!cancelReason.trim()) {
      setError("출고 취소 사유를 입력해 주세요.");
      return;
    }

    const shouldCancel = window.confirm(
      "출고를 취소하면 판매주문 출고수량과 LOT·전체 재고가 원복됩니다. 취소할까요?",
    );

    if (!shouldCancel) {
      return;
    }

    try {
      setCancelling(true);
      setError("");
      setSuccessMessage("");

      await cancelOutbound(outboundId, cancelReason.trim());

      setSuccessMessage("출고서가 취소되어 판매주문과 재고가 원복되었습니다.");

      await loadOutboundDetail();
    } catch (error) {
      setError(error.message);
    } finally {
      setCancelling(false);
    }
  }

  async function handleDelete() {
    const shouldDelete = window.confirm(
      "작성중인 출고서를 삭제할까요? 아직 재고에는 반영되지 않았습니다.",
    );

    if (!shouldDelete) {
      return;
    }

    try {
      setDeleting(true);
      setError("");
      setSuccessMessage("");

      await deleteOutbound(outboundId);

      navigate("/outbounds", { replace: true });
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setDeleting(false);
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
            <>
              <Link
                className="outbound-edit-link"
                to={`/outbounds/${outboundId}/edit`}
              >
                수정
              </Link>
              <button
                type="button"
                className="outbound-delete-button"
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? "삭제 중..." : "삭제"}
              </button>
              <OutboundConfirmButton
                onConfirm={handleConfirm}
                confirming={confirming}
              />
            </>
          )}

          {detail?.outbound.status === "CONFIRMED" && (
            <button
              type="button"
              className="outbound-cancel-button"
              onClick={handleCancel}
              disabled={cancelling}
            >
              {cancelling ? "취소 중..." : "출고 취소"}
            </button>
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
