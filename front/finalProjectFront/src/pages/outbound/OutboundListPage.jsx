import { Link } from "react-router-dom";

function OutboundListPage() {
  return (
    <section className="page">
      <div className="page-header">
        <div>
          <h1>출고관리</h1>
          <p>확정된 판매주문을 기준으로 출고를 관리합니다.</p>
        </div>

        <Link to="/outbounds/new">+ 출고 등록</Link>
      </div>

      <div className="content-panel">
        <h2>출고 목록</h2>

        {/* 출고 목록 조회 API는 다음 단계에서 연결한다. */}
        <p className="empty-message">등록된 출고서를 조회할 수 있습니다.</p>
      </div>
    </section>
  );
}

export default OutboundListPage;
