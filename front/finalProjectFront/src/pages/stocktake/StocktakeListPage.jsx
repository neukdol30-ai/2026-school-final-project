import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { getStocktakes } from "./js/stocktakeApi";
import "./css/StocktakeListPage.css";

const INITIAL_FILTERS = {
  stocktakeNo: "",
  status: "",
};

const STATUS_LABELS = {
  DRAFT: "작성중",
  CONFIRMED: "확정",
  CANCELLED: "취소",
};

function StocktakeListPage() {
  const [stocktakes, setStocktakes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filters, setFilters] = useState(INITIAL_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState(INITIAL_FILTERS);

  useEffect(() => {
    loadStocktakes();
  }, []);

  async function loadStocktakes() {
    try {
      setLoading(true);
      setError("");

      const result = await getStocktakes();

      setStocktakes(result);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  function handleFilterChange(event) {
    const { name, value } = event.target;

    setFilters((currentFilters) => ({
      ...currentFilters,
      [name]: value,
    }));
  }

  function handleSearch(event) {
    event.preventDefault();

    // 검색 API가 아직 없으므로, 이미 조회한 목록 안에서 화면 검색을 한다.
    setAppliedFilters(filters);
  }

  function handleReset() {
    setFilters(INITIAL_FILTERS);
    setAppliedFilters(INITIAL_FILTERS);
  }

  const filteredStocktakes = useMemo(
    () =>
      stocktakes.filter((stocktake) => {
        const stocktakeNo = stocktake.stocktakeNo ?? "";

        const matchesStocktakeNo = stocktakeNo
          .toLowerCase()
          .includes(appliedFilters.stocktakeNo.trim().toLowerCase());

        const matchesStatus =
          !appliedFilters.status || stocktake.status === appliedFilters.status;

        return matchesStocktakeNo && matchesStatus;
      }),
    [stocktakes, appliedFilters],
  );

  return (
    <section className="page stocktake-list-page">
      <div className="stocktake-list-page-header">
        <div>
          <h1>재고실사</h1>
          <p>창고의 실제 재고를 조사하고 실사 문서를 관리합니다.</p>
        </div>

        <Link className="stocktake-create-link" to="/stocktakes/new">
          + 재고실사 등록
        </Link>
      </div>

      <form className="stocktake-search-form" onSubmit={handleSearch}>
        <div className="stocktake-search-fields">
          <label>
            <span>실사번호 검색</span>
            <input
              type="text"
              name="stocktakeNo"
              value={filters.stocktakeNo}
              onChange={handleFilterChange}
              placeholder="전체 조회는 비워두세요"
            />
          </label>

          <label>
            <span>상태</span>
            <select
              name="status"
              value={filters.status}
              onChange={handleFilterChange}
            >
              <option value="">전체</option>
              <option value="DRAFT">작성중</option>
              <option value="CONFIRMED">확정</option>
              <option value="CANCELLED">취소</option>
            </select>
          </label>
        </div>

        <div className="stocktake-search-actions">
          <button type="submit" disabled={loading}>
            조회
          </button>
          <button type="button" onClick={handleReset} disabled={loading}>
            초기화
          </button>
          <button type="button" onClick={loadStocktakes} disabled={loading}>
            {loading ? "불러오는 중..." : "새로고침"}
          </button>
        </div>
      </form>

      {error && (
        <p className="stocktake-list-message error" role="alert">
          {error}
        </p>
      )}

      <section className="stocktake-list-result">
        <p className="stocktake-result-summary">
          조회 결과 {filteredStocktakes.length}건
        </p>

        {loading && (
          <p className="stocktake-list-message">
            재고실사 목록을 불러오는 중입니다.
          </p>
        )}

        {!loading && !error && (
          <div className="stocktake-table-wrap">
            <table className="stocktake-table">
              <thead>
                <tr>
                  <th>실사번호</th>
                  <th>실사 창고</th>
                  <th>실사일</th>
                  <th>상태</th>
                </tr>
              </thead>

              <tbody>
                {filteredStocktakes.length === 0 ? (
                  <tr>
                    <td colSpan="4" className="stocktake-empty-row">
                      등록된 재고실사 문서가 없습니다.
                    </td>
                  </tr>
                ) : (
                  filteredStocktakes.map((stocktake) => (
                    <tr key={stocktake.stocktakeId}>
                      <td>
                        <Link
                          className="stocktake-detail-link"
                          to={`/stocktakes/${stocktake.stocktakeId}`}
                        >
                          {stocktake.stocktakeNo}
                        </Link>
                      </td>
                      <td>{stocktake.warehouseName}</td>
                      <td>{stocktake.stocktakeDate}</td>
                      <td>
                        <span
                          className={`stocktake-status-badge ${stocktake.status?.toLowerCase()}`}
                        >
                          {STATUS_LABELS[stocktake.status] ?? stocktake.status}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </section>
  );
}

export default StocktakeListPage;
