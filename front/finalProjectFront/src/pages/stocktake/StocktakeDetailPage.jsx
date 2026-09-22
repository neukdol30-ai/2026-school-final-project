import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  confirmStocktake,
  deleteStocktake,
  getStocktakeDetail,
} from "./js/stocktakeApi";
import "./css/StocktakeDetailPage.css";

const STATUS_LABELS = {
  DRAFT: "작성중",
  CONFIRMED: "확정",
  CANCELLED: "취소",
};

function formatQuantity(value) {
  const numericValue = Number(value);

  if (Number.isNaN(numericValue)) {
    return "-";
  }

  return numericValue.toLocaleString("ko-KR", {
    maximumFractionDigits: 3,
  });
}

function StocktakeDetailPage() {
  const { stocktakeId } = useParams();
  const navigate = useNavigate();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [confirming, setConfirming] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    loadStocktakeDetail();
  }, [stocktakeId]);

  async function loadStocktakeDetail() {
    try {
      setLoading(true);
      setError("");

      const result = await getStocktakeDetail(stocktakeId);

      setDetail(result);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleConfirm() {
    const isConfirmed = window.confirm(
      "재고실사를 확정하면 차이 수량이 실제 재고에 반영되고 되돌릴 수 없습니다. 확정할까요?",
    );

    if (!isConfirmed) {
      return;
    }

    try {
      setConfirming(true);
      setError("");

      const confirmedDetail = await confirmStocktake(stocktakeId);

      setDetail(confirmedDetail);
      window.alert("재고실사가 확정되어 실제 재고와 재고 이력에 반영되었습니다.");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setConfirming(false);
    }
  }

  async function handleDelete() {
    const shouldDelete = window.confirm(
      "작성중인 재고실사를 삭제할까요? 아직 재고에는 반영되지 않았습니다.",
    );

    if (!shouldDelete) {
      return;
    }

    try {
      setDeleting(true);
      setError("");

      await deleteStocktake(stocktakeId);

      navigate("/stocktakes", {
        replace: true,
      });
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setDeleting(false);
    }
  }

  return (
    <section className="page stocktake-detail-page">
      <div className="stocktake-detail-page-header">
        <div>
          <h1>재고실사 상세</h1>
          <p>실사 문서의 기본 정보와 시스템 재고·실사 수량 차이를 확인합니다.</p>
        </div>

        <div className="stocktake-detail-actions">
          {detail?.status === "DRAFT" && (
            <>
              <Link
                className="stocktake-new-link"
                to={`/stocktakes/${stocktakeId}/edit`}
              >
                수정
              </Link>
              <button
                type="button"
                className="stocktake-delete-button"
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? "삭제 중..." : "삭제"}
              </button>
              <button
                type="button"
                className="stocktake-confirm-button"
                onClick={handleConfirm}
                disabled={confirming}
              >
                {confirming ? "확정 중..." : "재고실사 확정"}
              </button>
            </>
          )}
          <Link className="stocktake-new-link" to="/stocktakes/new">
            재고실사 등록
          </Link>
          <Link className="stocktake-list-link" to="/stocktakes">
            목록으로
          </Link>
        </div>
      </div>

      {error && (
        <p className="stocktake-detail-message error" role="alert">
          {error}
        </p>
      )}

      {loading && (
        <section className="stocktake-detail-panel">
          <p className="stocktake-detail-message">
            재고실사 정보를 불러오는 중입니다.
          </p>
        </section>
      )}

      {!loading && detail && (
        <>
          <section className="stocktake-detail-panel">
            <h2>실사 기본 정보</h2>

            <div className="stocktake-summary-grid">
              <div className="stocktake-summary-item">
                <span>실사번호</span>
                <strong>{detail.stocktakeNo}</strong>
              </div>
              <div className="stocktake-summary-item">
                <span>상태</span>
                <strong>
                  <span
                    className={`stocktake-status-badge ${detail.status?.toLowerCase()}`}
                  >
                    {STATUS_LABELS[detail.status] ?? detail.status}
                  </span>
                </strong>
              </div>
              <div className="stocktake-summary-item">
                <span>실사 창고</span>
                <strong>{detail.warehouseName}</strong>
              </div>
              <div className="stocktake-summary-item">
                <span>실사일</span>
                <strong>{detail.stocktakeDate}</strong>
              </div>
            </div>

            <div className="stocktake-memo-box">
              <span>메모</span>
              <p>{detail.memo || "등록된 메모가 없습니다."}</p>
            </div>
          </section>

          <section className="stocktake-detail-panel">
            <div className="stocktake-item-title">
              <h2>실사 품목</h2>
              <p>차이 수량은 실제 수량에서 시스템 수량을 뺀 값입니다.</p>
            </div>

            <div className="stocktake-detail-table-wrap">
              <table className="stocktake-detail-table">
                <thead>
                  <tr>
                    <th>순번</th>
                    <th>상품</th>
                    <th>LOT 번호</th>
                    <th>시스템 수량</th>
                    <th>실사 수량</th>
                    <th>차이 수량</th>
                    <th>차이 사유</th>
                  </tr>
                </thead>

                <tbody>
                  {detail.items?.length === 0 ? (
                    <tr>
                      <td colSpan="7" className="stocktake-detail-empty-row">
                        등록된 실사 품목이 없습니다.
                      </td>
                    </tr>
                  ) : (
                    detail.items?.map((item) => {
                      const differenceQuantity = Number(item.differenceQty);
                      const differenceClass =
                        differenceQuantity > 0
                          ? "positive"
                          : differenceQuantity < 0
                            ? "negative"
                            : "same";

                      return (
                        <tr key={item.stocktakeItemId}>
                          <td>{item.lineNo}</td>
                          <td>{item.productName}</td>
                          <td>{item.lotNo || "-"}</td>
                          <td>{formatQuantity(item.systemQty)}</td>
                          <td>{formatQuantity(item.actualQty)}</td>
                          <td className={`difference-quantity ${differenceClass}`}>
                            {differenceQuantity > 0 ? "+" : ""}
                            {formatQuantity(item.differenceQty)}
                          </td>
                          <td>{item.reason || "-"}</td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </section>
        </>
      )}
    </section>
  );
}

export default StocktakeDetailPage;
