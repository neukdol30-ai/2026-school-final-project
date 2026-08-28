import {useState} from "react";
import "./LoginPage.css"

function LoginPage(){

    const [loginId, setLoginId] = useState("");
    const [password, setPassword] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    const handleSubmit = (event) => {
        event.preventDefault();
        setErrorMessage("");

        if (!loginId.trim() || !password) {
            setErrorMessage("아이디와 비밀번호를 모두 입력해주세요.");
            return;
        }

        // 로그인 API연결할 부분
    };

    return (
        <main className="login-page">
            <section className="login-card">
                <header className="login-header">
                    <span className="login-brand">
                        Food Logistics ERP
                    </span>
                    <h1>로그인</h1>
                    <p>물류 ERP 관리 시스템</p>
                </header>

                <form className="login-form" onSubmit={handleSubmit}>
                    <div className="login-field">
                        <label htmlFor="loginId">아이디</label>
                        <input
                            id="loginId"
                            name="loginId"
                            type="text"
                            value={loginId}
                            onChange={(event) =>
                        setLoginId(event.target.value)}
                            placeholder="아이디를 입력하세요"
                            autoComplete="username"
                            autoFocus/>
                    </div>

                    <div className="login-field">
                        <label htmlFor="password">비밀번호</label>
                        <input
                            id="password"
                            name="password"
                            type="password"
                            value={password}
                            onChange={(event) =>
                        setPassword(event.target.value)}
                            placeholder="비밀번호를 입력하세요"
                            autoComplete="current-password"/>
                    </div>

                    {errorMessage && (
                        <p className="login-error" role="alert">
                            {errorMessage}
                        </p>
                    )}

                    <button className="login-button" type="submit">로그인</button>
                </form>
            </section>
        </main>

    )
}

export default LoginPage;