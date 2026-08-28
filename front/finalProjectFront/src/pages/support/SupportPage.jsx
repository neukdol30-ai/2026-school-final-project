import React, { useState } from 'react';
import { FiBell, FiHelpCircle, FiMessageSquare, FiPlus, FiSearch } from 'react-icons/fi';

export default function SupportPage() {
    // 탭 상태 관리 ('notice' | 'faq' | 'qna')
    const [activeTab, setActiveTab] = useState('notice');

    // 임시 데이터: 공지사항
    const notices = [
        { id: 1, title: '[공지] 추석 연휴 식자재 주문 및 배송 마감 일정 안내', date: '2026-09-20', author: '물류운영팀' },
        { id: 2, title: '[안내] 친환경 포장재 도입에 따른 일부 품목 규격 변경 안내', date: '2026-08-15', author: '상품관리팀' },
        { id: 3, title: '[시스템] 정기 서버 점검 및 서비스 일시 중단 안내 (03/05)', date: '2026-03-01', author: '전산팀' },
    ];

    // 임시 데이터: FAQ
    const faqs = [
        { q: '배송받은 식자재가 파손되었거나 불량이에요. 어떻게 하나요?', a: '수령 후 24시간 이내에 사진을 첨부하여 1:1 문의를 남겨주시거나 고객센터(1588-0000)로 연락 주시면 신속하게 교환 및 환불 처리를 도와드립니다.' },
        { q: '주문한 상품의 납품 시간을 변경하고 싶습니다.', a: '배송 출발 전(보통 전일 오후 6시 전까지) 고객센터나 담당 영업사원을 통해 변경 요청이 가능합니다.' },
        { q: '세금계산서는 언제 발행되나요?', a: '월 마감 후 익월 10일 이전에 전월 거래 내역을 통합하여 일괄 발행됩니다.' },
    ];

    // 임시 데이터: 1:1 문의 (Q&A)
    const [qnas, setQnas] = useState([
        { id: 1, title: '냉동 돈까스 품목 단가 문의드립니다.', writer: '푸드마켓 구로점', date: '2026-08-27', status: '답변완료' },
        { id: 2, title: '납품 시간 조정 요청 건', writer: '맛있는 한끼 식당', date: '2026-08-25', status: '답변대기' },
    ]);

    return (
        <div className="p-6 max-w-7xl mx-auto">
            {/* 페이지 타이틀 */}
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-gray-800">고객센터</h1>
                <p className="text-sm text-gray-500 mt-1">식자재 주문, 배송, 이용과 관련된 궁금증을 해결해 드립니다.</p>
            </div>

            {/* 상단 안내 카드 영역 */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                <div className="bg-blue-50 border border-blue-100 rounded-lg p-5 flex items-center justify-between">
                    <div>
                        <p className="text-xs font-semibold text-blue-600 uppercase tracking-wider">고객센터 대표번호</p>
                        <p className="text-xl font-bold text-gray-800 mt-1">1588-0000</p>
                        <p className="text-xs text-gray-500 mt-1">평일 08:00 - 18:00 (점심시간 12~13시)</p>
                    </div>
                    <FiHelpCircle className="text-blue-400 text-3xl" />
                </div>
                <div className="bg-white border rounded-lg p-5 flex items-center justify-between shadow-sm">
                    <div>
                        <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider">이메일 문의</p>
                        <p className="text-lg font-bold text-gray-800 mt-1">support@1sterp.com</p>
                        <p className="text-xs text-gray-500 mt-1">24시간 접수 가능</p>
                    </div>
                    <FiMessageSquare className="text-gray-400 text-3xl" />
                </div>
                <div className="bg-white border rounded-lg p-5 flex items-center justify-between shadow-sm">
                    <div>
                        <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider">실시간 긴급 공지</p>
                        <p className="text-sm font-bold text-red-600 mt-1">추석 연휴 배송 마감 안내</p>
                        <p className="text-xs text-gray-500 mt-1">공지사항 탭에서 확인하세요</p>
                    </div>
                    <FiBell className="text-red-400 text-3xl" />
                </div>
            </div>

            {/* 탭 네비게이션 */}
            <div className="flex border-b border-gray-200 mb-6">
                <button
                    onClick={() => setActiveTab('notice')}
                    className={`pb-3 px-6 text-sm font-medium border-b-2 transition-colors ${
                        activeTab === 'notice'
                            ? 'border-blue-600 text-blue-600'
                            : 'border-transparent text-gray-500 hover:text-gray-700'
                    }`}
                >
                    공지사항
                </button>
                <button
                    onClick={() => setActiveTab('faq')}
                    className={`pb-3 px-6 text-sm font-medium border-b-2 transition-colors ${
                        activeTab === 'faq'
                            ? 'border-blue-600 text-blue-600'
                            : 'border-transparent text-gray-500 hover:text-gray-700'
                    }`}
                >
                    자주 묻는 질문 (FAQ)
                </button>
                <button
                    onClick={() => setActiveTab('qna')}
                    className={`pb-3 px-6 text-sm font-medium border-b-2 transition-colors ${
                        activeTab === 'qna'
                            ? 'border-blue-600 text-blue-600'
                            : 'border-transparent text-gray-500 hover:text-gray-700'
                    }`}
                >
                    1:1 문의하기
                </button>
            </div>

            {/* 탭 내용 영역 */}
            <div className="bg-white rounded-lg border shadow-sm p-6">
                {/* 1. 공지사항 탭 */}
                {activeTab === 'notice' && (
                    <div>
                        <h2 className="text-lg font-semibold text-gray-800 mb-4">공지사항</h2>
                        <div className="divide-y divide-gray-100">
                            {notices.map((notice) => (
                                <div key={notice.id} className="py-4 flex justify-between items-center hover:bg-gray-50 px-3 rounded transition-colors cursor-pointer">
                                    <div>
                                        <span className="inline-block bg-blue-100 text-blue-700 text-xs px-2 py-0.5 rounded font-medium mr-2">공지</span>
                                        <span className="text-sm font-medium text-gray-800">{notice.title}</span>
                                    </div>
                                    <div className="text-xs text-gray-400 flex gap-4">
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
                        <h2 className="text-lg font-semibold text-gray-800 mb-4">자주 묻는 질문</h2>
                        <div className="space-y-4">
                            {faqs.map((faq, idx) => (
                                <div key={idx} className="border rounded-lg p-4 bg-gray-50">
                                    <p className="text-sm font-bold text-gray-800 flex items-start gap-2">
                                        <span className="text-blue-600 font-extrabold">Q.</span> {faq.q}
                                    </p>
                                    <p className="text-sm text-gray-600 mt-2 pl-5">
                                        <span className="text-emerald-600 font-bold mr-1">A.</span> {faq.a}
                                    </p>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* 3. 1:1 문의 탭 */}
                {activeTab === 'qna' && (
                    <div>
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-lg font-semibold text-gray-800">1:1 문의 내역</h2>
                            <button
                                onClick={() => alert('문의 작성 모달 또는 페이지로 연결됩니다!')}
                                className="flex items-center gap-1.5 bg-blue-600 text-white text-sm px-4 py-2 rounded hover:bg-blue-700 transition-colors"
                            >
                                <FiPlus /> 문의 등록
                            </button>
                        </div>
                        <table className="w-full text-left border-collapse">
                            <thead>
                            <tr className="border-b bg-gray-50 text-xs text-gray-500 uppercase">
                                <th className="py-3 px-4">제목</th>
                                <th className="py-3 px-4">작성자</th>
                                <th className="py-3 px-4">등록일</th>
                                <th className="py-3 px-4">처리 상태</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100 text-sm">
                            {qnas.map((qna) => (
                                <tr key={qna.id} className="hover:bg-gray-50">
                                    <td className="py-3 px-4 font-medium text-gray-800 cursor-pointer">{qna.title}</td>
                                    <td className="py-3 px-4 text-gray-600">{qna.writer}</td>
                                    <td className="py-3 px-4 text-gray-400">{qna.date}</td>
                                    <td className="py-3 px-4">
                      <span className={`px-2 py-1 text-xs rounded-full font-medium ${
                          qna.status === '답변완료' ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-700'
                      }`}>
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