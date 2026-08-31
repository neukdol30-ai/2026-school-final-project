import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function SupportWritePage() {
    const navigate = useNavigate();
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [category, setCategory] = useState('상품/단가');

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!title.trim() || !content.trim()) {
            alert('제목과 내용을 모두 입력해 주세요.');
            return;
        }

        // 실제 백엔드 연동 시 이 부분에서 API 호출을 하게 됩니다.
        alert('문의가 성공적으로 등록되었습니다.');
        navigate('/support'); // 등록 후 고객센터 메인으로 이동
    };

    return (
        <div className="support-container">
            <div className="support-header">
                <h1>1:1 문의 등록</h1>
                <p>궁금하신 점이나 불편 사항을 남겨주시면 담당자가 신속하게 답변해 드립니다.</p>
            </div>

            <div className="support-content-box">
                <form onSubmit={handleSubmit}>
                    {/* 문의 유형 선택 */}
                    <div style={{ marginBottom: '16px' }}>
                        <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', marginBottom: '8px', color: '#374151' }}>
                            문의 유형
                        </label>
                        <select
                            value={category}
                            onChange={(e) => setCategory(e.target.value)}
                            style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #d1d5db', fontSize: '14px' }}
                        >
                            <option value="상품/단가">상품 및 단가 문의</option>
                            <option value="배송/납품">배송 및 납품 시간 조정</option>
                            <option value="파손/불량">파손 및 오배송 접수</option>
                            <option value="기타">기타 문의</option>
                        </select>
                    </div>

                    {/* 제목 입력 */}
                    <div style={{ marginBottom: '16px' }}>
                        <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', marginBottom: '8px', color: '#374151' }}>
                            제목
                        </label>
                        <input
                            type="text"
                            placeholder="제목을 입력해 주세요."
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #d1d5db', fontSize: '14px' }}
                        />
                    </div>

                    {/* 내용 입력 */}
                    <div style={{ marginBottom: '24px' }}>
                        <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', marginBottom: '8px', color: '#374151' }}>
                            문의 내용
                        </label>
                        <textarea
                            rows="6"
                            placeholder="상세한 내용을 입력해 주세요."
                            value={content}
                            onChange={(e) => setContent(e.target.value)}
                            style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #d1d5db', fontSize: '14px', resize: 'vertical' }}
                        />
                    </div>

                    {/* 버튼 영역 */}
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                        <button
                            type="button"
                            onClick={() => navigate('/support')}
                            style={{ background: '#f3f4f6', color: '#374151', border: 'none', padding: '10px 20px', borderRadius: '6px', fontSize: '14px', fontWeight: '500', cursor: 'pointer' }}
                        >
                            취소
                        </button>
                        <button
                            type="submit"
                            className="qna-write-btn"
                        >
                            등록하기
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}