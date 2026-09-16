function ProductSearchForm({
                               filters,
                               loading,
                               onChange,
                               onSearch,
                               onReset,
                           }) {
    return (
        <form
            className="product-search-form"
            onSubmit={onSearch}
        >
            <div className="product-search-fields">
                <label>
                    <span>상품코드 / 상품명</span>
                    <input
                        type="text"
                        name="keyword"
                        value={filters.keyword}
                        onChange={onChange}
                        placeholder="검색어 입력"
                    />
                </label>

                <label>
                    <span>LOT 관리</span>
                    <select
                        name="lotManagedYn"
                        value={filters.lotManagedYn}
                        onChange={onChange}
                    >
                        <option value="">전체</option>
                        <option value="Y">사용</option>
                        <option value="N">미사용</option>
                    </select>
                </label>

                <label>
                    <span>과세구분</span>
                    <select
                        name="taxType"
                        value={filters.taxType}
                        onChange={onChange}
                    >
                        <option value="">전체</option>
                        <option value="TAXABLE">과세</option>
                        <option value="TAX_FREE">면세</option>
                    </select>
                </label>

                <label>
                    <span>보관유형</span>
                    <select
                        name="storageType"
                        value={filters.storageType}
                        onChange={onChange}
                    >
                        <option value="">전체</option>
                        <option value="AMBIENT">상온</option>
                        <option value="CHILLED">냉장</option>
                        <option value="FROZEN">냉동</option>
                    </select>
                </label>

                <label>
                    <span>사용상태</span>
                    <select
                        name="useYn"
                        value={filters.useYn}
                        onChange={onChange}
                    >
                        <option value="">전체</option>
                        <option value="Y">사용</option>
                        <option value="N">미사용</option>
                    </select>
                </label>
            </div>

            <div className="product-search-actions">
                <button
                    type="submit"
                    disabled={loading}
                >
                    {loading ? "조회 중..." : "조회"}
                </button>

                <button
                    type="button"
                    onClick={onReset}
                    disabled={loading}
                >
                    초기화
                </button>
            </div>
        </form>
    );
}

export default ProductSearchForm;