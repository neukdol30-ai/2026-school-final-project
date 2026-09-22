import { useEffect, useMemo, useRef, useState } from "react";
import {
  useNavigate,
  useParams,
  useSearchParams,
} from "react-router-dom";
import {
  requestCancelInbound,
  requestConfirmInbound,
  requestCreateInbound,
  requestInboundDetail,
  requestInboundPurchaseOrderItems,
  requestInboundPurchaseOrders,
  requestUpdateInboundItems,
} from "../../api/inboundApi.js";
import "./InboundEditPage.css";

// Backend 날짜 정책과 동일하게 한국시간을 기준으로 오늘 날짜를 계산한다.
const KOREA_TIME_ZONE = "Asia/Seoul";

const RECEIPT_STATUS_LABELS = {
  NOT_RECEIVED: "미입고",
  PARTIAL: "부분입고",
  RECEIVED: "입고완료",
  CLOSED: "잔량마감",
};

const INBOUND_STATUS_LABELS = {
  DRAFT: "작성중",
  CONFIRMED: "확정",
  CANCELLED: "취소",
};

// React 화면 안에서 LOT 입력행을 구분하기 위한 로컬 숫자 키다. crypto.randomUUID()를 사용하지 않는다.
let lotRowSequence = 1;

// 새 LOT 입력행 또는 서버에서 조회한 LOT를 화면 입력형식으로 바꾼다.
function createLotRow(lot = {}) {
  const savedId = lot.inboundItemLotId ?? lot.lotId;
  const rowId = savedId
    ? `saved-lot-${savedId}-${lotRowSequence++}`
    : `new-lot-${lotRowSequence++}`;

  return {
    rowId,
    lotNo: lot.lotNo ?? "",
    manufactureDate: lot.manufactureDate ?? "",
    expiryDate: lot.expiryDate ?? "",
    baseLotQty:
      lot.baseLotQty === null || lot.baseLotQty === undefined
        ? ""
        : String(lot.baseLotQty),
  };
}

function createEditableItemFromTarget(item) {
  return {
    purchaseOrderItemId: item.purchaseOrderItemId,
    productId: item.productId,
    productCode: item.productCode,
    productName: item.productName,
    lotManagedYn: item.lotManagedYn,
    productUnitId: item.productUnitId,
    unitCode: item.unitCode,
    unitName: item.unitName,
    orderedQty: item.orderedQty,
    conversionQty: Number(item.conversionQty),
    remainingBaseQty: Number(item.remainingBaseQty),
    receivedQty: "",
    lots: item.lotManagedYn === "Y" ? [createLotRow()] : [],
  };
}

function createEditableItemFromDetail(item) {
  return {
    purchaseOrderItemId: item.purchaseOrderItemId,
    productId: item.productId,
    productCode: item.productCode,
    productName: item.productName,
    lotManagedYn: item.lotManagedYn,
    productUnitId: item.productUnitId,
    unitCode: item.unitCode,
    unitName: item.unitName,
    orderedQty: null,
    conversionQty: Number(item.conversionQty),
    remainingBaseQty: Number(item.remainingBaseQty),
    receivedQty:
      item.receivedQty === null || item.receivedQty === undefined
        ? ""
        : String(item.receivedQty),
    lots:
      item.lotManagedYn === "Y"
        ? (item.lots ?? []).length > 0
          ? item.lots.map((lot) => createLotRow(lot))
          : [createLotRow()]
        : [],
  };
}

// 현재 발주 잔량 정보와 이미 저장된 작성중 입고 품목을 합쳐 이어쓰기 화면을 만든다.
function mergeTargetAndSavedItems(targetItems, savedItems) {
  const savedItemsByPurchaseOrderItemId = new Map(
    (savedItems ?? []).map((item) => [item.purchaseOrderItemId, item]),
  );

  return targetItems.map((targetItem) => {
    const savedItem = savedItemsByPurchaseOrderItemId.get(
      targetItem.purchaseOrderItemId,
    );

    if (!savedItem) {
      return createEditableItemFromTarget(targetItem);
    }

    return {
      ...createEditableItemFromDetail(savedItem),
      orderedQty: targetItem.orderedQty,
      remainingBaseQty: Number(targetItem.remainingBaseQty),
    };
  });
}

function getKstTodayString() {
  const parts = new Intl.DateTimeFormat("en-US", {
    timeZone: KOREA_TIME_ZONE,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(new Date());

  const year = parts.find((part) => part.type === "year")?.value;
  const month = parts.find((part) => part.type === "month")?.value;
  const day = parts.find((part) => part.type === "day")?.value;

  return `${year}-${month}-${day}`;
}

function roundQuantity(value) {
  return Math.round((value + Number.EPSILON) * 1000) / 1000;
}

function calculateBaseReceivedQty(receivedQty, conversionQty) {
  const numericReceivedQty = Number(receivedQty);
  const numericConversionQty = Number(conversionQty);

  if (
    !Number.isFinite(numericReceivedQty) ||
    numericReceivedQty <= 0 ||
    !Number.isFinite(numericConversionQty) ||
    numericConversionQty <= 0
  ) {
    return 0;
  }

  return roundQuantity(numericReceivedQty * numericConversionQty);
}

function formatQuantity(quantity) {
  if (quantity === null || quantity === undefined || quantity === "") {
    return "-";
  }

  const numericQuantity = Number(quantity);

  if (!Number.isFinite(numericQuantity)) {
    return "-";
  }

  return numericQuantity.toLocaleString("ko-KR", {
    maximumFractionDigits: 3,
  });
}

function formatRemainingQuantity(baseQuantity, conversionQty, unitCode) {
  const numericBaseQuantity = Number(baseQuantity);
  const numericConversionQty = Number(conversionQty);

  if (
    !Number.isFinite(numericBaseQuantity) ||
    !Number.isFinite(numericConversionQty) ||
    numericConversionQty <= 0
  ) {
    return `환산 수량 ${formatQuantity(baseQuantity)}`;
  }

  const unitQuantity = roundQuantity(numericBaseQuantity / numericConversionQty);

  return `${formatQuantity(unitQuantity)} ${unitCode ?? ""} (환산 수량 ${formatQuantity(numericBaseQuantity)})`;
}

// 저장 여부는 사용자가 입력한 값으로 비교한다. 화면용 LOT 행 키는 비교에서 제외한다.
// 조회 완료 또는 품목 PUT 성공 때만 기준값을 갱신하여, 저장 실패 시 변경 경고를 유지한다.
function createFormSnapshot(form, items) {
  return JSON.stringify({
    inboundDate: form.inboundDate,
    memo: form.memo,
    items: items.map((item) => ({
      purchaseOrderItemId: item.purchaseOrderItemId,
      receivedQty: item.receivedQty,
      lots: item.lots.map((lot) => ({
        lotNo: lot.lotNo,
        manufactureDate: lot.manufactureDate,
        expiryDate: lot.expiryDate,
        baseLotQty: lot.baseLotQty,
      })),
    })),
  });
}

function hasAtMostThreeDecimalPlaces(value) {
  const normalizedValue = String(value).trim();

  if (!normalizedValue) {
    return false;
  }

  const decimalPart = normalizedValue.split(".")[1];
  return !decimalPart || decimalPart.length <= 3;
}

function InboundEditPage() {
  const navigate = useNavigate();
  const { inboundId: routeInboundId } = useParams();
  const [searchParams] = useSearchParams();

  const kstToday = getKstTodayString();
  const numericRouteInboundId = Number(routeInboundId);
  const hasRouteInboundId =
    Number.isInteger(numericRouteInboundId) && numericRouteInboundId > 0;

  const [purchaseOrder, setPurchaseOrder] = useState(null);
  const [inbound, setInbound] = useState(null);
  const [createdInboundId, setCreatedInboundId] = useState(null);
  // ref는 렌더링을 기다리지 않고 생성 ID와 처리 중 여부를 기억한다.
  const createdInboundIdRef = useRef(null);
  const actionInProgressRef = useRef(false);
  const [savedSnapshot, setSavedSnapshot] = useState(null);
  const [pageReady, setPageReady] = useState(false);
  const [editableItems, setEditableItems] = useState([]);
  const [form, setForm] = useState({
    inboundDate: kstToday,
    memo: "",
  });

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [actionMessage, setActionMessage] = useState("");

  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [cancelReason, setCancelReason] = useState("");

  const activeInboundId = hasRouteInboundId
    ? numericRouteInboundId
    : createdInboundId;
  const inboundStatus = inbound?.status ?? (createdInboundId ? "DRAFT" : null);
  const isDraft = inboundStatus === "DRAFT";
  const isReadOnly = Boolean(activeInboundId) && !isDraft;
  const headerLocked = Boolean(activeInboundId);
  const currentSnapshot = createFormSnapshot(form, editableItems);
  const hasUnsavedChanges =
    !isReadOnly && savedSnapshot !== null && currentSnapshot !== savedSnapshot;

  const savedItemCount = useMemo(
    () => editableItems.filter((item) => Number(item.receivedQty) > 0).length,
    [editableItems],
  );

  // URL이 신규 등록인지 기존 입고서인지에 따라 필요한 서버 데이터를 한 번에 불러온다.
  useEffect(() => {
    // 최초 POST 직후 주소만 기존 입고서 경로로 바꾼 경우에는 다시 조회하지 않는다.
    // 아직 PUT 중인 입력값을 서버의 빈 품목 목록으로 덮어쓰지 않기 위한 분기다.
    if (
      hasRouteInboundId &&
      numericRouteInboundId === createdInboundIdRef.current
    ) {
      return;
    }

    let cancelled = false;

    async function loadPage() {
      setLoading(true);
      setPageReady(false);
      setSavedSnapshot(null);
      createdInboundIdRef.current = null;
      setErrorMessage("");
      setActionMessage("");

      try {
        const orders = await requestInboundPurchaseOrders();

        if (cancelled) {
          return;
        }

        if (hasRouteInboundId) {
          const detail = await requestInboundDetail(numericRouteInboundId);

          if (cancelled) {
            return;
          }

          setInbound(detail);
          setCreatedInboundId(null);
          const loadedForm = {
            inboundDate: detail.inboundDate ?? "",
            memo: detail.memo ?? "",
          };
          setForm(loadedForm);

          const matchedOrder = orders.find(
            (order) =>
              Number(order.purchaseOrderId) === Number(detail.purchaseOrderId),
          );

          setPurchaseOrder(
            matchedOrder ?? {
              purchaseOrderId: detail.purchaseOrderId,
              orderNo: detail.orderNo,
              supplierId: detail.supplierId,
              supplierName: detail.supplierName,
              warehouseId: detail.warehouseId,
              warehouseName: detail.warehouseName,
              orderDate: null,
              expectedDeliveryDate: null,
              receiptStatus: null,
              draftInboundId: detail.status === "DRAFT" ? detail.inboundId : null,
            },
          );

          if (detail.status === "DRAFT") {
            const targetItems = await requestInboundPurchaseOrderItems(
              detail.purchaseOrderId,
            );

            if (!cancelled) {
              const loadedItems = mergeTargetAndSavedItems(
                targetItems,
                detail.items ?? [],
              );
              setEditableItems(loadedItems);
              setSavedSnapshot(createFormSnapshot(loadedForm, loadedItems));
              setPageReady(true);
            }
          } else {
            const loadedItems = (detail.items ?? []).map((item) =>
              createEditableItemFromDetail(item),
            );
            setEditableItems(loadedItems);
            setSavedSnapshot(createFormSnapshot(loadedForm, loadedItems));
            setPageReady(true);
          }

          return;
        }

        setInbound(null);
        setCreatedInboundId(null);

        const requestedPurchaseOrderId = Number(
          searchParams.get("purchaseOrderId"),
        );

        if (
          Number.isInteger(requestedPurchaseOrderId) &&
          requestedPurchaseOrderId > 0
        ) {
          const requestedOrder = orders.find(
            (order) =>
              Number(order.purchaseOrderId) === requestedPurchaseOrderId,
          );

          if (!requestedOrder) {
            throw new Error("현재 입고 처리할 수 있는 발주를 찾을 수 없습니다.");
          }

          if (requestedOrder.draftInboundId) {
            navigate(`/inbounds/${requestedOrder.draftInboundId}`, {
              replace: true,
            });
            return;
          }

          const targetItems = await requestInboundPurchaseOrderItems(
            requestedPurchaseOrderId,
          );

          if (!cancelled) {
            const loadedForm = { inboundDate: kstToday, memo: "" };
            const loadedItems = targetItems.map(createEditableItemFromTarget);
            setPurchaseOrder(requestedOrder);
            setForm(loadedForm);
            setEditableItems(loadedItems);
            setSavedSnapshot(createFormSnapshot(loadedForm, loadedItems));
            setPageReady(true);
          }

          return;
        }

        setPurchaseOrder(null);
        setEditableItems([]);
        throw new Error("입고 관리 목록에서 입고할 발주의 ‘입고서 작성’을 선택해 주세요.");
      } catch (error) {
        if (!cancelled) {
          setErrorMessage(
            error instanceof Error
              ? error.message
              : "입고서 작성 정보를 불러오지 못했습니다.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadPage();

    return () => {
      cancelled = true;
    };
  }, [
    hasRouteInboundId,
    kstToday,
    navigate,
    numericRouteInboundId,
    searchParams,
  ]);

  // 새로고침·탭 닫기에도 미저장 입력을 보호한다. 이때의 문구는 브라우저가 정한다.
  useEffect(() => {
    if (!hasUnsavedChanges) {
      return;
    }

    function handleBeforeUnload(event) {
      event.preventDefault();
      event.returnValue = "";
    }

    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => window.removeEventListener("beforeunload", handleBeforeUnload);
  }, [hasUnsavedChanges]);

  function handleBackToList() {
    if (actionInProgressRef.current) {
      return;
    }

    if (
      hasUnsavedChanges &&
      !window.confirm("저장하지 않은 내용이 있습니다.\n저장하지 않고 나가시겠습니까?")
    ) {
      return;
    }

    navigate("/inbounds");
  }

  function handleFormChange(event) {
    const { name, value } = event.target;

    if (headerLocked) {
      return;
    }

    setErrorMessage("");
    setActionMessage("");
    setForm((previousForm) => ({
      ...previousForm,
      [name]: value,
    }));
  }

  function handleReceivedQtyChange(itemIndex, value) {
    setErrorMessage("");
    setActionMessage("");

    setEditableItems((previousItems) =>
      previousItems.map((item, index) => {
        if (index !== itemIndex) {
          return item;
        }

        if (item.lotManagedYn !== "Y" || item.lots.length !== 1) {
          return {
            ...item,
            receivedQty: value,
          };
        }

        const baseReceivedQty = calculateBaseReceivedQty(
          value,
          item.conversionQty,
        );

        return {
          ...item,
          receivedQty: value,
          lots: item.lots.map((lot) => ({
            ...lot,
            baseLotQty: baseReceivedQty > 0 ? String(baseReceivedQty) : "",
          })),
        };
      }),
    );
  }

  function handleLotChange(itemIndex, lotRowId, field, value) {
    setErrorMessage("");
    setActionMessage("");

    setEditableItems((previousItems) =>
      previousItems.map((item, index) => {
        if (index !== itemIndex) {
          return item;
        }

        return {
          ...item,
          lots: item.lots.map((lot) =>
            lot.rowId === lotRowId
              ? {
                  ...lot,
                  [field]: value,
                }
              : lot,
          ),
        };
      }),
    );
  }

  function handleAddLot(itemIndex) {
    setErrorMessage("");
    setActionMessage("");

    setEditableItems((previousItems) =>
      previousItems.map((item, index) =>
        index === itemIndex
          ? {
              ...item,
              lots: [...item.lots, createLotRow()],
            }
          : item,
      ),
    );
  }

  function handleRemoveLot(itemIndex, lotRowId) {
    setErrorMessage("");
    setActionMessage("");

    setEditableItems((previousItems) =>
      previousItems.map((item, index) => {
        if (index !== itemIndex || item.lots.length <= 1) {
          return item;
        }

        const remainingLots = item.lots.filter(
          (lot) => lot.rowId !== lotRowId,
        );

        return {
          ...item,
          lots: remainingLots,
        };
      }),
    );
  }

  function validateHeader() {
    if (!purchaseOrder) {
      throw new Error("입고할 발주를 선택해 주세요.");
    }

    if (!form.inboundDate) {
      throw new Error("입고일을 입력해 주세요.");
    }

    if (purchaseOrder.orderDate && form.inboundDate < purchaseOrder.orderDate) {
      throw new Error("입고일은 발주일보다 빠를 수 없습니다.");
    }

    if (form.inboundDate > kstToday) {
      throw new Error("입고일은 한국시간 기준 오늘 이후로 입력할 수 없습니다.");
    }

    if (form.memo.length > 500) {
      throw new Error("입고 메모는 500자 이하여야 합니다.");
    }
  }

  // 서버에 보내기 전에 수량, 소수점, LOT 번호, LOT 합계, 날짜를 Front에서 먼저 검증한다.
  function buildSaveItems() {
    const itemsToSave = editableItems.filter((item) => {
      const receivedQtyText = String(item.receivedQty).trim();

      if (receivedQtyText === "") {
        return false;
      }

      return Number(receivedQtyText) !== 0;
    });

    // 서버는 빈 목록을 전체 삭제로 처리하므로 임시저장·확정 모두 전송 전에 차단한다.
    if (itemsToSave.length === 0) {
      throw new Error("입고수량을 1건 이상 입력해 주세요.");
    }

    for (const item of itemsToSave) {
      const receivedQty = Number(item.receivedQty);
      const rawBaseReceivedQty = receivedQty * Number(item.conversionQty);
      const baseReceivedQty = calculateBaseReceivedQty(
        receivedQty,
        item.conversionQty,
      );

      if (!Number.isFinite(receivedQty) || receivedQty <= 0) {
        throw new Error(`${item.productName}의 입고수량은 0보다 커야 합니다.`);
      }

      if (!hasAtMostThreeDecimalPlaces(item.receivedQty)) {
        throw new Error(
          `${item.productName}의 입고수량은 소수점 셋째 자리까지만 입력할 수 있습니다.`,
        );
      }

      if (Math.abs(rawBaseReceivedQty - baseReceivedQty) > 0.0000001) {
        throw new Error(
          `${item.productName}의 입고수량과 단위 환산 결과가 소수점 셋째 자리를 초과합니다.`,
        );
      }

      if (baseReceivedQty <= 0) {
        throw new Error(`${item.productName}의 환산 수량을 계산할 수 없습니다.`);
      }

      if (baseReceivedQty > Number(item.remainingBaseQty)) {
        throw new Error(
          `${item.productName}의 입고수량이 현재 남은 수량을 초과했습니다.`,
        );
      }

      if (item.lotManagedYn === "Y") {
        if (item.lots.length === 0) {
          throw new Error(`${item.productName}의 LOT 정보를 입력해 주세요.`);
        }

        let totalBaseLotQty = 0;
        const lotNoSet = new Set();

        for (const lot of item.lots) {
          const normalizedLotNo = lot.lotNo.trim();
          const baseLotQty = Number(lot.baseLotQty);

          if (!normalizedLotNo) {
            throw new Error(`${item.productName}의 LOT 번호를 입력해 주세요.`);
          }

          if (normalizedLotNo.length > 50) {
            throw new Error(`${item.productName}의 LOT 번호는 50자 이하여야 합니다.`);
          }

          if (lotNoSet.has(normalizedLotNo)) {
            throw new Error(
              `${item.productName}에 같은 LOT 번호를 중복 입력할 수 없습니다.`,
            );
          }

          lotNoSet.add(normalizedLotNo);

          if (!Number.isFinite(baseLotQty) || baseLotQty <= 0) {
            throw new Error(`${item.productName}의 LOT 수량을 입력해 주세요.`);
          }

          if (!hasAtMostThreeDecimalPlaces(lot.baseLotQty)) {
            throw new Error(
              `${item.productName}의 LOT 수량은 소수점 셋째 자리까지만 입력할 수 있습니다.`,
            );
          }

          if (
            lot.manufactureDate &&
            lot.expiryDate &&
            lot.manufactureDate > lot.expiryDate
          ) {
            throw new Error(
              `${item.productName}의 제조일은 소비기한보다 늦을 수 없습니다.`,
            );
          }

          totalBaseLotQty += baseLotQty;
        }

        // 입고 단위 수량에 환산값을 곱한 결과와 LOT별 수량 합계가 같아야 한다.
        // 제조일·소비기한은 서버 정책대로 선택 입력이며, 두 날짜가 있을 때만 순서를 검사한다.
        if (roundQuantity(totalBaseLotQty) !== baseReceivedQty) {
          throw new Error(
            `${item.productName}의 LOT 수량 합계는 이번 입고의 환산 수량 ${formatQuantity(baseReceivedQty)}와 같아야 합니다.`,
          );
        }
      }
    }

    return itemsToSave.map((item) => ({
      purchaseOrderItemId: item.purchaseOrderItemId,
      receivedQty: Number(item.receivedQty),
      lots:
        item.lotManagedYn === "Y"
          ? item.lots.map((lot) => ({
              lotNo: lot.lotNo.trim(),
              baseLotQty: Number(lot.baseLotQty),
              manufactureDate: lot.manufactureDate || null,
              expiryDate: lot.expiryDate || null,
            }))
          : [],
    }));
  }

  // 신규 화면에서는 최초 1회만 Header를 POST로 만든다. 성공 즉시 ID와 URL을 보존해 이후에는 다시 POST하지 않는다.
  async function ensureInboundCreated() {
    if (createdInboundIdRef.current) {
      return createdInboundIdRef.current;
    }

    if (activeInboundId) {
      return activeInboundId;
    }

    validateHeader();

    const createdInbound = await requestCreateInbound({
      purchaseOrderId: purchaseOrder.purchaseOrderId,
      inboundDate: form.inboundDate,
      memo: form.memo.trim() || null,
    });

    const newInboundId = Number(createdInbound.inboundId);

    if (!Number.isInteger(newInboundId) || newInboundId <= 0) {
      throw new Error("생성된 입고 ID를 확인할 수 없습니다.");
    }

    // POST가 성공한 즉시 ID를 state와 ref에 보존한다. 이후 PUT·확정이 실패해도 지우지 않는다.
    // 재시도는 위 분기에서 같은 ID를 반환하므로 Header POST를 반복하지 않는다.
    createdInboundIdRef.current = newInboundId;
    setCreatedInboundId(newInboundId);
    setInbound({
      ...createdInbound,
      orderNo: purchaseOrder.orderNo,
      supplierId: purchaseOrder.supplierId,
      supplierName: purchaseOrder.supplierName,
      warehouseName: purchaseOrder.warehouseName,
      memo: form.memo.trim() || null,
      status: "DRAFT",
      items: [],
    });

    // 라우터에도 새 주소를 알린다. 조회 effect의 ID 확인으로 현재 입력값을 유지한다.
    // replace를 사용하여 뒤로 가기에 신규 작성 주소가 남지 않도록 한다.
    navigate(`/inbounds/${newInboundId}`, { replace: true });

    return newInboundId;
  }

  // 임시저장은 신규이면 Header 생성 후 품목 PUT, 기존 작성중이면 품목 PUT만 실행한다.
  async function handleSave() {
    if (actionInProgressRef.current || isReadOnly || !pageReady) {
      return;
    }

    actionInProgressRef.current = true;
    setSaving(true);
    setErrorMessage("");
    setActionMessage("");

    try {
      validateHeader();

      const itemsToSave = buildSaveItems();
      const savedInboundId = await ensureInboundCreated();

      await requestUpdateInboundItems(savedInboundId, itemsToSave);

      // 품목까지 저장된 경우에만 미저장 비교 기준을 갱신한다. 상태는 작성중으로 유지된다.
      setSavedSnapshot(currentSnapshot);
      setActionMessage("입고서가 임시저장되었습니다.");
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "입고 임시저장에 실패했습니다.",
      );
    } finally {
      actionInProgressRef.current = false;
      setSaving(false);
    }
  }

  // 입고 확정은 최신 입력값을 먼저 저장한 뒤 confirm API를 호출한다.
  async function handleConfirm() {
    if (actionInProgressRef.current || isReadOnly || !pageReady) {
      return;
    }

    setErrorMessage("");
    setActionMessage("");

    let itemsToSave;

    try {
      validateHeader();
      itemsToSave = buildSaveItems();
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "입고 입력값을 확인해 주세요.",
      );
      return;
    }

    const confirmed = window.confirm(
      "입고를 확정하면 발주 입고수량과 재고가 실제로 반영됩니다. 계속하시겠습니까?",
    );

    if (!confirmed) {
      return;
    }

    actionInProgressRef.current = true;
    setSaving(true);

    try {
      const savedInboundId = await ensureInboundCreated();

      await requestUpdateInboundItems(savedInboundId, itemsToSave);
      // 확정만 실패한 경우에도 품목 저장은 성공했으므로 입력값의 저장 기준을 갱신한다.
      setSavedSnapshot(currentSnapshot);
      await requestConfirmInbound(savedInboundId);

      // 성공 안내는 목록으로 전달한다. 목록은 다시 조회하여 변경된 발주 상태를 보여준다.
      navigate("/inbounds", {
        replace: true,
        state: { inboundSuccessMessage: "입고가 확정되었습니다." },
      });
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "입고 확정에 실패했습니다.",
      );
    } finally {
      actionInProgressRef.current = false;
      setSaving(false);
    }
  }

  function openCancelDialog() {
    if (!activeInboundId || !isDraft || saving) {
      return;
    }

    setCancelReason("");
    setErrorMessage("");
    setCancelDialogOpen(true);
  }

  function closeCancelDialog() {
    if (saving) {
      return;
    }

    setCancelDialogOpen(false);
    setCancelReason("");
    setErrorMessage("");
  }

  // 입고서 취소는 취소사유를 검증한 뒤 서버의 DRAFT(작성중) → CANCELLED(취소) 전이를 요청한다.
  async function handleCancelInbound() {
    if (actionInProgressRef.current || !activeInboundId || !isDraft) {
      return;
    }

    const trimmedReason = cancelReason.trim();

    if (!trimmedReason) {
      setErrorMessage("입고서 취소사유를 입력해 주세요.");
      return;
    }

    if (trimmedReason.length > 500) {
      setErrorMessage("입고서 취소사유는 500자 이하여야 합니다.");
      return;
    }

    const confirmed = window.confirm(
      "입고서를 취소하면 다시 수정하거나 확정할 수 없습니다. 정말 취소하시겠습니까?",
    );

    if (!confirmed) {
      return;
    }

    actionInProgressRef.current = true;
    setSaving(true);
    setErrorMessage("");
    setActionMessage("");

    try {
      await requestCancelInbound(activeInboundId, trimmedReason);
      // 실제 취소 성공 후에만 이동한다. 목록 재조회 결과에는 취소된 입고서가 작성중으로 잡히지 않는다.
      navigate("/inbounds", {
        replace: true,
        state: { inboundSuccessMessage: "입고서를 취소했습니다." },
      });
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "입고서 취소에 실패했습니다.",
      );
    } finally {
      actionInProgressRef.current = false;
      setSaving(false);
    }
  }

  const pageTitle = isReadOnly ? "입고 상세" : "입고서 작성";

  const pageDescription = isReadOnly
    ? "확정 또는 취소된 입고서의 상세정보를 확인합니다."
    : "입고수량과 LOT 정보를 확인하고 필요한 내용을 입력하세요.";

  return (
    <div className="page inbound-edit-page">
      <div className="inbound-edit-header">
        <div>
          <h1>{pageTitle}</h1>
          <p>{pageDescription}</p>
        </div>

        <button
          type="button"
          className="inbound-secondary-button"
          onClick={handleBackToList}
          disabled={saving}
        >
          목록으로
        </button>
      </div>

      {loading && (
        <div className="content-panel inbound-edit-message">
          입고 정보를 불러오는 중입니다.
        </div>
      )}

      {errorMessage && !cancelDialogOpen && (
        <div className="content-panel inbound-edit-message error" role="alert">
          {errorMessage}
        </div>
      )}

      {actionMessage && (
        <div className="content-panel inbound-edit-message success" role="status">
          {actionMessage}
        </div>
      )}

      {!loading && pageReady && (
        <>
          <section className="content-panel inbound-edit-section">
            <div className="inbound-section-title-row">
              <div>
                <h2>발주 정보</h2>
              </div>

              {inboundStatus && (
                <span className={`inbound-status-badge ${inboundStatus.toLowerCase()}`}>
                  {INBOUND_STATUS_LABELS[inboundStatus] ?? "상태 확인 필요"}
                </span>
              )}
            </div>

            {purchaseOrder && (
              <div className="inbound-order-info-grid">
                <div>
                  <span>발주번호</span>
                  <strong>{purchaseOrder.orderNo ?? inbound?.orderNo ?? "-"}</strong>
                </div>
                <div>
                  <span>공급업체</span>
                  <strong>{purchaseOrder.supplierName}</strong>
                </div>
                <div>
                  <span>입고창고</span>
                  <strong>{purchaseOrder.warehouseName}</strong>
                </div>
                <div>
                  <span>발주일</span>
                  <strong>{purchaseOrder.orderDate ?? "-"}</strong>
                </div>
                <div>
                  <span>납품희망일</span>
                  <strong>{purchaseOrder.expectedDeliveryDate ?? "-"}</strong>
                </div>
                <div>
                  <span>발주 입고상태</span>
                  <strong>
                    {RECEIPT_STATUS_LABELS[purchaseOrder.receiptStatus] ??
                      "-"}
                  </strong>
                </div>
                {activeInboundId && (
                  <div>
                    <span>입고번호</span>
                    <strong>{inbound?.inboundNo ?? "생성됨"}</strong>
                  </div>
                )}
              </div>
            )}
          </section>

          {purchaseOrder && (
            <section className="content-panel inbound-edit-section">
              <div className="inbound-section-title-row">
                <div>
                  <h2>입고 기본정보</h2>
                </div>
              </div>

              <div className="inbound-basic-form-grid">
                <label>
                  <span>입고일</span>
                  <input
                    type="date"
                    lang="en-CA"
                    name="inboundDate"
                    value={form.inboundDate}
                    min={purchaseOrder.orderDate ?? undefined}
                    max={kstToday}
                    onChange={handleFormChange}
                    disabled={headerLocked || saving || isReadOnly}
                  />
                </label>

                <label className="inbound-memo-field">
                  <span>입고 메모</span>
                  <textarea
                    name="memo"
                    value={form.memo}
                    maxLength="500"
                    onChange={handleFormChange}
                    disabled={headerLocked || saving || isReadOnly}
                    placeholder="입고 관련 메모를 입력하세요."
                  />
                  {!headerLocked && <small>{form.memo.length}/500</small>}
                </label>
              </div>

              {headerLocked && isDraft && (
                <div className="inbound-lock-notice">
                  입고일과 메모는 최초 저장 후 변경할 수 없습니다. 잘못 입력했다면 입고서를 취소하고 다시 등록하세요.
                </div>
              )}
            </section>
          )}

          {purchaseOrder && (
            <section className="content-panel inbound-edit-section">
              <div className="inbound-section-title-row">
                <div>
                  <h2>입고 품목</h2>
                  <p>이번에 실제로 입고할 수량과 LOT 정보를 입력합니다.</p>
                </div>

                {isDraft && <span>입력 품목 {savedItemCount}건</span>}
              </div>

              {editableItems.length === 0 ? (
                <div className="inbound-empty-items">
                  현재 입력할 수 있는 입고 품목이 없습니다.
                </div>
              ) : (
                <div className="inbound-item-list">
                  {editableItems.map((item, itemIndex) => {
                    const baseReceivedQty = calculateBaseReceivedQty(
                      item.receivedQty,
                      item.conversionQty,
                    );

                    return (
                      <article
                        key={item.purchaseOrderItemId}
                        className="inbound-item-card"
                      >
                        <div className="inbound-item-card-header">
                          <div>
                            <strong>{item.productName}</strong>
                            <span>{item.productCode}</span>
                          </div>

                          <span className="inbound-lot-policy">
                            {item.lotManagedYn === "Y"
                              ? "LOT 입력 필요"
                              : "LOT 입력 없음"}
                          </span>
                        </div>

                        <div className="inbound-item-summary-grid">
                          <div>
                            <span>입고 단위</span>
                            <strong>
                              {item.unitName} ({item.unitCode})
                            </strong>
                          </div>

                          <div>
                            <span>단위 환산</span>
                            <strong>
                              1 {item.unitCode} = 환산 수량 {formatQuantity(item.conversionQty)}
                            </strong>
                          </div>

                          <div>
                            <span>남은 입고 가능 수량</span>
                            <strong>
                              {formatRemainingQuantity(
                                item.remainingBaseQty,
                                item.conversionQty,
                                item.unitCode,
                              )}
                            </strong>
                          </div>

                          <div>
                            <span>이번 입고의 환산 수량</span>
                            <strong>
                              {item.receivedQty === ""
                                ? "-"
                                : formatQuantity(baseReceivedQty)}
                            </strong>
                          </div>
                        </div>

                        <label className="inbound-received-qty-field">
                          <span>이번 입고수량 ({item.unitCode})</span>
                          <input
                            type="number"
                            min="0.001"
                            step="0.001"
                            value={item.receivedQty}
                            onChange={(event) =>
                              handleReceivedQtyChange(
                                itemIndex,
                                event.target.value,
                              )
                            }
                            disabled={isReadOnly || saving}
                            placeholder="입고수량 입력"
                          />
                        </label>

                        {item.lotManagedYn === "Y" && (
                          <div className="inbound-lot-section">
                            <div className="inbound-lot-section-header">
                              <div>
                                <strong>LOT 정보</strong>
                                <span>상품 또는 박스에 표시된 LOT 번호를 입력하세요.</span>
                                <span>LOT별 수량은 위 환산 수량에 맞춰 입력하세요. 합계가 같아야 합니다.</span>
                              </div>

                              {!isReadOnly && (
                                <button
                                  type="button"
                                  onClick={() => handleAddLot(itemIndex)}
                                  disabled={saving}
                                >
                                  LOT 추가
                                </button>
                              )}
                            </div>

                            {item.lots.map((lot) => (
                              <div key={lot.rowId} className="inbound-lot-row">
                                <label>
                                  <span>LOT 번호</span>
                                  <input
                                    type="text"
                                    value={lot.lotNo}
                                    maxLength="50"
                                    onChange={(event) =>
                                      handleLotChange(
                                        itemIndex,
                                        lot.rowId,
                                        "lotNo",
                                        event.target.value,
                                      )
                                    }
                                    disabled={isReadOnly || saving}
                                  />
                                </label>

                                <label>
                                  <span>제조일</span>
                                  <input
                                    type="date"
                                    lang="en-CA"
                                    value={lot.manufactureDate}
                                    onChange={(event) =>
                                      handleLotChange(
                                        itemIndex,
                                        lot.rowId,
                                        "manufactureDate",
                                        event.target.value,
                                      )
                                    }
                                    disabled={isReadOnly || saving}
                                  />
                                </label>

                                <label>
                                  <span>소비기한</span>
                                  <input
                                    type="date"
                                    lang="en-CA"
                                    value={lot.expiryDate}
                                    onChange={(event) =>
                                      handleLotChange(
                                        itemIndex,
                                        lot.rowId,
                                        "expiryDate",
                                        event.target.value,
                                      )
                                    }
                                    disabled={isReadOnly || saving}
                                  />
                                </label>

                                <label>
                                  <span>LOT별 수량</span>
                                  <input
                                    type="number"
                                    min="0.001"
                                    step="0.001"
                                    value={lot.baseLotQty}
                                    onChange={(event) =>
                                      handleLotChange(
                                        itemIndex,
                                        lot.rowId,
                                        "baseLotQty",
                                        event.target.value,
                                      )
                                    }
                                    disabled={isReadOnly || saving}
                                  />
                                </label>

                                {!isReadOnly && (
                                  <button
                                    type="button"
                                    className="inbound-lot-remove-button"
                                    onClick={() =>
                                      handleRemoveLot(itemIndex, lot.rowId)
                                    }
                                    disabled={saving || item.lots.length <= 1}
                                    title={
                                      item.lots.length <= 1
                                        ? "LOT 입력행은 최소 1개가 필요합니다."
                                        : "LOT 삭제"
                                    }
                                  >
                                    삭제
                                  </button>
                                )}
                              </div>
                            ))}
                          </div>
                        )}
                      </article>
                    );
                  })}
                </div>
              )}
            </section>
          )}

          {purchaseOrder && !isReadOnly && (
            <div className="inbound-edit-actions">
              <div>
                {activeInboundId && isDraft && (
                  <button
                    type="button"
                    className="inbound-danger-button"
                    onClick={openCancelDialog}
                    disabled={saving}
                  >
                    입고서 취소
                  </button>
                )}
              </div>

              <div className="inbound-edit-action-group">
                <button
                  type="button"
                  className="inbound-save-button"
                  onClick={handleSave}
                  disabled={saving}
                >
                  {saving ? "처리 중..." : "임시저장"}
                </button>

                <button
                  type="button"
                  className="inbound-primary-button"
                  onClick={handleConfirm}
                  disabled={saving}
                >
                  입고 확정
                </button>
              </div>
            </div>
          )}
        </>
      )}

      {cancelDialogOpen && (
        <div className="inbound-modal-backdrop" role="presentation">
          <div
            className="inbound-cancel-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="inbound-cancel-title"
          >
            <h2 id="inbound-cancel-title">입고서 취소</h2>
            <p>
              입고서를 취소하면 다시 수정하거나 확정할 수 없습니다.
            </p>
            <p id="inbound-cancel-reason-label">취소사유를 입력해 주세요.</p>

            {errorMessage && (
              <div className="content-panel inbound-edit-message error" role="alert">
                {errorMessage}
              </div>
            )}

            <textarea
              aria-labelledby="inbound-cancel-reason-label"
              required
              value={cancelReason}
              maxLength="500"
              onChange={(event) => setCancelReason(event.target.value)}
              disabled={saving}
              placeholder="취소사유를 입력하세요."
            />

            <div className="inbound-cancel-count">{cancelReason.length}/500</div>

            <div className="inbound-cancel-modal-actions">
              <button
                type="button"
                className="inbound-secondary-button"
                onClick={closeCancelDialog}
                disabled={saving}
              >
                닫기
              </button>

              <button
                type="button"
                className="inbound-danger-button"
                onClick={handleCancelInbound}
                disabled={saving}
              >
                {saving ? "취소 처리 중..." : "입고서 취소"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default InboundEditPage;
