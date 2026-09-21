function UnitSearchForm({
                            filters,
                            loading,
                            onChange,
                            onSearch,
                            onReset,
                        }) {
    return (
        <form
            className="unit-search-form"
            onSubmit={onSearch}
        >
            <div className="unit-search-fields">
                <label>
                    <span>단위 검색</span>

                    <input
                        type="text"
                        name="keyword"
                        value={filters.keyword}
                        onChange={onChange}
                        placeholder="단위코드 또는 단위명"
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

            <div className="unit-search-actions">
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

export default UnitSearchForm;