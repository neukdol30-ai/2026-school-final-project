// V25 제약조건과 기존 이력 저장 코드를 확인한 표시값이다. DB의 값 자체는 변경하지 않는다.
export const MOVEMENT_LABELS = {
  INBOUND: "입고",
  OUTBOUND: "출고",
  OUTBOUND_CANCEL: "출고취소",
  ADJUSTMENT: "재고실사",
  // DB에서 허용하는 과거 이력을 표시하기 위한 값이며, 현재 입고 취소가 이력을 새로 만들지는 않는다.
  INBOUND_CANCEL: "입고취소",
};

// 시스템·브라우저의 시간대가 달라도 한국시간의 오늘 날짜를 사용한다.
export function getRecentMonthRange(now = new Date()) {
  const parts = new Intl.DateTimeFormat("en-US", {
    timeZone: "Asia/Seoul",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(now);
  const getPart = (type) => parts.find((part) => part.type === type).value;
  const year = Number(getPart("year"));
  const month = Number(getPart("month"));
  const day = Number(getPart("day"));

  // 한 달 전의 같은 날짜를 사용하되 3월 31일 → 2월 말일처럼 없는 날짜는 말일로 맞춘다.
  // UTC 메서드는 여기서 달력 계산에만 사용한다. 한국의 오늘 날짜를 UTC로 바꾸는 것이 아니다.
  const previousMonthLastDay = new Date(Date.UTC(year, month - 1, 0));
  const previousDay = Math.min(day, previousMonthLastDay.getUTCDate());
  const pad = (value) => String(value).padStart(2, "0");
  const startDate = [
    previousMonthLastDay.getUTCFullYear(),
    pad(previousMonthLastDay.getUTCMonth() + 1),
    pad(previousDay),
  ].join("-");
  const endDate = `${year}-${pad(month)}-${pad(day)}`;

  return { startDate, endDate };
}

export function createInitialFilters() {
  return {
    warehouseKeyword: "",
    productKeyword: "",
    lotNo: "",
    includeZero: false,
    movementType: "",
    ...getRecentMonthRange(),
  };
}

// 날짜 문자열을 비교하기 전에 실제로 존재하는 YYYY-MM-DD인지 확인한다.
function isValidDate(value) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value) || value.startsWith("0000")) {
    return false;
  }
  const date = new Date(`${value}T00:00:00Z`);
  return Number.isFinite(date.getTime()) && date.toISOString().slice(0, 10) === value;
}

export function validateHistoryDates(filters) {
  if (!isValidDate(filters.startDate) || !isValidDate(filters.endDate)) {
    return "조회 시작일과 종료일을 올바르게 입력해 주세요.";
  }
  if (filters.startDate > filters.endDate) {
    return "조회 시작일은 종료일보다 늦을 수 없습니다.";
  }
  if (filters.endDate === "9999-12-31") {
    return "조회 종료일은 9999-12-30 이전 날짜로 입력해 주세요.";
  }
  return "";
}

// Oracle 수량은 정수 16자리까지 가능하므로 Number 변환 없이 문자열로 천 단위를 구분한다.
// JSON 숫자 응답이 이미 JS 안전 범위를 넘으면 정확한 숫자인 것처럼 표시하지 않는다.
export function formatQuantity(value, signed = false) {
  if (value === null || value === undefined || value === "") {
    return "-";
  }
  if (typeof value === "number" && (
    !Number.isFinite(value) || Math.abs(value) > Number.MAX_SAFE_INTEGER
  )) {
    return "표시 범위 초과";
  }

  const match = String(value).match(/^(-?)(\d+)(?:\.(\d+))?$/);
  if (!match) {
    return "-";
  }
  const [, negative, integer, fraction = ""] = match;
  const decimal = fraction.replace(/0+$/, "");
  const nonzero = /[1-9]/.test(integer + fraction);
  const sign = nonzero ? negative || (signed ? "+" : "") : "";
  return sign + integer.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
    + (decimal ? `.${decimal}` : "");
}

// 서버의 LocalDateTime은 이미 한국시간이다. new Date로 다시 해석하면 다른 시간대에서 시각이 바뀐다.
export function formatHistoryDateTime(value) {
  return value ? String(value).replace("T", " ").slice(0, 19) : "-";
}

export function formatBaseUnit(row) {
  return row.baseUnitCode || row.baseUnitName || "-";
}
