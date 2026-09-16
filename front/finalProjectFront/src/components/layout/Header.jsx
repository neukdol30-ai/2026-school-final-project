import { FiLogOut } from "react-icons/fi";
import { useNavigate } from "react-router-dom";
import {
    clearAuthSession,
} from "../../storage/authStorage.js";

function Header() {
    const navigate = useNavigate();

    function handleLogout() {
        clearAuthSession();

        navigate("/login", {
            replace: true,
        });
    }

    return (
        <header className="header">
            <div className="header-left">
        <span className="header-title">
          물류 ERP 관리시스템
        </span>
            </div>

            <div className="header-right">
        <span className="header-company">
          Demo 식자재유통
        </span>

                <span className="header-user">
          관리자
        </span>

                <button
                    type="button"
                    className="header-logout-button"
                    onClick={handleLogout}
                    title="로그아웃"
                >
                    <FiLogOut />
                    <span>로그아웃</span>
                </button>
            </div>
        </header>
    );
}

export default Header;