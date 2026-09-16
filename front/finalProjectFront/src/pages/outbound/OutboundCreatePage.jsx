import { useEffect, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import {
  createOutbound,
  getOutboundDetail,
  getOutboundLots,
  getOutboundWarehouses,
  updateOutbound,
} from "./js/outboundApi";
import "./css/OutboundCreatePage.css";
import OutboundBasicInfoFields from "./components/OutboundBasicInfoFields";
import OutboundItemList from "./components/OutboundItemList";
import OutboundCreateResult from "./components/OutboundCreateResult";
import {
  getSalesOrderDetail,
  getSalesOrders,
} from "../sales/js/salesOrderApi";

function OutboundCreatePage() {
  const [searchParams] = useSearchParams();
  const { outboundId } = useParams();
  const navigate = useNavigate();
  const isEditMode = Boolean(outboundId);
  const initialSalesOrderId = searchParams.get("salesOrderId");
  const [salesOrderId, setSalesOrderId] = useState(
    () => initialSalesOrderId ?? "",
  );
  const [warehouseId, setWarehouseId] = useState("");
  const [items, setItems] = useState([]);
  const [salesOrderOptions, setSalesOrderOptions] = useState([]);
  const [warehouseOptions, setWarehouseOptions] = useState([]);
  const [optionsLoading, setOptionsLoading] = useState(true);
  const [lotOptionsLoading, setLotOptionsLoading] = useState(false);

  const [createLoading, setCreateLoading] = useState(false);
  const [formError, setFormError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [createdOutbound, setCreatedOutbound] = useState(null);
  const [salesOrderLoading, setSalesOrderLoading] = useState(false);
  const [loadedSalesOrder, setLoadedSalesOrder] = useState(null);

  useEffect(() => {
    async function fetchOutboundOptions() {
      try {
        const [salesOrders, warehouses] = await Promise.all([
          getSalesOrders(),
          getOutboundWarehouses(),
        ]);

        const confirmedOrders = salesOrders.filter(
          (salesOrder) => salesOrder.orderStatus === "CONFIRMED",
        );

        setSalesOrderOptions(confirmedOrders);
        setWarehouseOptions(warehouses);

        if (isEditMode) {
          const detail = await getOutboundDetail(outboundId);

          if (detail.outbound.status !== "DRAFT") {
            setFormError("작성중인 출고서만 수정할 수 있습니다.");
            return;
          }

          setSalesOrderId(String(detail.outbound.salesOrderId));
          setWarehouseId(String(detail.outbound.warehouseId));
          await loadSalesOrder(detail.outbound.salesOrderId, detail);
        } else if (initialSalesOrderId) {
          const hasSelectedOrder = confirmedOrders.some(
            (salesOrder) =>
              String(salesOrder.salesOrderId) === initialSalesOrderId,
          );

          if (hasSelectedOrder) {
            await loadSalesOrder(initialSalesOrderId);
          } else {
            setFormError("출고 가능한 확정 판매주문이 아닙니다.");
          }
        }
      } catch (error) {
        setFormError(error.message);
      } finally {
        setOptionsLoading(false);
      }
    }

    fetchOutboundOptions();
  }, [initialSalesOrderId, isEditMode, outboundId]);

  useEffect(() => {
    if (!warehouseId || !loadedSalesOrder) {
      return;
    }

    const lotManagedItems = loadedSalesOrder.items.filter(
      (item) => item.lotManagedYn === "Y",
    );

    if (lotManagedItems.length === 0) {
      return;
    }

    let isActive = true;

    async function loadLotOptions() {
      try {
        setLotOptionsLoading(true);

        const lotOptionsByProductId = new Map(
          await Promise.all(
            lotManagedItems.map(async (item) => [
              String(item.productId),
              await getOutboundLots(warehouseId, item.productId),
            ]),
          ),
        );

        if (!isActive) {
          return;
        }

        setItems((currentItems) =>
          currentItems.map((item) => ({
            ...item,
            lotOptions:
              lotOptionsByProductId.get(String(item.productId)) ?? [],
          })),
        );
      } catch (error) {
        if (isActive) {
          setFormError(error.message);
        }
      } finally {
        if (isActive) {
          setLotOptionsLoading(false);
        }
      }
    }

    loadLotOptions();

    return () => {
      isActive = false;
    };
  }, [warehouseId, loadedSalesOrder]);

  async function loadSalesOrder(selectedSalesOrderId, draftDetail = null) {
    try {
      setSalesOrderLoading(true);
      setFormError("");
      setSuccessMessage("");

      const salesOrder = await getSalesOrderDetail(selectedSalesOrderId);

      if (salesOrder.orderStatus !== "CONFIRMED") {
        setFormError("확정된 판매주문만 출고서를 등록할 수 있습니다.");
        return;
      }

      const availableItems = draftDetail
        ? draftDetail.items.map((savedItem) => {
            const salesItem = salesOrder.items.find(
              (item) =>
                String(item.salesOrderItemId) ===
                String(savedItem.salesOrderItemId),
            );

            if (!salesItem) {
              throw new Error("출고서에 연결된 판매주문 품목을 찾을 수 없습니다.");
            }

            return {
              ...salesItem,
              shippedQty: savedItem.shippedQty,
              lotAssignments: draftDetail.lotAssignments
                .filter(
                  (lot) =>
                    String(lot.outboundItemId) ===
                    String(savedItem.outboundItemId),
                )
                .map((lot) => ({
                  lotId: String(lot.lotId),
                  baseLotQty: String(lot.baseLotQty),
                })),
            };
          })
        : salesOrder.items.filter((item) => Number(item.remainingQty) > 0);

      if (availableItems.length === 0) {
        setFormError("출고 가능한 판매주문 품목이 없습니다.");
        return;
      }

      setLoadedSalesOrder(salesOrder);

      setItems(
        availableItems.map((item) => ({
          salesOrderItemId: String(item.salesOrderItemId),
          productUnitId: String(item.productUnitId),
          shippedQty: "",
          remainingQty: String(item.remainingQty),
          productId: String(item.productId),
          productName: item.productName,
          lotManagedYn: item.lotManagedYn,
          conversionQty: String(item.conversionQty),
          lotOptions: [],
          lotAssignments: item.lotAssignments ?? [],
        })),
      );
    } catch (error) {
      setFormError(error.message);
    } finally {
      setSalesOrderLoading(false);
    }
  }

  function handleSalesOrderChange(value) {
    setSalesOrderId(value);
    setLoadedSalesOrder(null);
    setItems([]);

    if (!value) {
      return;
    }

    loadSalesOrder(value);
  }

  function handleWarehouseChange(value) {
    setWarehouseId(value);
    setItems((currentItems) =>
      currentItems.map((item) => ({
        ...item,
        lotOptions: [],
        lotAssignments: [],
      })),
    );
  }

  function handleItemChange(index, fieldName, value) {
    setItems((currentItems) =>
      currentItems.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [fieldName]: value } : item,
      ),
    );
  }

  function handleRemoveItem(index) {
    if (items.length === 1) {
      setFormError("출고 품목은 한 건 이상 필요합니다.");
      return;
    }

    setItems((currentItems) =>
      currentItems.filter((_, itemIndex) => itemIndex !== index),
    );
  }

  function handleLotAssignmentChange(itemIndex, lotIndex, fieldName, value) {
    setItems((currentItems) =>
      currentItems.map((item, currentItemIndex) => {
        if (currentItemIndex !== itemIndex) {
          return item;
        }

        return {
          ...item,
          lotAssignments: item.lotAssignments.map((lot, currentLotIndex) =>
            currentLotIndex === lotIndex
              ? { ...lot, [fieldName]: value }
              : lot,
          ),
        };
      }),
    );
  }

  function handleAddLotAssignment(itemIndex) {
    setItems((currentItems) =>
      currentItems.map((item, currentItemIndex) =>
        currentItemIndex === itemIndex
          ? {
              ...item,
              lotAssignments: [
                ...item.lotAssignments,
                { lotId: "", baseLotQty: "" },
              ],
            }
          : item,
      ),
    );
  }

  function handleRemoveLotAssignment(itemIndex, lotIndex) {
    setItems((currentItems) =>
      currentItems.map((item, currentItemIndex) =>
        currentItemIndex === itemIndex
          ? {
              ...item,
              lotAssignments: item.lotAssignments.filter(
                (_, currentLotIndex) => currentLotIndex !== lotIndex,
              ),
            }
          : item,
      ),
    );
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setFormError("");
    setSuccessMessage("");
    setCreatedOutbound(null);

    if (!salesOrderId || !warehouseId) {
      setFormError("판매주문과 출고 창고를 선택해 주세요.");
      return;
    }

    if (!loadedSalesOrder) {
      setFormError("확정 판매주문을 선택해 품목을 불러와 주세요.");
      return;
    }

    if (items.length === 0) {
      setFormError("출고 품목은 한 건 이상 필요합니다.");
      return;
    }

    const hasEmptyItem = items.some(
      (item) => !item.shippedQty,
    );

    if (hasEmptyItem) {
      setFormError("모든 출고 품목 정보를 입력해 주세요.");
      return;
    }

    const hasInvalidLotAssignment = items.some((item) => {
      if (item.lotManagedYn !== "Y") {
        return false;
      }

      if (item.lotAssignments.length === 0) {
        return true;
      }

      return item.lotAssignments.some(
        (lot) => !lot.lotId || !lot.baseLotQty,
      );
    });

    if (hasInvalidLotAssignment) {
      setFormError("LOT 관리 상품은 출고할 LOT와 LOT 출고 수량을 입력해 주세요.");
      return;
    }

    const requestData = {
      salesOrderId: Number(salesOrderId),
      warehouseId: Number(warehouseId),
      items: items.map((item) => ({
        salesOrderItemId: Number(item.salesOrderItemId),
        productUnitId: Number(item.productUnitId),
        shippedQty: Number(item.shippedQty),
        lotAssignments: item.lotAssignments.map((lot) => ({
          lotId: Number(lot.lotId),
          baseLotQty: Number(lot.baseLotQty),
        })),
      })),
    };

    try {
      setCreateLoading(true);

      const outbound = isEditMode
        ? await updateOutbound(outboundId, requestData)
        : await createOutbound(requestData);

      if (isEditMode) {
        navigate(`/outbounds/${outbound.outboundId}`);
        return;
      }

      setCreatedOutbound(outbound);
      setSuccessMessage("출고서가 작성중 상태로 등록되었습니다.");

      setSalesOrderId("");
      setLoadedSalesOrder(null);
      setWarehouseId("");
      setItems([]);
    } catch (error) {
      setFormError(error.message);
    } finally {
      setCreateLoading(false);
    }
  }

  return (
    <section className="page outbound-create-page">
      <div className="page-header outbound-create-header">
        <div>
          <h1>{isEditMode ? "출고 수정" : "출고 등록"}</h1>
          <p>
            {isEditMode
              ? "작성중 출고서의 수량과 LOT 배정을 수정합니다."
              : "확정된 판매주문을 기준으로 출고서를 작성합니다."}
          </p>
        </div>
      </div>

      {successMessage && (
        <p className="outbound-success-message">{successMessage}</p>
      )}

      {formError && (
        <p className="outbound-form-error" role="alert">
          {formError}
        </p>
      )}

      <form
        className="content-panel outbound-create-form"
        onSubmit={handleSubmit}
      >
        <OutboundBasicInfoFields
          salesOrderId={salesOrderId}
          warehouseId={warehouseId}
          salesOrderOptions={salesOrderOptions}
          warehouseOptions={warehouseOptions}
          optionsLoading={optionsLoading}
          onSalesOrderChange={handleSalesOrderChange}
          onWarehouseIdChange={handleWarehouseChange}
          salesOrderLoading={salesOrderLoading}
          loadedSalesOrder={loadedSalesOrder}
          salesOrderDisabled={isEditMode}
        />

        <OutboundItemList
          items={items}
          onRemoveItem={handleRemoveItem}
          onItemChange={handleItemChange}
          onLotAssignmentChange={handleLotAssignmentChange}
          onAddLotAssignment={handleAddLotAssignment}
          onRemoveLotAssignment={handleRemoveLotAssignment}
          isSalesOrderLoaded={Boolean(loadedSalesOrder)}
          warehouseSelected={Boolean(warehouseId)}
          lotOptionsLoading={lotOptionsLoading}
        />

        <div className="outbound-form-actions">
          <p>출고서를 등록한 뒤 목록에서 확정할 수 있습니다.</p>

          <button
            type="submit"
            className="outbound-submit-button"
            disabled={createLoading}
          >
            {createLoading
              ? isEditMode
                ? "수정 중..."
                : "등록 중..."
              : isEditMode
                ? "출고서 수정"
                : "출고서 등록"}
          </button>
        </div>
      </form>

      {createdOutbound && <OutboundCreateResult outbound={createdOutbound} />}
    </section>
  );
}

export default OutboundCreatePage;
