import OutboundStatusBadge from "./OutboundStatusBadge";
import "../css/OutboundSummary.css";

function OutboundSummary({ outbound }) {
  const cancelledAt = outbound.cancelledAt
    ? outbound.cancelledAt.replace("T", " ").slice(0, 16)
    : "-";

  return (
    <section className="content-panel outbound-summary-panel">
      <h2>출고 기본 정보</h2>

      <dl className="outbound-summary-grid">
        <div>
          <dt>출고번호</dt>
          <dd>{outbound.outboundNo}</dd>
        </div>

        <div>
          <dt>상태</dt>
          <dd>
            <OutboundStatusBadge status={outbound.status} />
          </dd>
        </div>

        <div>
          <dt>판매주문번호</dt>
          <dd>{outbound.salesOrderNo}</dd>
        </div>

        <div>
          <dt>출고 창고</dt>
          <dd>{outbound.warehouseName}</dd>
        </div>

        {outbound.status === "CANCELLED" && (
          <>
            <div>
              <dt>취소 일시</dt>
              <dd>{cancelledAt}</dd>
            </div>
            <div>
              <dt>취소 사유</dt>
              <dd>{outbound.cancelReason || "-"}</dd>
            </div>
          </>
        )}
      </dl>
    </section>
  );
}

export default OutboundSummary;
