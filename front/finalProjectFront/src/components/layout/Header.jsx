import { Link } from 'react-router-dom';
import { FiHeadphones } from 'react-icons/fi';

function Header() {
    return (
        <header className="header flex items-center justify-between px-4 py-2 border-b bg-white">
            <div className="header-left">
                <span className="header-title font-semibold text-lg">물류 ERP 관리시스템</span>
            </div>

            <div className="header-right flex items-center gap-4">
                <Link
                    to="/support"
                    className="flex items-center gap-1.5 text-sm text-gray-600 hover:text-blue-600 transition-colors"
                >
                    <FiHeadphones className="text-base" />
                    <span>고객센터</span>
                </Link>

                <div className="flex items-center gap-2 text-sm">
                    <span className="header-company text-gray-500">Demo 식자재유통</span>
                    <span className="header-user font-medium">관리자</span>
                </div>
            </div>
        </header>
    );
}

export default Header;