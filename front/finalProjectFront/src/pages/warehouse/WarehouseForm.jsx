function WarehouseForm({
                           form,
                           saving,
                           editing,
                           onChange,
                           onSubmit,
                           onCancel,
                       }) {
    return (
        <form
            className="warehouse-edit-panel"
            onSubmit={onSubmit}
        >
            <div className="warehouse-edit-heading">
                <h2>
                    {editing
                        ? "창고 수정"
                        : "창고 등록"}
                </h2>

                <button
                    type="button"
                    onClick={onCancel}
                    disabled={saving}
                >
                    닫기
                </button>
            </div>

            <div className="warehouse-edit-fields">
                <label>
                    <span>창고코드 *</span>

                    <input
                        type="text"
                        name="warehouseCode"
                        maxLength="30"
                        value={form.warehouseCode}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="창고코드 입력"
                        required
                    />
                </label>

                <label>
                    <span>창고명 *</span>

                    <input
                        type="text"
                        name="warehouseName"
                        maxLength="100"
                        value={form.warehouseName}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="창고명 입력"
                        required
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

                <label className="warehouse-field-wide">
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

                <label className="warehouse-field-wide">
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

                <label className="warehouse-field-wide">
                    <span>설명</span>

                    <textarea
                        name="description"
                        maxLength="200"
                        value={form.description}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="창고 설명 입력"
                        rows="3"
                    />
                </label>
            </div>

            <div className="warehouse-edit-actions">
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

export default WarehouseForm;