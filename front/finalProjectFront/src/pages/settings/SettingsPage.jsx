import { useState } from 'react';

export default function SettingsPage() {
    const [users, setUsers] = useState([
        { id: 1, name: '관리자', email: 'admin@1sterp.com', department: '전산운영팀', role: 'ADMIN' },
        { id: 2, name: '김물류', email: 'logistics@1sterp.com', department: '물류운영팀', role: 'USER' },
        { id: 3, name: '이영업', email: 'sales@1sterp.com', department: '영업관리팀', role: 'USER' },
        { id: 4, name: '박재고', email: 'stock@1sterp.com', department: '재고관리팀', role: 'USER' },
    ]);

    // 모달 오픈 상태 및 입력 폼 상태 관리
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        department: '물류운영팀',
        role: 'USER',
    });

    // 권한 토글 함수 (ADMIN <-> USER)
    const toggleRole = (id) => {
        setUsers(users.map(user => {
            if (user.id === id) {
                const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
                return { ...user, role: newRole };
            }
            return user;
        }));
    };

    // 사용자 삭제 함수
    const handleDeleteUser = (id, name) => {
        if (window.confirm(`정말로 '${name}' 사용자를 삭제하시겠습니까?`)) {
            setUsers(users.filter(user => user.id !== id));
        }
    };

    // 사용자 등록 폼 제출 핸들러
    const handleAddUser = (e) => {
        e.preventDefault();
        if (!formData.name.trim() || !formData.email.trim()) {
            alert('이름과 이메일을 모두 입력해 주세요.');
            return;
        }

        const newUser = {
            id: Date.now(), // 고유 ID 생성
            ...formData,
        };

        setUsers([...users, newUser]);
        setFormData({ name: '', email: '', department: '물류운영팀', role: 'USER' }); // 폼 초기화
        setIsModalOpen(false); // 모달 닫기
    };

    return (
        <div className="support-container">
            {/* 페이지 타이틀 */}
            <div className="support-header">
                <h1>사용자 / 권한 설정</h1>
                <p>시스템에 등록된 사용자의 권한(관리자 / 일반 사용자)을 관리하고 신규 사용자를 추가할 수 있습니다.</p>
            </div>

            {/* 안내 박스 */}
            <div style={{ background: '#eff6ff', border: '1px solid #dbeafe', borderRadius: '8px', padding: '16px', marginBottom: '24px', fontSize: '14px', color: '#1e40af' }}>
                💡 <strong>안내:</strong> 관리자(ADMIN) 권한을 가진 사용자는 고객센터 문의 상태 변경, 전체 시스템 설정 등을 제어할 수 있습니다.
            </div>

            {/* 사용자 목록 헤더 및 추가 버튼 */}
            <div className="qna-header-row" style={{ marginBottom: '16px' }}>
                <h2 style={{ fontSize: '18px', fontWeight: '600', color: '#1f2937', margin: 0 }}>사용자 권한 목록</h2>
                <button
                    onClick={() => setIsModalOpen(true)}
                    className="qna-write-btn"
                >
                    + 사용자 등록
                </button>
            </div>

            {/* 사용자 목록 테이블 */}
            <div className="support-content-box">
                <table className="support-table">
                    <thead>
                    <tr>
                        <th>이름</th>
                        <th>이메일</th>
                        <th>부서</th>
                        <th>현재 권한</th>
                        <th>권한 변경</th>
                        <th>관리</th>
                    </tr>
                    </thead>
                    <tbody>
                    {users.map((user) => (
                        <tr key={user.id}>
                            <td style={{ fontWeight: '500', color: '#1f2937' }}>{user.name}</td>
                            <td style={{ color: '#4b5563' }}>{user.email}</td>
                            <td style={{ color: '#4b5563' }}>{user.department}</td>
                            <td>
                  <span style={{
                      padding: '4px 10px',
                      fontSize: '11px',
                      fontWeight: 'bold',
                      borderRadius: '9999px',
                      background: user.role === 'ADMIN' ? '#d1fae5' : '#f3f4f6',
                      color: user.role === 'ADMIN' ? '#065f46' : '#4b5563',
                  }}>
                    {user.role === 'ADMIN' ? '관리자 (ADMIN)' : '일반사용자 (USER)'}
                  </span>
                            </td>
                            <td>
                                <button
                                    onClick={() => toggleRole(user.id)}
                                    style={{
                                        padding: '6px 12px',
                                        fontSize: '12px',
                                        fontWeight: '500',
                                        borderRadius: '6px',
                                        border: '1px solid #d1d5db',
                                        background: user.role === 'ADMIN' ? '#fee2e2' : '#eff6ff',
                                        color: user.role === 'ADMIN' ? '#991b1b' : '#1d4ed8',
                                        cursor: 'pointer',
                                    }}
                                >
                                    {user.role === 'ADMIN' ? '관리자 해제' : '관리자 승격'}
                                </button>
                            </td>
                            <td>
                                <button
                                    onClick={() => handleDeleteUser(user.id, user.name)}
                                    style={{
                                        padding: '6px 10px',
                                        fontSize: '12px',
                                        fontWeight: '500',
                                        borderRadius: '6px',
                                        border: '1px solid #fecaca',
                                        background: '#fff5f5',
                                        color: '#dc2626',
                                        cursor: 'pointer',
                                    }}
                                >
                                    삭제
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {/* 사용자 등록 모달 (팝업) */}
            {isModalOpen && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    width: '100%',
                    height: '100%',
                    background: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    zIndex: 1000
                }}>
                    <div style={{
                        background: '#fff',
                        padding: '24px',
                        borderRadius: '12px',
                        width: '400px',
                        boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
                    }}>
                        <h2 style={{ fontSize: '18px', fontWeight: 'bold', marginBottom: '16px', color: '#1f2937' }}>신규 사용자 등록</h2>

                        <form onSubmit={handleAddUser}>
                            <div style={{ marginBottom: '12px' }}>
                                <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: '#374151' }}>이름</label>
                                <input
                                    type="text"
                                    placeholder="예: 홍길동"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #d1d5db', fontSize: '14px' }}
                                />
                            </div>

                            <div style={{ marginBottom: '12px' }}>
                                <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: '#374151' }}>이메일</label>
                                <input
                                    type="email"
                                    placeholder="예: user@1sterp.com"
                                    value={formData.email}
                                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #d1d5db', fontSize: '14px' }}
                                />
                            </div>

                            <div style={{ marginBottom: '12px' }}>
                                <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: '#374151' }}>부서</label>
                                <select
                                    value={formData.department}
                                    onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #d1d5db', fontSize: '14px' }}
                                >
                                    <option value="물류운영팀">물류운영팀</option>
                                    <option value="영업관리팀">영업관리팀</option>
                                    <option value="재고관리팀">재고관리팀</option>
                                    <option value="상품관리팀">상품관리팀</option>
                                    <option value="전산운영팀">전산운영팀</option>
                                </select>
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: '#374151' }}>초기 권한</label>
                                <select
                                    value={formData.role}
                                    onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #d1d5db', fontSize: '14px' }}
                                >
                                    <option value="USER">일반사용자 (USER)</option>
                                    <option value="ADMIN">관리자 (ADMIN)</option>
                                </select>
                            </div>

                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                                <button
                                    type="button"
                                    onClick={() => setIsModalOpen(false)}
                                    style={{ background: '#f3f4f6', color: '#374151', border: 'none', padding: '8px 16px', borderRadius: '6px', fontSize: '13px', cursor: 'pointer' }}
                                >
                                    취소
                                </button>
                                <button
                                    type="submit"
                                    className="qna-write-btn"
                                    style={{ padding: '8px 16px', fontSize: '13px' }}
                                >
                                    등록
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}