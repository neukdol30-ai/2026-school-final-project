import {
    Menu,
    Bell,
    UserCircle,
    LogOut,
} from 'lucide-react'

function Topbar({ onToggleSidebar }) {

    const handleLogout = () => {
        console.log('로그아웃')
    }

    const handleNotification = () => {
        console.log('알림')
    }

    return (
        <header className="topbar">

            {/* 왼쪽 */}
            <div className="topbar-left">

                <button
                    type="button"
                    className="menu-button"
                    onClick={onToggleSidebar}
                    aria-label="메뉴 열기"
                >
                    <Menu size={22} />
                </button>

                <div className="topbar-logo">
                    식자재 ERP
                </div>

            </div>


            {/* 오른쪽 */}
            <div className="topbar-right">

                {/* 회사 */}
                <div className="company-info">

          <span className="company-label">
            회사
          </span>

                    <strong>
                        우리식자재
                    </strong>

                </div>


                {/* 알림 */}
                <button
                    type="button"
                    className="notification-button"
                    onClick={handleNotification}
                    aria-label="알림"
                >
                    <Bell size={20} />

                    <span className="notification-badge">
            3
          </span>

                </button>


                {/* 사용자 */}
                <div className="user-info">

                    <UserCircle size={24} />

                    <div className="user-detail">

                        <strong>
                            홍길동
                        </strong>

                        <span>
              관리자
            </span>

                    </div>

                </div>


                {/* 로그아웃 */}
                <button
                    type="button"
                    className="logout-button"
                    onClick={handleLogout}
                >
                    <LogOut size={18} />

                    <span>
            로그아웃
          </span>

                </button>

            </div>

        </header>
    )
}

export default Topbar