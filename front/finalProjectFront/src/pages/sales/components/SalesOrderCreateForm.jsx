import { useEffect, useState } from "react";
import { requestBusinessPartners } from "../../../api/businessPartnerManagementApi.js";
import {
  requestManagementProducts,
  requestManagementProductUnits,
} from "../../../api/productManagementApi.js";

function createEmptyItem() {
  return {
    productUnitId: "",
    orderedQty: "",
    unitPrice: "",
  };
}

function SalesOrderCreateForm({
  onCreate,
  createLoading,
  validationErrors = [],
  initialData = null,
  title = "판매주문 등록",
  description = "고객과 주문 품목을 입력하세요. 출고 창고는 출고 단계에서 정합니다.",
  submitLabel = "판매주문 등록",
}) {
  const [customerId, setCustomerId] = useState(initialData?.customerId ?? "");

  const [items, setItems] = useState(initialData?.items ?? [createEmptyItem()]);

  const [formError, setFormError] = useState("");
  const [customers, setCustomers] = useState([]);
  const [customerKeyword, setCustomerKeyword] = useState("");
  const [productUnits, setProductUnits] = useState([]);
  const [optionsLoading, setOptionsLoading] = useState(true);
  const [optionsError, setOptionsError] = useState("");

  useEffect(() => {
    if (!initialData) {
      return;
    }

    setCustomerId(initialData.customerId ?? "");
    setItems(initialData.items?.length ? initialData.items : [createEmptyItem()]);
  }, [initialData]);

  useEffect(() => {
    let cancelled = false;

    async function loadOrderOptions() {
      try {
        setOptionsLoading(true);
        setOptionsError("");

        const [customerData, productData] = await Promise.all([
          requestBusinessPartners({
            partnerType: "CUSTOMER",
            useYn: "Y",
          }),
          requestManagementProducts({ useYn: "Y" }),
        ]);

        const productUnitGroups = await Promise.all(
          productData.map(async (product) => {
            const units = await requestManagementProductUnits(
              product.productId,
              "Y",
            );

            return units.map((unit) => ({
              ...unit,
              productCode: product.productCode,
              productName: product.productName,
            }));
          }),
        );

        if (!cancelled) {
          setCustomers(customerData);
          setProductUnits(productUnitGroups.flat());
        }
      } catch (error) {
        if (!cancelled) {
          setOptionsError(
            error.message ??
              "거래처와 상품 단위 목록을 불러오지 못했습니다.",
          );
        }
      } finally {
        if (!cancelled) {
          setOptionsLoading(false);
        }
      }
    }

    loadOrderOptions();

    return () => {
      cancelled = true;
    };
  }, []);

  function getValidationErrorMessage(fieldName) {
    const validationError = validationErrors.find(
      (error) => error.field === fieldName,
    );

    return validationError?.message;
  }

  function handleItemChange(index, fieldName, value) {
    setItems((currentItems) =>
      currentItems.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [fieldName]: value } : item,
      ),
    );
  }

  function handleAddItem() {
    setItems((currentItems) => [
      ...currentItems,
      createEmptyItem(),
    ]);
  }

  function handleRemoveItem(index) {
    setItems((currentItems) =>
      currentItems.filter((item, itemIndex) => itemIndex !== index),
    );
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setFormError("");

    const enteredProductUnitIds = items
      .map((item) => Number(item.productUnitId))
      .filter((productUnitId) => productUnitId > 0);

    if (new Set(enteredProductUnitIds).size !== enteredProductUnitIds.length) {
      setFormError("같은 상품 단위는 주문 품목에 한 번만 등록할 수 있습니다.");
      return;
    }

    const created = await onCreate({
      customerId: Number(customerId),
      items: items.map((item) => ({
        productUnitId: Number(item.productUnitId),
        orderedQty: Number(item.orderedQty),
        unitPrice: Number(item.unitPrice),
      })),
    });

    if (created) {
      setCustomerId("");

      setItems([createEmptyItem()]);
    }
  }

  const customerIdError = getValidationErrorMessage("customerId");
  const filteredCustomers = customers.filter((customer) => {
    const keyword = customerKeyword.trim().toLowerCase();

    if (!keyword) {
      return true;
    }

    return [customer.partnerName, customer.partnerCode].some((value) =>
      String(value ?? "").toLowerCase().includes(keyword),
    );
  });
  const selectedCustomerIsVisible = filteredCustomers.some(
    (customer) => String(customer.partnerId) === String(customerId),
  );

  return (
    <div className="content-panel sales-order-entry-panel">
      <div className="sales-order-entry-title">
          <h2>{title}</h2>
          <p>{description}</p>
      </div>

      <form onSubmit={handleSubmit}>
        {optionsError && (
          <p className="sales-order-form-error" role="alert">
            {optionsError}
          </p>
        )}

        {/* 주문 전체에 공통으로 적용되는 정보 */}
        <section className="sales-order-basic-section">
          <h3>기본 정보</h3>

          <div className="sales-order-basic-grid">
            <div className="sales-order-field">
              <label htmlFor="customerKeyword">거래처명 검색</label>
              <input
                id="customerKeyword"
                type="search"
                placeholder="거래처명 또는 거래처코드 입력"
                value={customerKeyword}
                disabled={optionsLoading}
                onChange={(event) => setCustomerKeyword(event.target.value)}
              />
            </div>

            <div className="sales-order-field">
              <label htmlFor="customerId">고객 거래처</label>
              <select
                id="customerId"
                required
                value={customerId}
                disabled={optionsLoading}
                onChange={(event) => setCustomerId(event.target.value)}
              >
                <option value="">
                  {optionsLoading
                    ? "거래처 목록을 불러오는 중..."
                    : "고객 거래처를 선택하세요."}
                </option>
                {customerId && !selectedCustomerIsVisible && (
                  <option value={customerId}>
                    현재 선택된 거래처 (ID: {customerId})
                  </option>
                )}
                {filteredCustomers.map((customer) => (
                  <option
                    key={customer.partnerId}
                    value={customer.partnerId}
                  >
                    {customer.partnerName} ({customer.partnerCode})
                  </option>
                ))}
              </select>
              {customerIdError && (
                <p className="field-error">{customerIdError}</p>
              )}
            </div>
          </div>
        </section>

        <section className="sales-order-items-section">
          <div className="sales-order-items-title">
            <div>
              <h3>주문 품목</h3>
              <p>한 줄이 주문 품목 한 건입니다.</p>
            </div>

            <button
              className="sales-order-add-button"
              type="button"
              onClick={handleAddItem}
            >
              + 품목 추가
            </button>
          </div>

          {formError && (
            <p className="sales-order-form-error" role="alert">
              {formError}
            </p>
          )}

          {/* 품목이 늘어나도 카드 대신 표의 행 한 줄만 추가된다. */}
          <div className="sales-order-table-wrap">
            <table className="sales-order-entry-table">
              <thead>
                <tr>
                  <th>번호</th>
                  <th>상품 · 단위</th>
                  <th>주문 수량</th>
                  <th>판매 단가</th>
                  <th>관리</th>
                </tr>
              </thead>

              <tbody>
                {items.map((item, index) => {
                  const productUnitIdError = getValidationErrorMessage(
                    `items[${index}].productUnitId`,
                  );
                  const orderedQtyError = getValidationErrorMessage(
                    `items[${index}].orderedQty`,
                  );
                  const unitPriceError = getValidationErrorMessage(
                    `items[${index}].unitPrice`,
                  );
                  const selectedProductUnitExists = productUnits.some(
                    (productUnit) =>
                      String(productUnit.productUnitId) ===
                      String(item.productUnitId),
                  );

                  return (
                    <tr key={index}>
                      <td className="sales-order-row-number">{index + 1}</td>

                      <td>
                        <select
                          aria-label={`품목 ${index + 1} 상품 단위`}
                          required
                          value={item.productUnitId}
                          disabled={optionsLoading}
                          onChange={(event) =>
                            handleItemChange(
                              index,
                              "productUnitId",
                              event.target.value,
                            )
                          }
                        >
                          <option value="">
                            {optionsLoading
                              ? "상품 단위를 불러오는 중..."
                              : "상품과 단위를 선택하세요."}
                          </option>
                          {item.productUnitId &&
                            !selectedProductUnitExists && (
                              <option value={item.productUnitId}>
                                현재 선택된 상품 단위 (ID: {item.productUnitId})
                              </option>
                            )}
                          {productUnits.map((productUnit) => (
                            <option
                              key={productUnit.productUnitId}
                              value={productUnit.productUnitId}
                            >
                              {productUnit.productCode} ·{" "}
                              {productUnit.productName} /{" "}
                              {productUnit.unitName} ({productUnit.unitCode})
                            </option>
                          ))}
                        </select>
                        {productUnitIdError && (
                          <p className="field-error">{productUnitIdError}</p>
                        )}
                      </td>

                      <td>
                        <input
                          aria-label={`품목 ${index + 1} 주문 수량`}
                          type="number"
                          min="0.001"
                          step="0.001"
                          placeholder="예: 3"
                          required
                          value={item.orderedQty}
                          onChange={(event) =>
                            handleItemChange(
                              index,
                              "orderedQty",
                              event.target.value,
                            )
                          }
                        />
                        {orderedQtyError && (
                          <p className="field-error">{orderedQtyError}</p>
                        )}
                      </td>

                      <td>
                        <input
                          aria-label={`품목 ${index + 1} 판매 단가`}
                          type="number"
                          min="1"
                          step="1"
                          placeholder="예: 25000"
                          required
                          value={item.unitPrice}
                          onChange={(event) =>
                            handleItemChange(
                              index,
                              "unitPrice",
                              event.target.value,
                            )
                          }
                        />
                        {unitPriceError && (
                          <p className="field-error">{unitPriceError}</p>
                        )}
                      </td>

                      <td>
                        {items.length > 1 ? (
                          <button
                            className="sales-order-delete-button"
                            type="button"
                            onClick={() => handleRemoveItem(index)}
                          >
                            삭제
                          </button>
                        ) : (
                          <span className="sales-order-row-fixed">기본 행</span>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </section>

        <div className="sales-order-submit-area">
          <p>판매 단가는 주문 당시 값으로 저장됩니다.</p>

          <button
            className="sales-order-submit-button"
            type="submit"
            disabled={createLoading || optionsLoading}
          >
            {createLoading
              ? "저장 중..."
              : optionsLoading
                ? "기준정보 조회 중..."
                : submitLabel}
          </button>
        </div>
      </form>
    </div>
  );
}

export default SalesOrderCreateForm;
