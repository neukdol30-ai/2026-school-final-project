function BusinessPartnerSearchForm({
                                       filters,
                                       loading,
                                       onChange,
                                       onSearch,
                                       onReset,
                                   }) {
    return (
        <form
            className="business-partner-search-form"
            onSubmit={onSearch}
        >
            <div className="business-partner-search-fields">
                <label>
                    <span>거래처 검색</span>

                    <input
                        type="text"
                        name="keyword"
                        value={filters.keyword}
                        onChange={onChange}
                        placeholder={
                            "거래처코드, 거래처명, 사업자번호"
                        }
                    />
                </label>

                <label>
                    <span>거래처 구분</span>

                    <select
                        name="partnerType"
                        value={filters.partnerType}
                        onChange={onChange}
                    >
                        <option value="">전체</option>
                        <option value="SUPPLIER">
                            공급업체
                        </option>
                        <option value="CUSTOMER">
                            판매처
                        </option>
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

            <div className="business-partner-search-actions">
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

export default BusinessPartnerSearchForm;