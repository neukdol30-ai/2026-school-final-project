function EmptyPage({ title }) {
    return (
        <section className="page">
            <div className="page-header">
                <h1>{title}</h1>
            </div>

            <div className="content-panel">
                <p className="empty-message">화면 구현 예정</p>
            </div>
        </section>
    );
}

export default EmptyPage;