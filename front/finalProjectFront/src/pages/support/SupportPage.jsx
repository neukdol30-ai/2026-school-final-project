import { useState } from 'react';
import { Link } from 'react-router-dom';

export default function SupportPage() {
    const [activeTab, setActiveTab] = useState('notice');

    // 임시로 내 권한 상태 설정 (실제 프로젝트에서는 전역 상태나 로그인 정보 연동)
    // 'ADMIN' 이면 공지 등록 가능, 'USER' 이면 등록 버튼 숨김
    const [currentUserRole, setCurrentUserRole] = useState('ADMIN');

    // 공지사항 데이터
    const [notices, setNotices] = useState([
        {
            id: 1,
            title: '[공지] 추석 연휴 식자재 주문 및 배송 마감 일정 안내',
            date: '2026-09-20',
            author: '물류운영팀',
            content: '다가오는 추석 연휴 기간 동안의 식자재 주문 및 물류 센터 배송 마감 일정을 안내해 드립니다. 연휴 전 마지막 발주는 9월 25일 18:00까지 마감되오니 업무에 차질 없으시길 바랍니다.',
            isOpen: false
        },
        {
            id: 2,
            title: '[안내] 친환경 포장재 도입에 따른 일부 품목 규격 변경 안내',
            date: '2026-08-15',
            author: '상품관리팀',
            content: '환경 보호 및 신선도 유지를 위해 기존 스티로폼 박스 포장이 친환경 종이 펄프 및 생분해성 포장재로 순차 변경됩니다. 품목별 규격은 상품 상세페이지를 참고해 주세요.',
            isOpen: false
        },
        {
            id: 3,
            title: '[시스템] 정기 서버 점검 및 서비스 일시 중단 안내 (03/05)',
            date: '2026-03-01',
            author: '전산팀',
            content: '더 나은 ERP 서비스 제공을 위한 안정성 강화 작업으로 인해 3월 5일 새벽 02:00부터 04:00까지 2시간 동안 시스템 접속 및 주문 서비스가 일시 중단됩니다.',
            isOpen: false
        },
    ]);

    // 공지 등록 모달 및 폼 상태
    const [isNoticeModalOpen, setIsNoticeModalOpen] = useState(false);
    const [noticeForm, setNoticeForm] = useState({ title: '', author: '전산운영팀', content: '' });

    const faqs = [
        { q: '배송받은 식자재가 파손되었거나 불량이에요. 어떻게 하나요?', a: '수령 후 24시간 이내에 사진을 첨부하여 1:1 문의를 남겨주시거나 고객센터(1588-0000)로 연락 주시면 신속하게 교환 및 환불 처리를 도와드립니다.' },
        { q: '주문한 상품의 납품 시간을 변경하고 싶습니다.', a: '배송 출발 전(보통 전일 오후 6시 전까지) 고객센터나 담당 영업사원을 통해 변경 요청이 가능합니다.' },
        { q: '세금계산서는 언제 발행되나요?', a: '월 마감 후 익월 10일 이전에 전월 거래 내역을 통합하여 일괄 발행됩니다.' },
    ];

    const [qnas, setQnas] = useState([
        {
            id: 1,
            title: '냉동 돈까스 품목 단가 문의드립니다.',
            writer: '푸드마켓 구로점',
            date: '2026-08-27',
            status: '답변완료',
            content: '최근 물가 상승으로 인해 냉동 돈까스(치즈/등심) 품목의 대량 단가 변동이 있는지 확인 부탁드립니다. 기존 단가 그대로 유지되는지도 궁금합니다.',
            isOpen: false
        },
        {
            id: 2,
            title: '납품 시간 조정 요청 건',
            writer: '맛있는 한끼 식당',
            date: '2026-08-25',
            status: '답변대기',
            content: '기존 오전 9시 납품이었으나, 매장 사정으로 인해 내일(26일)만 오전 7시 30분으로 조기 납품이 가능한지 문의드립니다.',
            isOpen: false
        },
    ]);

    // 공지사항 아코디언 토글
    const toggleNotice = (id) => {
        setNotices(notices.map(n => n.id === id ? { ...n, isOpen: !n.isOpen } : n));
    };

    // 공지사항 등록 핸들러
    const handleAddNotice = (e) => {
        e.preventDefault();
        if (!noticeForm.title.trim() || !noticeForm.content.trim()) {
            alert('제목과 내용을 모두 입력해 주세요.');
            return;
        }

        const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD 형식
        const newNotice = {
            id: Date.now(),
            title: noticeForm.title,
            author: noticeForm.author,
            date: today,
            content: noticeForm.content,
            isOpen: false
        };

        setNotices([newNotice, ...notices]); // 최신 공지가 맨 위로 오도록 추가
        setNoticeForm({ title: '', author: '전산운영팀', content: '' });
        setIsNoticeModalOpen(false);
    };

    // 1:1 문의 아코디언 토글
    const toggleQna = (id) => {
        setQnas(qnas.map(q => q.id === id ? { ...q, isOpen: !q.isOpen } : q));
    };

    // 1:1 문의 상태 변경
    const handleStatusChange = (id, newStatus, e) => {
        e.stopPropagation();
        setQnas(qnas.map(q => q.id === id ? { ...q, status: newStatus } : q));
    };

    return (
        <div className="support-container">
            {/* 페이지 타이틀 및 테스트용 권한 변경 바 */}
            <div className="support-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                    <h1>고객센터</h1>
                    <p>식자재 주문, 배송, 이용과 관련된 궁금증을 해결해 드립니다.</p>
                </div>

                {/* 관리자 권한 테스트용 토글 버튼 (실제 운영 시에는 로그인 세션으로 대체) */}
                <div style={{ background: '#f3f4f6', padding: '8px 12px', borderRadius: '8px', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span>현재 내 권한: <strong>{currentUserRole}</strong></span>
                    <button
                        onClick={() => setCurrentUserRole(currentUserRole === 'ADMIN' ? 'USER' : 'ADMIN')}
                        style={{ padding: '2px 6px', background: '#fff', border: '1px solid #d1d5db', borderRadius: '4px', cursor: 'pointer' }}
                    >
                        권한전환
                    </button>
                </div>
            </div>

            {/* 상단 안내 카드 영역 */}
            <div className="support-cards">
                <div className="support-card blue">
                    <div className="card-title">고객센터 대표번호</div>
                    <div className="card-value">1588-0000</div>
                    <div className="card-desc">평일 08:00 - 18:00 (점심시간 12~13시)</div>
                </div>
                <div className="support-card">
                    <div className="card-title">이메일 문의</div>
                    <div className="card-value">support@1sterp.com</div>
                    <div className="card-desc">24시간 접수 가능</div>
                </div>
                <div className="support-card">
                    <div className="card-title">실시간 긴급 공지</div>
                    <div className="card-value" style={{ color: '#dc2626' }}>추석 연휴 배송 마감</div>
                    <div className="card-desc">공지사항 탭에서 확인하세요</div>
                </div>
            </div>

            {/* 탭 네비게이션 */}
            <div className="support-tabs">
                <button
                    onClick={() => setActiveTab('notice')}
                    className={`tab-btn ${activeTab === 'notice' ? 'active' : ''}`}
                >
                    공지사항
                </button>
                <button
                    onClick={() => setActiveTab('faq')}
                    className={`tab-btn ${activeTab === 'faq' ? 'active' : ''}`}
                >
                    자주 묻는 질문 (FAQ)
                </button>
                <button
                    onClick={() => setActiveTab('qna')}
                    className={`tab-btn ${activeTab === 'qna' ? 'active' : ''}`}
                >
                    1:1 문의하기
                </button>
            </div>

            {/* 탭 내용 영역 */}
            <div className="support-content-box">
                {/* 1. 공지사항 탭 */}
                {activeTab === 'notice' && (
                    <div>
                        <div className="qna-header-row" style={{ marginBottom: '16px' }}>
                            <h2>공지사항</h2>
                            {/* ADMIN 권한일 때만 공지 등록 버튼 노출 */}
                            {currentUserRole === 'ADMIN' && (
                                <button
                                    onClick={() => setIsNoticeModalOpen(true)}
                                    className="qna-write-btn"
                                >
                                    + 공지 등록
                                </button>
                            )}
                        </div>

                        <div className="divide-y divide-gray-100">
                            {notices.map((notice) => (
                                <div key={notice.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
                                    <div
                                        onClick={() => toggleNotice(notice.id)}
                                        className="notice-item"
                                        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 12px', cursor: 'pointer' }}
                                    >
                                        <div>
                                            <span className="notice-badge">공지</span>
                                            <span className="notice-title" style={{ fontWeight: '500' }}>{notice.title}</span>
                                            <span style={{ fontSize: '11px', color: '#9ca3af', marginLeft: '8px' }}>{notice.isOpen ? '▲ 닫기' : '▼ 내용보기'}</span>
                                        </div>
                                        <div className="notice-meta" style={{ display: 'flex', gap: '16px', fontSize: '12px', color: '#9ca3af' }}>
                                            <span>{notice.author}</span>
                                            <span>{notice.date}</span>
                                        </div>
                                    </div>

                                    {notice.isOpen && (
                                        <div style={{ background: '#f8fafc', padding: '20px 24px', borderTop: '1px solid #f1f5f9', borderBottom: '1px solid #e2e8f0' }}>
                                            <div style={{ fontSize: '13px', fontWeight: 'bold', color: '#475569', marginBottom: '6px' }}>
                                                [공지 상세 내용]
                                            </div>
                                            <div style={{ fontSize: '14px', color: '#1e293b', lineHeight: '1.6' }}>
                                                {notice.content}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* 2. FAQ 탭 */}
                {activeTab === 'faq' && (
                    <div>
                        <h2>자주 묻는 질문</h2>
                        <div>
                            {faqs.map((faq, idx) => (
                                <div key={idx} className="faq-item">
                                    <div className="faq-q">
                                        <span>Q.</span> {faq.q}
                                    </div>
                                    <div className="faq-a">
                                        <span>A.</span> {faq.a}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* 3. 1:1 문의 탭 */}
                {activeTab === 'qna' && (
                    <div>
                        <div className="qna-header-row">
                            <h2>1:1 문의 내역</h2>
                            <Link to="/support/write" className="qna-write-btn">
                                + 문의 등록
                            </Link>
                        </div>
                        <table className="support-table">
                            <thead>
                            <tr>
                                <th>제목</th>
                                <th>작성자</th>
                                <th>등록일</th>
                                <th>처리 상태</th>
                            </tr>
                            </thead>
                            <tbody>
                            {qnas.map((qna) => (
                                <>
                                    <tr
                                        key={qna.id}
                                        onClick={() => toggleQna(qna.id)}
                                        style={{ cursor: 'pointer', background: qna.isOpen ? '#f9fafb' : 'transparent' }}
                                    >
                                        <td style={{ fontWeight: '500', color: '#1f2937' }}>
                                            {qna.title} <span style={{ fontSize: '11px', color: '#9ca3af', marginLeft: '6px' }}>{qna.isOpen ? '▲ 닫기' : '▼ 내용보기'}</span>
                                        </td>
                                        <td style={{ color: '#4b5563' }}>{qna.writer}</td>
                                        <td style={{ color: '#9ca3af' }}>{qna.date}</td>
                                        <td>
                                            <select
                                                value={qna.status}
                                                onChange={(e) => handleStatusChange(qna.id, e.target.value, e)}
                                                onClick={(e) => e.stopPropagation()}
                                                style={{
                                                    padding: '4px 8px',
                                                    fontSize: '12px',
                                                    borderRadius: '9999px',
                                                    fontWeight: '600',
                                                    border: '1px solid #d1d5db',
                                                    background: qna.status === '답변완료' ? '#d1fae5' : '#fef3c7',
                                                    color: qna.status === '답변완료' ? '#065f46' : '#92400e',
                                                    cursor: 'pointer'
                                                }}
                                            >
                                                <option value="답변대기">답변대기</option>
                                                <option value="답변완료">답변완료</option>
                                            </select>
                                        </td>
                                    </tr>

                                    {qna.isOpen && (
                                        <tr key={`content-${qna.id}`} style={{ background: '#f8fafc' }}>
                                            <td colSpan="4" style={{ padding: '20px 24px', borderBottom: '1px solid #e2e8f0' }}>
                                                <div style={{ fontSize: '13px', fontWeight: 'bold', color: '#475569', marginBottom: '6px' }}>
                                                    [문의 상세 내용]
                                                </div>
                                                <div style={{ fontSize: '14px', color: '#1e293b', lineHeight: '1.6' }}>
                                                    {qna.content}
                                                </div>
                                            </td>
                                        </tr>
                                    )}
                                </>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* 관리자 전용 공지사항 등록 모달 */}
            {isNoticeModalOpen && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
                    background: 'rgba(0, 0, 0, 0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000
                }}>
                    <div style={{ background: '#fff', padding: '24px', borderRadius: '12px', width: '450px', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}>
                        <h2 style={{ fontSize: '18px', fontWeight: 'bold', marginBottom: '16px', color: '#1f2937' }}>공지사항 등록 (관리자 전용)</h2>

                        <form onSubmit={handleAddNotice}>
                            <div style={{ marginBottom: '12px' }}>
                                <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: '#374151' }}>공지 제목</label>
                                <input
                                    type="text"
                                    placeholder="예: [안내] 시스템 점검 일정 안내"
                                    value={noticeForm.title}
                                    onChange={(e) => setNoticeForm({ ...noticeForm, title: e.target.value })}
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #d1d5db', fontSize: '14px' }}
                                />
                            </div>

                            <div style={{ marginBottom: '12px' }}>
                                <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: '#374151' }}>작성 부서</label>
                                <select
                                    value={noticeForm.author}
                                    onChange={(e) => setNoticeForm({ ...noticeForm, author: e.target.value })}
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #d1d5db', fontSize: '14px' }}
                                >
                                    <option value="물류운영팀">물류운영팀</option>
                                    <option value="상품관리팀">상품관리팀</option>
                                    <option value="전산운영팀">전산운영팀</option>
                                    <option value="경영지원팀">경영지원팀</option>
                                </select>
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: '#374151' }}>공지 내용</label>
                                <textarea
                                    rows="5"
                                    placeholder="공지할 상세 내용을 입력하세요."
                                    value={noticeForm.content}
                                    onChange={(e) => setNoticeForm({ ...noticeForm, content: e.target.value })}
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #d1d5db', fontSize: '14px', resize: 'vertical' }}
                                />
                            </div>

                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                                <button
                                    type="button"
                                    onClick={() => setIsNoticeModalOpen(false)}
                                    style={{ background: '#f3f4f6', color: '#374151', border: 'none', padding: '8px 16px', borderRadius: '6px', fontSize: '13px', cursor: 'pointer' }}
                                >
                                    취소
                                </button>
                                <button
                                    type="submit"
                                    className="qna-write-btn"
                                    style={{ padding: '8px 16px', fontSize: '13px' }}
                                >
                                    등록하기
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}