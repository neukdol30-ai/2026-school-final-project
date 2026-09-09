import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { confirmOutbound, getOutbounds } from "./js/outboundApi";
import "./css/OutboundListPage.css";
import OutboundConfirmButton from "./components/OutboundConfirmButton";
import OutboundStatusBadge from "./components/OutboundStatusBadge";

const INITIAL_FILTERS = {
  outboundNo: "",
  status: "",
};

function OutboundListPage() {
  const [outbounds, setOutbounds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [confirmingId, setConfirmingId] = useState(null);

  // 입력 중인 검색값
  const [filters, setFilters] = useState(INITIAL_FILTERS);

  // 실제 표에 적용된 검색값
  const [appliedFilters, setAppliedFilters] = useState(INITIAL_FILTERS);

  useEffect(() => {
    loadOutbounds();
  }, []);

  async function loadOutbounds() {
    try {
      setLoading(true);
      setError("");

      const result = await getOutbounds();

      setOutbounds(result);
    } catch (error) {
      setError(error.message);
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

    // 백엔드 검색 API는 아직 없으므로,
    // 이미 불러온 출고 목록에서 프론트가 조건에 맞는 행만 보여 준다.
    setAppliedFilters(filters);
  }

  function handleReset() {
    setFilters(INITIAL_FILTERS);
    setAppliedFilters(INITIAL_FILTERS);
  }

  async function handleConfirm(outboundId) {
    const shouldConfirm = window.confirm("이 출고서를 확정하시겠습니까?");

    if (!shouldConfirm) {
      return;
    }

    try {
      setConfirmingId(outboundId);
      setError("");
      setSuccessMessage("");

      await confirmOutbound(outboundId);

      setSuccessMessage("출고서가 확정되었습니다.");

      // 확정된 최신 상태를 다시 받아 표를 갱신한다.
      await loadOutbounds();
    } catch (error) {
      setError(error.message);
    } finally {
      setConfirmingId(null);
    }
  }

  const filteredOutbounds = outbounds.filter((outbound) => {
    const matchesOutboundNo = outbound.outboundNo
      .toLowerCase()
      .includes(appliedFilters.outboundNo.trim().toLowerCase());

    const matchesStatus =
      !appliedFilters.status || outbound.status === appliedFilters.status;

    return matchesOutboundNo && matchesStatus;
  });

  return (
    <section className="page outbound-list-page">
      <div className="outbound-list-page-header">
        <div>
          <h1>출고 목록</h1>
          <p>등록된 출고서를 조회하고 작성중인 출고서를 확정합니다.</p>
        </div>

        <Link className="outbound-create-link" to="/outbounds/new">
          + 출고 등록
        </Link>
      </div>

      <form className="outbound-search-form" onSubmit={handleSearch}>
        <div className="outbound-search-fields">
          <label>
            <span>출고번호</span>
            <input
              type="text"
              name="outboundNo"
              value={filters.outboundNo}
              onChange={handleFilterChange}
              placeholder="출고번호 입력"
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
            </select>
          </label>
        </div>

        <div className="outbound-search-actions">
          <button type="submit" disabled={loading}>
            조회
          </button>

          <button type="button" onClick={handleReset} disabled={loading}>
            초기화
          </button>

          <button type="button" onClick={loadOutbounds} disabled={loading}>
            {loading ? "불러오는 중..." : "새로고침"}
          </button>
        </div>
      </form>

      {error && (
        <p className="outbound-list-message error" role="alert">
          {error}
        </p>
      )}

      {successMessage && (
        <p className="outbound-list-message success" role="status">
          {successMessage}
        </p>
      )}

      <section className="outbound-list-result">
        <p className="outbound-result-summary">
          조회 결과 {filteredOutbounds.length}건
        </p>

        {loading && (
          <p className="outbound-list-message">
            출고 목록을 불러오는 중입니다.
          </p>
        )}

        {!loading && !error && (
          <div className="outbound-table-wrap">
            <table className="outbound-table">
              <thead>
                <tr>
                  <th>출고번호</th>
                  <th>판매주문번호</th>
                  <th>출고 창고</th>
                  <th>상태</th>
                  <th>관리</th>
                </tr>
              </thead>

              <tbody>
                {filteredOutbounds.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="outbound-empty-row">
                      조건에 맞는 출고서가 없습니다.
                    </td>
                  </tr>
                ) : (
                  filteredOutbounds.map((outbound) => (
                    <tr key={outbound.outboundId}>
                      <td>
                        <Link
                          className="outbound-detail-link"
                          to={`/outbounds/${outbound.outboundId}`}
                        >
                          {outbound.outboundNo}
                        </Link>
                      </td>
                      <td>{outbound.salesOrderNo}</td>
                      <td>{outbound.warehouseName}</td>
                      <td>
                        <OutboundStatusBadge status={outbound.status} />
                      </td>
                      <td>
                        {outbound.status === "DRAFT" ? (
                          <OutboundConfirmButton
                            onConfirm={() => handleConfirm(outbound.outboundId)}
                            confirming={confirmingId === outbound.outboundId}
                          />
                        ) : (
                          "-"
                        )}
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

export default OutboundListPage;
