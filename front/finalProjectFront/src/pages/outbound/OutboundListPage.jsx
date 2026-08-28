function OutboundListPage() {
  return (
    <section className="page">
      <div className="page-header">
        <div>
          <h1>출고관리</h1>
          <p>확정된 판매주문을 기준으로 출고를 관리합니다.</p>
        </div>
      </div>

      <div className="content-panel">
        <h2>출고 목록</h2>

        {/* 출고 API 연결 전 임시 안내 문구 */}
        <p className="empty-message">현재 등록된 출고가 없습니다.</p>
      </div>
    </section>
  );
}
export default OutboundListPage;
