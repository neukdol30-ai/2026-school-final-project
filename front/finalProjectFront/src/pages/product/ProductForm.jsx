function ProductForm({
                         form,
                         saving,
                         editing,
                         onChange,
                         onSubmit,
                         onCancel,
                     }) {
    return (
        <form
            className="product-edit-panel"
            onSubmit={onSubmit}
        >
            <div className="product-edit-heading">
                <h2>
                    {editing ? "상품 수정" : "상품 등록"}
                </h2>

                <button
                    type="button"
                    onClick={onCancel}
                    disabled={saving}
                >
                    닫기
                </button>
            </div>

            <div className="product-edit-fields">
                <label>
                    <span>상품코드 *</span>

                    <input
                        type="text"
                        name="productCode"
                        maxLength="50"
                        value={form.productCode}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="상품코드 입력"
                    />
                </label>

                <label>
                    <span>상품명 *</span>

                    <input
                        type="text"
                        name="productName"
                        maxLength="100"
                        value={form.productName}
                        onChange={onChange}
                        disabled={saving}
                        placeholder="상품명 입력"
                    />
                </label>

                <label>
                    <span>LOT 관리</span>

                    <select
                        name="lotManagedYn"
                        value={form.lotManagedYn}
                        onChange={onChange}
                        disabled={saving}
                    >
                        <option value="N">미사용</option>
                        <option value="Y">사용</option>
                    </select>
                </label>

                <label>
                    <span>과세구분</span>

                    <select
                        name="taxType"
                        value={form.taxType}
                        onChange={onChange}
                        disabled={saving}
                    >
                        <option value="TAXABLE">
                            과세
                        </option>

                        <option value="TAX_FREE">
                            면세
                        </option>
                    </select>
                </label>

                <label>
                    <span>보관유형</span>

                    <select
                        name="storageType"
                        value={form.storageType}
                        onChange={onChange}
                        disabled={saving}
                    >
                        <option value="AMBIENT">
                            상온
                        </option>

                        <option value="CHILLED">
                            냉장
                        </option>

                        <option value="FROZEN">
                            냉동
                        </option>
                    </select>
                </label>
            </div>

            <div className="product-edit-actions">
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

export default ProductForm;