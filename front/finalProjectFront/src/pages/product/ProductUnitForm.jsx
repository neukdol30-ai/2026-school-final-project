function ProductUnitForm({
                             form,
                             units,
                             saving,
                             editing,
                             onChange,
                             onSubmit,
                             onCancel,
                         }) {
    return (
        <form
            className="product-unit-form"
            onSubmit={onSubmit}
        >
            <div className="product-unit-form-heading">
                <h3>
                    {editing
                        ? "상품단위 수정"
                        : "상품단위 등록"}
                </h3>
            </div>

            <div className="product-unit-form-fields">
                <label>
                    <span>단위 *</span>

                    <select
                        name="unitId"
                        value={form.unitId}
                        onChange={onChange}
                        disabled={saving || editing}
                    >
                        <option value="">
                            단위를 선택하세요
                        </option>

                        {units.map((unit) => (
                            <option
                                key={unit.unitId}
                                value={unit.unitId}
                            >
                                {unit.unitCode} · {unit.unitName}
                            </option>
                        ))}
                    </select>
                </label>

                <label>
                    <span>환산수량 *</span>

                    <input
                        type="number"
                        name="conversionQty"
                        min="0.001"
                        step="0.001"
                        value={form.conversionQty}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="예: 1"
                    />
                </label>

                <label>
                    <span>단위구분</span>

                    <select
                        name="isBaseYn"
                        value={form.isBaseYn}
                        onChange={onChange}
                        disabled={saving}
                    >
                        <option value="N">
                            환산단위
                        </option>

                        <option value="Y">
                            기준단위
                        </option>
                    </select>
                </label>
            </div>

            <div className="product-unit-form-actions">
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

export default ProductUnitForm;