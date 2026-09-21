function UnitForm({
                      form,
                      saving,
                      editing,
                      onChange,
                      onSubmit,
                      onCancel,
                  }) {
    return (
        <form
            className="unit-edit-panel"
            onSubmit={onSubmit}
        >
            <div className="unit-edit-heading">
                <h2>
                    {editing
                        ? "단위 수정"
                        : "단위 등록"}
                </h2>

                <button
                    type="button"
                    onClick={onCancel}
                    disabled={saving}
                >
                    닫기
                </button>
            </div>

            <div className="unit-edit-fields">
                <label>
                    <span>단위코드 *</span>

                    <input
                        type="text"
                        name="unitCode"
                        maxLength="20"
                        value={form.unitCode}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="예: EA, BOX, KG"
                        required
                    />
                </label>

                <label>
                    <span>단위명 *</span>

                    <input
                        type="text"
                        name="unitName"
                        maxLength="50"
                        value={form.unitName}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="단위명 입력"
                        required
                    />
                </label>

                <label className="unit-field-wide">
                    <span>설명</span>

                    <textarea
                        name="description"
                        maxLength="200"
                        value={form.description}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="단위 설명 입력"
                        rows="3"
                    />
                </label>
            </div>

            <div className="unit-edit-actions">
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

export default UnitForm;