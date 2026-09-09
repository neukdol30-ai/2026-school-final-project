import { Link } from "react-router-dom";
import OutboundStatusBadge from "./OutboundStatusBadge";
import "../css/OutboundCreateResult.css";

function OutboundCreateResult({ outbound }) {
  return (
    <div className="content-panel outbound-result-panel">
      <h2>등록 결과</h2>

      <p>
        <span>출고번호</span>
        <strong>{outbound.outboundNo}</strong>
      </p>

      <p>
        <span>상태</span>
        <OutboundStatusBadge status={outbound.status} />
      </p>

      <div className="outbound-result-actions">
        <Link
          className="outbound-detail-link"
          to={`/outbounds/${outbound.outboundId}`}
        >
          등록한 출고서 상세 보기
        </Link>
      </div>
    </div>
  );
}

export default OutboundCreateResult;
