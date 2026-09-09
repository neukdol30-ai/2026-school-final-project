import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { createOutbound } from "./js/outboundApi";
import "./css/OutboundCreatePage.css";
import OutboundBasicInfoFields from "./components/OutboundBasicInfoFields";
import OutboundItemList from "./components/OutboundItemList";
import OutboundCreateResult from "./components/OutboundCreateResult";
import { getSalesOrderDetail } from "../sales/js/salesOrderApi";

function createEmptyItem() {
  return {
    salesOrderItemId: "",
    productUnitId: "",
    shippedQty: "",
  };
}

function OutboundCreatePage() {
  const [searchParams] = useSearchParams();
  const [salesOrderId, setSalesOrderId] = useState(
    () => searchParams.get("salesOrderId") ?? "",
  );
  const [warehouseId, setWarehouseId] = useState("");
  const [items, setItems] = useState([createEmptyItem()]);

  const [createLoading, setCreateLoading] = useState(false);
  const [formError, setFormError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [createdOutbound, setCreatedOutbound] = useState(null);
  const [salesOrderLoading, setSalesOrderLoading] = useState(false);
  const [loadedSalesOrder, setLoadedSalesOrder] = useState(null);

  function handleSalesOrderIdChange(value) {
    setSalesOrderId(value);
    setLoadedSalesOrder(null);
  }

  async function handleLoadSalesOrder() {
    if (!salesOrderId) {
      setFormError("조회할 판매주문 ID를 입력해 주세요.");
      return;
    }

    try {
      setSalesOrderLoading(true);
      setFormError("");
      setSuccessMessage("");

      const salesOrder = await getSalesOrderDetail(salesOrderId);

      if (salesOrder.orderStatus !== "CONFIRMED") {
        setFormError("확정된 판매주문만 출고서를 등록할 수 있습니다.");
        return;
      }

      const availableItems = salesOrder.items.filter(
        (item) => Number(item.remainingQty) > 0,
      );

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
        })),
      );
    } catch (error) {
      setFormError(error.message);
    } finally {
      setSalesOrderLoading(false);
    }
  }

  function handleItemChange(index, fieldName, value) {
    setItems((currentItems) =>
      currentItems.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [fieldName]: value } : item,
      ),
    );
  }

  function handleAddItem() {
    setItems((currentItems) => [...currentItems, createEmptyItem()]);
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

  async function handleSubmit(event) {
    event.preventDefault();

    setFormError("");
    setSuccessMessage("");
    setCreatedOutbound(null);

    if (!salesOrderId || !warehouseId) {
      setFormError("판매주문 ID와 출고 창고 ID를 입력해 주세요.");
      return;
    }

    const hasEmptyItem = items.some(
      (item) =>
        !item.salesOrderItemId || !item.productUnitId || !item.shippedQty,
    );

    if (hasEmptyItem) {
      setFormError("모든 출고 품목 정보를 입력해 주세요.");
      return;
    }

    const requestData = {
      salesOrderId: Number(salesOrderId),
      warehouseId: Number(warehouseId),
      items: items.map((item) => ({
        salesOrderItemId: Number(item.salesOrderItemId),
        productUnitId: Number(item.productUnitId),
        shippedQty: Number(item.shippedQty),
      })),
    };

    try {
      setCreateLoading(true);

      const outbound = await createOutbound(requestData);

      setCreatedOutbound(outbound);
      setSuccessMessage("출고서가 작성중 상태로 등록되었습니다.");

      setSalesOrderId("");
      setLoadedSalesOrder(null);
      setWarehouseId("");
      setItems([createEmptyItem()]);
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
          <h1>출고 등록</h1>
          <p>확정된 판매주문을 기준으로 출고서를 작성합니다.</p>
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
          onSalesOrderIdChange={handleSalesOrderIdChange}
          onWarehouseIdChange={setWarehouseId}
          onLoadSalesOrder={handleLoadSalesOrder}
          salesOrderLoading={salesOrderLoading}
          loadedSalesOrder={loadedSalesOrder}
        />

        <OutboundItemList
          items={items}
          onAddItem={handleAddItem}
          onRemoveItem={handleRemoveItem}
          onItemChange={handleItemChange}
          isSalesOrderLoaded={Boolean(loadedSalesOrder)}
        />

        <div className="outbound-form-actions">
          <p>출고서를 등록한 뒤 목록에서 확정할 수 있습니다.</p>

          <button
            type="submit"
            className="outbound-submit-button"
            disabled={createLoading}
          >
            {createLoading ? "등록 중..." : "출고서 등록"}
          </button>
        </div>
      </form>

      {createdOutbound && <OutboundCreateResult outbound={createdOutbound} />}
    </section>
  );
}

export default OutboundCreatePage;
