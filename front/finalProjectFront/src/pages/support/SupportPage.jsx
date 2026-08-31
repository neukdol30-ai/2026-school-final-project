import { useState } from 'react';

export default function SupportPage() {
    const [activeTab, setActiveTab] = useState('notice');

    const notices = [
        { id: 1, title: '[공지] 추석 연휴 식자재 주문 및 배송 마감 일정 안내', date: '2026-09-20', author: '물류운영팀' },
        { id: 2, title: '[안내] 친환경 포장재 도입에 따른 일부 품목 규격 변경 안내', date: '2026-08-15', author: '상품관리팀' },
        { id: 3, title: '[시스템] 정기 서버 점검 및 서비스 일시 중단 안내 (03/05)', date: '2026-03-01', author: '전산팀' },
    ];

    const faqs = [
        { q: '배송받은 식자재가 파손되었거나 불량이에요. 어떻게 하나요?', a: '수령 후 24시간 이내에 사진을 첨부하여 1:1 문의를 남겨주시거나 고객센터(1588-0000)로 연락 주시면 신속하게 교환 및 환불 처리를 도와드립니다.' },
        { q: '주문한 상품의 납품 시간을 변경하고 싶습니다.', a: '배송 출발 전(보통 전일 오후 6시 전까지) 고객센터나 담당 영업사원을 통해 변경 요청이 가능합니다.' },
        { q: '세금계산서는 언제 발행되나요?', a: '월 마감 후 익월 10일 이전에 전월 거래 내역을 통합하여 일괄 발행됩니다.' },
    ];

    const qnas = [
        { id: 1, title: '냉동 돈까스 품목 단가 문의드립니다.', writer: '푸드마켓 구로점', date: '2026-08-27', status: '답변완료' },
        { id: 2, title: '납품 시간 조정 요청 건', writer: '맛있는 한끼 식당', date: '2026-08-25', status: '답변대기' },
    ];

    return (
        <div className="support-container">
            {/* 페이지 타이틀 */}
            <div className="support-header">
                <h1>고객센터</h1>
                <p>식자재 주문, 배송, 이용과 관련된 궁금증을 해결해 드립니다.</p>
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
                        <h2>공지사항</h2>
                        <div>
                            {notices.map((notice) => (
                                <div key={notice.id} className="notice-item">
                                    <div>
                                        <span className="notice-badge">공지</span>
                                        <span className="notice-title">{notice.title}</span>
                                    </div>
                                    <div className="notice-meta">
                                        <span>{notice.author}</span>
                                        <span>{notice.date}</span>
                                    </div>
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
                            <button
                                onClick={() => alert('문의 등록 모달이 열립니다!')}
                                className="qna-write-btn"
                            >
                                + 문의 등록
                            </button>
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
                                <tr key={qna.id}>
                                    <td style={{ fontWeight: '500', cursor: 'pointer' }}>{qna.title}</td>
                                    <td style={{ color: '#4b5563' }}>{qna.writer}</td>
                                    <td style={{ color: '#9ca3af' }}>{qna.date}</td>
                                    <td>
                      <span className={`status-badge ${qna.status === '답변완료' ? 'done' : 'wait'}`}>
                        {qna.status}
                      </span>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}