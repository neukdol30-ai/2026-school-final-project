import "./ManagementAccessDenied.css";

function ManagementAccessDenied({ resourceName }) {
    return (
        <section
            className="management-access-denied"
            role="alert"
        >
            <div
                className="management-access-denied-icon"
                aria-hidden="true"
            >
                !
            </div>
            <h1>권한 없음</h1>
            <p>
                {resourceName} 정보를 조회할 권한이 없습니다.
                관리자에게 권한을 요청해주세요.
            </p>
        </section>
    );
}

export default ManagementAccessDenied;
