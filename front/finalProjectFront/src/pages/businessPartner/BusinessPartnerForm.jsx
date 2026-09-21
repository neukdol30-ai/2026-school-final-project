function BusinessPartnerForm({
                                 form,
                                 saving,
                                 editing,
                                 onChange,
                                 onSubmit,
                                 onCancel,
                             }) {
    return (
        <form
            className="business-partner-edit-panel"
            onSubmit={onSubmit}
        >
            <div className="business-partner-edit-heading">
                <h2>
                    {editing
                        ? "거래처 수정"
                        : "거래처 등록"}
                </h2>

                <button
                    type="button"
                    onClick={onCancel}
                    disabled={saving}
                >
                    닫기
                </button>
            </div>

            <div className="business-partner-edit-fields">
                <label>
                    <span>거래처코드 *</span>

                    <input
                        type="text"
                        name="partnerCode"
                        maxLength="30"
                        value={form.partnerCode}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="거래처코드 입력"
                        required
                    />
                </label>

                <label>
                    <span>거래처명 *</span>

                    <input
                        type="text"
                        name="partnerName"
                        maxLength="100"
                        value={form.partnerName}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="거래처명 입력"
                        required
                    />
                </label>

                <label>
                    <span>공급업체 여부 *</span>

                    <select
                        name="supplierYn"
                        value={form.supplierYn}
                        onChange={onChange}
                        disabled={saving}
                    >
                        <option value="Y">해당</option>
                        <option value="N">해당 없음</option>
                    </select>
                </label>

                <label>
                    <span>판매처 여부 *</span>

                    <select
                        name="customerYn"
                        value={form.customerYn}
                        onChange={onChange}
                        disabled={saving}
                    >
                        <option value="Y">해당</option>
                        <option value="N">해당 없음</option>
                    </select>
                </label>

                <label>
                    <span>사업자등록번호</span>

                    <input
                        type="text"
                        name="businessNumber"
                        maxLength="12"
                        value={form.businessNumber}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="000-00-00000"
                    />
                </label>

                <label>
                    <span>대표자명</span>

                    <input
                        type="text"
                        name="representativeName"
                        maxLength="50"
                        value={form.representativeName}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="대표자명 입력"
                    />
                </label>

                <label>
                    <span>담당자명</span>

                    <input
                        type="text"
                        name="contactName"
                        maxLength="50"
                        value={form.contactName}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="담당자명 입력"
                    />
                </label>

                <label>
                    <span>연락처</span>

                    <input
                        type="text"
                        name="phone"
                        maxLength="20"
                        value={form.phone}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="연락처 입력"
                    />
                </label>

                <label>
                    <span>이메일</span>

                    <input
                        type="email"
                        name="email"
                        maxLength="254"
                        value={form.email}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="example@company.com"
                    />
                </label>

                <label>
                    <span>우편번호</span>

                    <input
                        type="text"
                        name="postalCode"
                        maxLength="10"
                        value={form.postalCode}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="우편번호 입력"
                    />
                </label>

                <label className="business-partner-field-wide">
                    <span>기본주소</span>

                    <input
                        type="text"
                        name="address1"
                        maxLength="200"
                        value={form.address1}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="기본주소 입력"
                    />
                </label>

                <label className="business-partner-field-wide">
                    <span>상세주소</span>

                    <input
                        type="text"
                        name="address2"
                        maxLength="200"
                        value={form.address2}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="상세주소 입력"
                    />
                </label>
            </div>

            <div className="business-partner-edit-actions">
                <button
                    type="button"
                    onClick={onCancel}
                    disabled={saving}
                >
                    취소
                </button>

                <button
                    type="submit"
                    disabled={saving}
                >
                    {saving ? "저장 중..." : "저장"}
                </button>
            </div>
        </form>
    );
}

export default BusinessPartnerForm;