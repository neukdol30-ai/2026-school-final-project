import React from 'react';

function CsPage() {
    return (
        <div className="p-6 space-y-6 bg-slate-50 min-h-screen font-sans">

            {/* 페이지 타이틀 */}
            <div className="text-xl font-bold text-slate-800">
                CS관리
            </div>

            {/* 상단 상태 요약 카드 (접수, 처리중, 완료, 반려) */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
                    <span className="text-sm font-semibold text-slate-600">접수</span>
                    <div className="text-3xl font-bold text-blue-600 mt-2">0</div>
                </div>
                <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
                    <span className="text-sm font-semibold text-slate-600">처리중</span>
                    <div className="text-3xl font-bold text-blue-600 mt-2">0</div>
                </div>
                <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
                    <span className="text-sm font-semibold text-slate-600">완료</span>
                    <div className="text-3xl font-bold text-emerald-600 mt-2">0</div>
                </div>
                <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
                    <span className="text-sm font-semibold text-slate-600">반려</span>
                    <div className="text-3xl font-bold text-rose-600 mt-2">0</div>
                </div>
            </div>

            {/* 검색 및 액션 버튼 영역 */}
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-wrap items-center justify-between gap-4">
                <div className="flex items-center gap-2">
                    <select className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white">
                        <option>전체 유형</option>
                    </select>
                    <select className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white">
                        <option>전체 상태</option>
                    </select>
                    <input
                        type="text"
                        placeholder="주문번호 / 송장번호"
                        className="border border-slate-300 rounded-lg px-3 py-2 text-sm w-64 focus:outline-none focus:border-indigo-500"
                    />
                    <button className="bg-slate-800 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-slate-700">
                        검색
                    </button>
                </div>

                <div className="flex items-center gap-2">
                    <button className="bg-emerald-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-emerald-500">
                        반품검수
                    </button>
                    <button className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-500">
                        + CS등록
                    </button>
                </div>
            </div>

            {/* CS 목록 테이블 영역 */}
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <div className="p-4 border-b border-slate-100 text-sm font-bold text-slate-700">
                    총 0건
                </div>

                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse text-sm">
                        <thead>
                        <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 text-xs">
                            <th className="p-3 font-semibold text-center">No</th>
                            <th className="p-3 font-semibold">유형</th>
                            <th className="p-3 font-semibold">주문번호</th>
                            <th className="p-3 font-semibold">송장번호</th>
                            <th className="p-3 font-semibold">고객 / CS사유</th>
                            <th className="p-3 font-semibold">상품</th>
                            <th className="p-3 font-semibold">상태</th>
                            <th className="p-3 font-semibold">검수결과</th>
                            <th className="p-3 font-semibold">처리자</th>
                            <th className="p-3 font-semibold">등록일</th>
                            <th className="p-3 font-semibold text-center">재입고</th>
                            <th className="p-3 font-semibold text-center">처리</th>
                            <th className="p-3 font-semibold text-center">수정</th>
                            <th className="p-3 font-semibold text-center">삭제</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td colSpan="14" className="text-center py-16 text-slate-400 text-sm">
                                등록된 CS건이 없습니다.
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    );
}

export default CsPage;