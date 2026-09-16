import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import {
  requestProductUnits,
  requestProducts,
  requestSuppliers,
  requestWarehouses,
} from "../../api/masterDataApi.js";
import { requestCreatePurchaseOrder } from "../../api/purchaseOrderApi.js";
import "./PurchaseOrderCreatePage.css";

const KOREA_TIME_ZONE = "Asia/Seoul";

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

function addMonthsToDateString(dateString, monthsToAdd) {
  const [year, month, day] = dateString.split("-").map(Number);

  const totalMonths = year * 12 + (month - 1) + monthsToAdd;

  const targetYear = Math.floor(totalMonths / 12);

  const targetMonthIndex = ((totalMonths % 12) + 12) % 12;

  const lastDayOfTargetMonth = new Date(
    Date.UTC(targetYear, targetMonthIndex + 1, 0),
  ).getUTCDate();

  const targetDay = Math.min(day, lastDayOfTargetMonth);

  const formattedMonth = String(targetMonthIndex + 1).padStart(2, "0");

  const formattedDay = String(targetDay).padStart(2, "0");

  return `${targetYear}-${formattedMonth}-${formattedDay}`;
}

function createEmptyItem() {
  return {
    rowId: crypto.randomUUID(),
    productId: "",
    productUnitId: "",
    orderedQty: "",
    unitPrice: "",
    units: [],
    loadingUnits: false,
  };
}

export default function PurchaseOrderCreatePage() {
  const navigate = useNavigate();
  const location = useLocation();

  const [suppliers, setSuppliers] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [products, setProducts] = useState([]);
  const [loadingMasterData, setLoadingMasterData] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const [form, setForm] = useState({
    supplierId: "",
    warehouseId: "",
    orderDate: getKstTodayString(),
    expectedDeliveryDate: "",
    requestNote: "",
    internalMemo: "",
    items: [createEmptyItem()],
  });

  const kstToday = getKstTodayString();

  const minimumOrderDate = addMonthsToDateString(kstToday, -1);

  const maximumExpectedDeliveryDate = form.orderDate
    ? addMonthsToDateString(form.orderDate, 1)
    : "";

  useEffect(() => {
    let cancelled = false;

    async function loadMasterData() {
      try {
        setLoadingMasterData(true);
        setErrorMessage("");

        const [supplierData, warehouseData, productData] = await Promise.all([
          requestSuppliers(),
          requestWarehouses(),
          requestProducts(),
        ]);

        if (!cancelled) {
          setSuppliers(supplierData);
          setWarehouses(warehouseData);
          setProducts(productData);
        }
      } catch (error) {
        if (!cancelled) {
          setErrorMessage(error.message);
        }
      } finally {
        if (!cancelled) {
          setLoadingMasterData(false);
        }
      }
    }

    loadMasterData();

    return () => {
      cancelled = true;
    };
  }, []);

  function handleHeaderChange(event) {
    setErrorMessage("");

    const { name, value } = event.target;

    setForm((previousForm) => ({
      ...previousForm,
      [name]: value,
    }));
  }

  function handleItemChange(index, field, value) {
    setErrorMessage("");

    setForm((previousForm) => ({
      ...previousForm,
      items: previousForm.items.map((item, itemIndex) =>
        itemIndex === index
          ? {
              ...item,
              [field]: value,
            }
          : item,
      ),
    }));
  }

  async function handleProductChange(index, productId) {
    setErrorMessage("");

    setForm((previousForm) => ({
      ...previousForm,
      items: previousForm.items.map((item, itemIndex) =>
        itemIndex === index
          ? {
              ...item,
              productId,
              productUnitId: "",
              units: [],
              loadingUnits: Boolean(productId),
            }
          : item,
      ),
    }));

    if (!productId) {
      return;
    }

    try {
      const unitData = await requestProductUnits(productId);

      setForm((previousForm) => ({
        ...previousForm,
        items: previousForm.items.map((item, itemIndex) => {
          if (itemIndex !== index) {
            return item;
          }

          if (String(item.productId) !== String(productId)) {
            return item;
          }

          return {
            ...item,
            units: unitData,
            loadingUnits: false,
          };
        }),
      }));
    } catch (error) {
      setErrorMessage(error.message);

      setForm((previousForm) => ({
        ...previousForm,
        items: previousForm.items.map((item, itemIndex) =>
          itemIndex === index
            ? {
                ...item,
                units: [],
                loadingUnits: false,
              }
            : item,
        ),
      }));
    }
  }

  function handleAddItem() {
    setErrorMessage("");

    setForm((previousForm) => ({
      ...previousForm,
      items: [...previousForm.items, createEmptyItem()],
    }));
  }

  function handleRemoveItem(rowId) {
    if (form.items.length <= 1) {
      return;
    }

    setErrorMessage("");

    setForm((previousForm) => ({
      ...previousForm,
      items: previousForm.items.filter((item) => item.rowId !== rowId),
    }));
  }

  function validateForm() {
    if (!form.supplierId) {
      return "공급업체를 선택해 주세요.";
    }

    if (!form.warehouseId) {
      return "창고를 선택해 주세요.";
    }

    if (!form.orderDate) {
      return "발주일을 입력해 주세요.";
    }

    if (form.orderDate < minimumOrderDate) {
      return "발주일은 한국시간 기준 최근 1개월 이내의 날짜만 입력할 수 있습니다.";
    }

    if (form.orderDate > kstToday) {
      return "발주일은 한국시간 기준 오늘보다 미래로 지정할 수 없습니다.";
    }

    if (form.expectedDeliveryDate) {
      if (form.expectedDeliveryDate < form.orderDate) {
        return "납품희망일은 발주일보다 빠를 수 없습니다.";
      }

      if (form.expectedDeliveryDate > maximumExpectedDeliveryDate) {
        return "납품희망일은 발주일로부터 1개월을 초과할 수 없습니다.";
      }
    }

    for (let index = 0; index < form.items.length; index += 1) {
      const item = form.items[index];

      if (!item.productId) {
        return `${index + 1}번째 품목의 상품을 선택해 주세요.`;
      }

      if (!item.productUnitId) {
        return `${index + 1}번째 품목의 단위를 선택해 주세요.`;
      }

      if (!item.orderedQty || Number(item.orderedQty) <= 0) {
        return `${index + 1}번째 품목의 발주수량은 0보다 커야 합니다.`;
      }

      if (item.unitPrice === "" || Number(item.unitPrice) < 0) {
        return `${index + 1}번째 품목의 매입단가는 0 이상이어야 합니다.`;
      }
    }

    return "";
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setErrorMessage("");

    const validationMessage = validateForm();

    if (validationMessage) {
      setErrorMessage(validationMessage);
      return;
    }

    const requestData = {
      supplierId: Number(form.supplierId),
      warehouseId: Number(form.warehouseId),
      orderDate: form.orderDate,
      expectedDeliveryDate: form.expectedDeliveryDate || null,
      requestNote: form.requestNote,
      internalMemo: form.internalMemo,
      items: form.items.map((item) => ({
        productId: Number(item.productId),
        productUnitId: Number(item.productUnitId),
        orderedQty: item.orderedQty,
        unitPrice: item.unitPrice,
      })),
    };

    try {
      setSubmitting(true);

      const createdPurchaseOrder =
        await requestCreatePurchaseOrder(requestData);

      navigate(`/purchase-orders/${createdPurchaseOrder.purchaseOrderId}`);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  function handleCancel() {
    navigate(`/purchase-orders${location.search}`);
  }

  return (
    <section className="page purchase-order-create-page">
      <div className="purchase-order-create-header">
        <div>
          <h1>발주 등록</h1>
          <p>공급업체와 발주 품목을 입력해 새 발주서를 등록합니다.</p>
        </div>
      </div>

      {errorMessage && (
        <div className="purchase-order-create-error">{errorMessage}</div>
      )}

      {loadingMasterData ? (
        <p>기준정보를 불러오는 중입니다.</p>
      ) : (
        <form onSubmit={handleSubmit} noValidate>
          <section className="purchase-order-create-section">
            <h2>발주 기본정보</h2>

            <div className="purchase-order-create-form-grid">
              <label>
                공급업체
                <select
                  name="supplierId"
                  value={form.supplierId}
                  onChange={handleHeaderChange}
                >
                  <option value="">공급업체를 선택하세요.</option>
                  {suppliers.map((supplier) => (
                    <option
                      key={supplier.supplierId}
                      value={supplier.supplierId}
                    >
                      {supplier.supplierCode} - {supplier.supplierName}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                창고
                <select
                  name="warehouseId"
                  value={form.warehouseId}
                  onChange={handleHeaderChange}
                >
                  <option value="">창고를 선택하세요.</option>
                  {warehouses.map((warehouse) => (
                    <option
                      key={warehouse.warehouseId}
                      value={warehouse.warehouseId}
                    >
                      {warehouse.warehouseCode} - {warehouse.warehouseName}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                발주일
                <input
                  type="date"
                  name="orderDate"
                  value={form.orderDate}
                  min={minimumOrderDate}
                  max={kstToday}
                  onChange={handleHeaderChange}
                />
              </label>

              <label>
                납품희망일
                <input
                  type="date"
                  name="expectedDeliveryDate"
                  value={form.expectedDeliveryDate}
                  min={form.orderDate || undefined}
                  max={maximumExpectedDeliveryDate || undefined}
                  onChange={handleHeaderChange}
                />
              </label>
            </div>

            <label>
              공급업체 요청사항
              <textarea
                name="requestNote"
                value={form.requestNote}
                onChange={handleHeaderChange}
                maxLength={500}
                rows={3}
              />
            </label>

            <label>
              내부 메모
              <textarea
                name="internalMemo"
                value={form.internalMemo}
                onChange={handleHeaderChange}
                maxLength={500}
                rows={3}
              />
            </label>
          </section>

          <section className="purchase-order-create-section">
            <div className="purchase-order-create-item-header">
              <h2>발주 품목</h2>

              <button type="button" onClick={handleAddItem}>
                품목 추가
              </button>
            </div>

            <div className="purchase-order-create-table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>상품</th>
                    <th>단위</th>
                    <th>발주수량</th>
                    <th>매입단가</th>
                    <th>삭제</th>
                  </tr>
                </thead>

                <tbody>
                  {form.items.map((item, index) => (
                    <tr key={item.rowId}>
                      <td>
                        <select
                          value={item.productId}
                          onChange={(event) =>
                            handleProductChange(index, event.target.value)
                          }
                        >
                          <option value="">상품을 선택하세요.</option>
                          {products.map((product) => (
                            <option
                              key={product.productId}
                              value={product.productId}
                            >
                              {product.productCode} - {product.productName}
                            </option>
                          ))}
                        </select>
                      </td>

                      <td>
                        <select
                          value={item.productUnitId}
                          disabled={!item.productId || item.loadingUnits}
                          onChange={(event) =>
                            handleItemChange(
                              index,
                              "productUnitId",
                              event.target.value,
                            )
                          }
                        >
                          <option value="">
                            {item.loadingUnits
                              ? "단위 조회 중..."
                              : "단위를 선택하세요."}
                          </option>
                          {item.units.map((unit) => (
                            <option
                              key={unit.productUnitId}
                              value={unit.productUnitId}
                            >
                              {unit.unitCode} - {unit.unitName}
                            </option>
                          ))}
                        </select>
                      </td>

                      <td>
                        <input
                          type="number"
                          min="0.001"
                          step="0.001"
                          value={item.orderedQty}
                          onChange={(event) =>
                            handleItemChange(
                              index,
                              "orderedQty",
                              event.target.value,
                            )
                          }
                        />
                      </td>

                      <td>
                        <input
                          type="number"
                          min="0"
                          step="0.01"
                          value={item.unitPrice}
                          onChange={(event) =>
                            handleItemChange(
                              index,
                              "unitPrice",
                              event.target.value,
                            )
                          }
                        />
                      </td>

                      <td>
                        <button
                          type="button"
                          disabled={form.items.length === 1}
                          onClick={() => handleRemoveItem(item.rowId)}
                        >
                          삭제
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>

          <div className="purchase-order-create-actions">
            <button type="button" onClick={handleCancel} disabled={submitting}>
              취소
            </button>

            <button type="submit" disabled={submitting}>
              {submitting ? "등록 중..." : "발주 등록"}
            </button>
          </div>
        </form>
      )}
    </section>
  );
}
