import { useNavigate } from 'react-router-dom';

export default function Header() {
    const navigate = useNavigate();

    // 로그아웃 버튼 클릭 시 동작
    const handleLogout = () => {
        alert('로그아웃 되었습니다.');
        // 필요한 경우 로그인 페이지나 메인 페이지로 이동
        navigate('/');
    };

    return (
        <header style={{
            height: '60px',
            background: '#fff',
            borderBottom: '1px solid #e5e7eb',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '0 24px',
            position: 'sticky',
            top: 0,
            zIndex: 50
        }}>
            {/* 좌측 시스템 타이틀 영역 */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <span style={{ fontWeight: 'bold', fontSize: '16px', color: '#1f2937' }}>
          물류 ERP 관리시스템
        </span>
            </div>

            {/* 우측 고객센터 및 로그아웃 영역 */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                <button
                    onClick={() => navigate('/support')}
                    style={{ background: 'transparent', border: 'none', cursor: 'pointer', fontSize: '14px', color: '#4b5563', fontWeight: '500' }}
                >
                    🎧 고객센터
                </button>

                <div style={{ width: '1px', height: '16px', background: '#e5e7eb' }}></div>

                <button
                    onClick={handleLogout}
                    style={{
                        padding: '6px 14px',
                        fontSize: '13px',
                        background: '#f3f4f6',
                        color: '#374151',
                        border: '1px solid #d1d5db',
                        borderRadius: '6px',
                        cursor: 'pointer',
                        fontWeight: '500'
                    }}
                >
                    로그아웃
                </button>
            </div>
        </header>
    );
}