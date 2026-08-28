function DashboardPage() {
    return (
        <section className="page">
            <div className="page-header">
                <div>
                    <h1>Dashboard</h1>
                    <p>오늘의 물류 · 재고 현황을 확인합니다.</p>
                </div>
            </div>

            <div className="dashboard-summary">
                <div className="summary-card">
                    <span>승인 대기 발주</span>
                    <strong>0</strong>
                </div>

                <div className="summary-card">
                    <span>오늘 입고</span>
                    <strong>0</strong>
                </div>

                <div className="summary-card">
                    <span>오늘 출고</span>
                    <strong>0</strong>
                </div>

                <div className="summary-card">
                    <span>재고 확인 필요</span>
                    <strong>0</strong>
                </div>
            </div>

            <div className="content-panel">
                <h2>업무 현황</h2>
                <p className="empty-message">
                    Backend API 연결 후 실제 업무 현황을 표시합니다.
                </p>
            </div>
        </section>
    );
}

export default DashboardPage;