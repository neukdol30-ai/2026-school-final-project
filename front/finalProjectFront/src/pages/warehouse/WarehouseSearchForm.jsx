function WarehouseSearchForm({
                                 filters,
                                 loading,
                                 onChange,
                                 onSearch,
                                 onReset,
                             }) {
    return (
        <form
            className="warehouse-search-form"
            onSubmit={onSearch}
        >
            <div className="warehouse-search-fields">
                <label>
                    <span>창고 검색</span>

                    <input
                        type="text"
                        name="keyword"
                        value={filters.keyword}
                        onChange={onChange}
                        placeholder="창고코드 또는 창고명"
                    />
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

            <div className="warehouse-search-actions">
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

export default WarehouseSearchForm;