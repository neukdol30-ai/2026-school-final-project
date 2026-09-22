import { useEffect, useState } from "react";
import {
  requestInventoryStocks,
  requestInventoryLots,
  requestInventoryHistory,
} from "../../api/inventoryApi.js";
import {
  MOVEMENT_LABELS,
  createInitialFilters,
  validateHistoryDates,
  formatQuantity,
  formatHistoryDateTime,
  formatBaseUnit,
} from "./inventoryUtils.js";
import "./InventoryPage.css";

// 탭 이름·요청 함수·표 제목을 한곳에 두어 선택한 화면과 API가 서로 어긋나지 않게 한다.
const TABS = [
  { id: "stocks", label: "재고 현황" },
  { id: "lots", label: "LOT별 재고" },
  { id: "history", label: "재고 이력" },
];
const REQUESTS = {
  stocks: requestInventoryStocks,
  lots: requestInventoryLots,
  history: requestInventoryHistory,
};
const COLUMNS = {
  stocks: ["창고", "상품코드", "상품명", "현재고", "단위"],
  lots: ["창고", "상품코드", "상품명", "LOT 번호", "제조일", "소비기한", "LOT 재고수량", "단위"],
  history: ["발생일시", "창고", "상품코드", "상품명", "LOT 번호", "변동유형", "증감수량", "단위", "업무번호"],
};

function InventoryPage() {
  const [activeTab, setActiveTab] = useState("stocks");

  // 마우스뿐 아니라 좌우 화살표와 Home/End 키로도 탭을 선택할 수 있다.
  function handleTabKeyDown(event, index) {
    let nextIndex;
    if (event.key === "ArrowRight") nextIndex = (index + 1) % TABS.length;
    else if (event.key === "ArrowLeft") nextIndex = (index + TABS.length - 1) % TABS.length;
    else if (event.key === "Home") nextIndex = 0;
    else if (event.key === "End") nextIndex = TABS.length - 1;
    else return;

    event.preventDefault();
    setActiveTab(TABS[nextIndex].id);
    document.getElementById(`inventory-tab-${TABS[nextIndex].id}`)?.focus();
  }

  return (
    <div className="page inventory-page">
      <div className="inventory-header">
        <h1>재고 / LOT</h1>
        <p>창고별 현재고, LOT별 재고와 재고 변동 내역을 확인하세요.</p>
      </div>

      {/* 기존 사이드바와 /inventory 주소는 유지하고 이 화면 안에서만 탭을 전환한다. */}
      <div className="inventory-tabs" role="tablist" aria-label="재고 조회 화면">
        {TABS.map((tab, index) => (
          <button
            key={tab.id}
            id={`inventory-tab-${tab.id}`}
            type="button"
            role="tab"
            aria-selected={activeTab === tab.id}
            aria-controls={`inventory-panel-${tab.id}`}
            tabIndex={activeTab === tab.id ? 0 : -1}
            onClick={() => setActiveTab(tab.id)}
            onKeyDown={(event) => handleTabKeyDown(event, index)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* 탭별 컴포넌트를 다시 만들면 이전 탭의 결과가 새 탭의 표로 보이지 않는다. */}
      <InventoryTab key={activeTab} tab={activeTab} />
    </div>
  );
}

function InventoryTab({ tab }) {
  // 입력 중인 조건과 실제 조회 조건을 분리하여, 글자를 입력할 때마다 요청하지 않는다.
  const [filters, setFilters] = useState(createInitialFilters);
  const [query, setQuery] = useState(filters);
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [validationError, setValidationError] = useState("");
  const isHistory = tab === "history";

  useEffect(() => {
    // 다른 검색 또는 탭 전환 시 이전 요청을 중단하여 늦은 응답이 최신 표를 덮지 않게 한다.
    const controller = new AbortController();
    let cancelled = false;

    async function loadRows() {
      try {
        // 선택한 탭 → 전용 GET API → 공통 ApiResponse의 data → rows 순서로 값이 전달된다.
        const data = await REQUESTS[tab](query, controller.signal);
        if (!cancelled) {
          setRows(data);
        }
      } catch (requestError) {
        if (!cancelled && requestError.name !== "AbortError") {
          setRows([]);
          setError(
            requestError instanceof Error
              ? requestError.message
              : "재고 정보를 불러오지 못했습니다.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadRows();
    return () => {
      cancelled = true;
      controller.abort();
    };
  }, [tab, query]);

  function handleFilterChange(event) {
    const { name, value, type, checked } = event.target;
    setFilters((previous) => ({
      ...previous,
      [name]: type === "checkbox" ? checked : value,
    }));
    setValidationError("");
  }

  function applySearch(nextFilters) {
    // 이력은 기간이 비어 있거나 역전되면 요청하지 않는다. Backend도 같은 조건을 검사한다.
    const message = isHistory ? validateHistoryDates(nextFilters) : "";
    setValidationError(message);
    if (message) {
      return;
    }

    setError("");
    setRows([]);
    setLoading(true);
    setQuery({ ...nextFilters });
  }

  function handleSearch(event) {
    event.preventDefault();
    applySearch(filters);
  }

  function handleReset() {
    // 초기화 시에도 한국시간의 최근 한 달을 다시 계산하며 전체 기간 조회로 바꾸지 않는다.
    const initialFilters = createInitialFilters();
    setFilters(initialFilters);
    applySearch(initialFilters);
  }

  return (
    <section
      id={`inventory-panel-${tab}`}
      role="tabpanel"
      aria-labelledby={`inventory-tab-${tab}`}
      className="inventory-panel"
      tabIndex={0}
    >
      {/* 기존 발주 목록과 같은 검색 폼·카드·테이블 배치를 사용한다. */}
      <form className="content-panel inventory-search-form" onSubmit={handleSearch}>
        <div className="inventory-search-fields">
          {isHistory && (
            <>
              <label>
                <span>시작일</span>
                <input
                  type="date"
                  name="startDate"
                  value={filters.startDate}
                  max={filters.endDate || undefined}
                  required
                  onChange={handleFilterChange}
                />
              </label>
              <label>
                <span>종료일</span>
                <input
                  type="date"
                  name="endDate"
                  value={filters.endDate}
                  min={filters.startDate || undefined}
                  required
                  onChange={handleFilterChange}
                />
              </label>
            </>
          )}

          <label>
            <span>창고</span>
            <input
              type="search"
              name="warehouseKeyword"
              value={filters.warehouseKeyword}
              maxLength={100}
              placeholder="창고코드 또는 창고명"
              onChange={handleFilterChange}
            />
          </label>
          <label>
            <span>상품</span>
            <input
              type="search"
              name="productKeyword"
              value={filters.productKeyword}
              maxLength={100}
              placeholder="상품코드 또는 상품명"
              onChange={handleFilterChange}
            />
          </label>

          {tab !== "stocks" && (
            <label>
              <span>LOT 번호</span>
              <input
                type="search"
                name="lotNo"
                value={filters.lotNo}
                maxLength={100}
                placeholder="LOT 번호 입력"
                onChange={handleFilterChange}
              />
            </label>
          )}

          {isHistory && (
            <label>
              <span>변동유형</span>
              <select
                name="movementType"
                value={filters.movementType}
                onChange={handleFilterChange}
              >
                <option value="">전체</option>
                {Object.entries(MOVEMENT_LABELS).map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </select>
            </label>
          )}
        </div>

        <div className="inventory-search-footer">
          {isHistory ? (
            <p>한국시간 기준이며 종료일 당일의 내역까지 조회합니다.</p>
          ) : (
            <label className="inventory-zero-filter">
              <input
                type="checkbox"
                name="includeZero"
                checked={filters.includeZero}
                onChange={handleFilterChange}
              />
              <span>재고 0 포함</span>
            </label>
          )}
          <div className="inventory-search-actions">
            <button type="button" onClick={handleReset}>초기화</button>
            <button type="submit" disabled={loading}>
              {loading ? "조회 중..." : "조회"}
            </button>
          </div>
        </div>
      </form>

      {validationError && <p className="inventory-message error" role="alert">{validationError}</p>}
      {error && <p className="inventory-message error" role="alert">{error}</p>}
      {loading && <p className="inventory-message" role="status">재고 정보를 불러오는 중입니다.</p>}

      {!loading && !error && (
        <div className="content-panel inventory-results">
          <div className="inventory-result-summary" role="status">
            <strong>조회 결과 {rows.length}건</strong>
            {/* 입력 중인 날짜가 아닌 실제 조회에 사용한 기간을 보여준다. */}
            {isHistory && <span>{query.startDate} ~ {query.endDate}</span>}
          </div>
          {rows.length === 0 ? (
            <p className="inventory-empty">검색조건에 맞는 {isHistory ? "재고 이력이" : "재고가"} 없습니다.</p>
          ) : (
            <div className="inventory-table-wrap">
              <table className="inventory-table">
                <caption className="inventory-screen-reader-only">
                  {TABS.find((item) => item.id === tab).label} 조회 결과
                </caption>
                <thead>
                  <tr>{COLUMNS[tab].map((column) => <th key={column} scope="col">{column}</th>)}</tr>
                </thead>
                <tbody>
                  {rows.map((row) => (
                    <InventoryRow
                      key={row.stockId ?? row.lotStockId ?? row.stockHistoryId}
                      row={row}
                      tab={tab}
                    />
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </section>
  );
}

// API의 ID는 React 행 구분에만 사용한다. 표에는 업무용 코드·이름·문서번호를 보여준다.
function InventoryRow({ row, tab }) {
  const isHistory = tab === "history";
  const changeClass = String(row.changeQty).startsWith("-") ? "decrease" : "increase";

  return (
    <tr>
      {isHistory && <td>{formatHistoryDateTime(row.createdAt)}</td>}
      <td>
        <span>{row.warehouseName ?? "-"}</span>
        <small>{row.warehouseCode}</small>
      </td>
      <td>{row.productCode ?? "-"}</td>
      <td>{row.productName ?? "-"}</td>
      {tab !== "stocks" && <td>{row.lotNo ?? "-"}</td>}
      {tab === "lots" && (
        <>
          <td>{row.manufactureDate ?? "-"}</td>
          <td>{row.expiryDate ?? "-"}</td>
        </>
      )}
      {isHistory && <td>{MOVEMENT_LABELS[row.movementType] ?? "기타"}</td>}
      <td className={`inventory-quantity ${isHistory ? changeClass : ""}`}>
        {/* 색상 외에도 +와 - 기호를 표시하여 증감 방향을 구분한다. */}
        {formatQuantity(isHistory ? row.changeQty : row.quantity, isHistory)}
      </td>
      <td title={row.baseUnitName || undefined}>{formatBaseUnit(row)}</td>
      {isHistory && (
        <td>
          {/* 문서가 없는 과거 데이터만 원본 sourceId로 대체한다. 번호를 새로 만들지 않는다. */}
          {row.sourceNo || (row.sourceId == null ? "-" : `문서 ID ${row.sourceId}`)}
        </td>
      )}
    </tr>
  );
}

export default InventoryPage;
