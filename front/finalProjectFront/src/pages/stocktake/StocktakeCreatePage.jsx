import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  createStocktake,
  getStocktakeDetail,
  getStocktakeLots,
  getStocktakeProducts,
  getStocktakeWarehouses,
  updateStocktake,
} from "./js/stocktakeApi";
import "./css/StocktakeCreatePage.css";

function createEmptyItem() {
  return {
    productId: "",
    lotId: "",
    actualQty: "",
    reason: "",
  };
}

function formatQuantity(value) {
  return Number(value).toLocaleString("ko-KR", {
    maximumFractionDigits: 3,
  });
}

function StocktakeCreatePage() {
  const navigate = useNavigate();
  const { stocktakeId } = useParams();
  const isEditMode = Boolean(stocktakeId);
  const [warehouseId, setWarehouseId] = useState("");
  const [memo, setMemo] = useState("");
  const [items, setItems] = useState([createEmptyItem()]);
  const [warehouseOptions, setWarehouseOptions] = useState([]);
  const [productOptions, setProductOptions] = useState([]);
  const [lotOptionsByProduct, setLotOptionsByProduct] = useState({});
  const [lotLoadingByProduct, setLotLoadingByProduct] = useState({});
  const [optionsLoading, setOptionsLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    loadInitialOptions();
  }, [stocktakeId]);

  async function loadInitialOptions() {
    try {
      setOptionsLoading(true);
      setError("");

      const [warehouses, products, detail] = await Promise.all([
        getStocktakeWarehouses(),
        getStocktakeProducts(),
        isEditMode ? getStocktakeDetail(stocktakeId) : Promise.resolve(null),
      ]);

      setWarehouseOptions(warehouses);
      setProductOptions(products);

      if (detail) {
        if (detail.status !== "DRAFT") {
          throw new Error("작성중인 재고실사만 수정할 수 있습니다.");
        }

        setWarehouseId(String(detail.warehouseId));
        setMemo(detail.memo ?? "");
        setItems(
          detail.items.map((item) => ({
            productId: String(item.productId),
            lotId: item.lotId === null ? "" : String(item.lotId),
            actualQty: String(item.actualQty),
            reason: item.reason ?? "",
          })),
        );

        const productById = new Map(
          products.map((product) => [String(product.productId), product]),
        );
        const lotProductIds = [
          ...new Set(
            detail.items
              .filter(
                (item) =>
                  productById.get(String(item.productId))?.lotManagedYn === "Y",
              )
              .map((item) => String(item.productId)),
          ),
        ];

        const lotEntries = await Promise.all(
          lotProductIds.map(async (productId) => [
            productId,
            await getStocktakeLots(detail.warehouseId, productId),
          ]),
        );

        setLotOptionsByProduct(Object.fromEntries(lotEntries));
      }
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setOptionsLoading(false);
    }
  }

  function findProductOption(productId) {
    return productOptions.find(
      (product) => String(product.productId) === String(productId),
    );
  }

  function handleWarehouseChange(event) {
    setWarehouseId(event.target.value);
    setLotOptionsByProduct({});
    setLotLoadingByProduct({});

    // 창고가 바뀌면 기존 LOT는 다른 창고 소속일 수 있어 초기화한다.
    setItems((currentItems) =>
      currentItems.map((item) => ({ ...item, lotId: "" })),
    );
  }

  function handleItemChange(index, event) {
    const { name, value } = event.target;

    setItems((currentItems) =>
      currentItems.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [name]: value } : item,
      ),
    );
  }

  async function handleProductChange(index, event) {
    const productId = event.target.value;

    setItems((currentItems) =>
      currentItems.map((item, itemIndex) =>
        itemIndex === index ? { ...item, productId, lotId: "" } : item,
      ),
    );

    const selectedProduct = findProductOption(productId);

    if (!warehouseId || !productId || selectedProduct?.lotManagedYn !== "Y") {
      return;
    }

    if (lotOptionsByProduct[productId]) {
      return;
    }

    try {
      setLotLoadingByProduct((currentState) => ({
        ...currentState,
        [productId]: true,
      }));

      const lots = await getStocktakeLots(warehouseId, productId);

      setLotOptionsByProduct((currentState) => ({
        ...currentState,
        [productId]: lots,
      }));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLotLoadingByProduct((currentState) => ({
        ...currentState,
        [productId]: false,
      }));
    }
  }

  function addItem() {
    setItems((currentItems) => [...currentItems, createEmptyItem()]);
  }

  function removeItem(index) {
    if (items.length === 1) {
      setError("실사 품목은 한 건 이상 필요합니다.");
      return;
    }

    setItems((currentItems) =>
      currentItems.filter((_, itemIndex) => itemIndex !== index),
    );
  }

  function createRequestData() {
    const parsedWarehouseId = Number(warehouseId);

    if (!Number.isInteger(parsedWarehouseId) || parsedWarehouseId <= 0) {
      throw new Error("실사 창고를 선택해 주세요.");
    }

    const parsedItems = items.map((item, index) => {
      const productId = Number(item.productId);
      const actualQty = Number(item.actualQty);
      const lotId = item.lotId === "" ? null : Number(item.lotId);
      const selectedProduct = findProductOption(item.productId);

      if (!Number.isInteger(productId) || productId <= 0) {
        throw new Error(`${index + 1}번째 품목의 상품을 선택해 주세요.`);
      }

      if (item.actualQty === "" || Number.isNaN(actualQty) || actualQty < 0) {
        throw new Error(`${index + 1}번째 품목의 실사 수량을 확인해 주세요.`);
      }

      if (selectedProduct?.lotManagedYn === "Y" && lotId === null) {
        throw new Error(`${index + 1}번째 LOT 관리 상품의 LOT를 선택해 주세요.`);
      }

      return {
        productId,
        lotId,
        actualQty,
        reason: item.reason.trim() || null,
      };
    });

    return {
      warehouseId: parsedWarehouseId,
      memo: memo.trim() || null,
      items: parsedItems,
    };
  }

  async function handleSubmit(event) {
    event.preventDefault();

    try {
      setSaving(true);
      setError("");

      const savedStocktakeId = isEditMode
        ? await updateStocktake(stocktakeId, createRequestData())
        : await createStocktake(createRequestData());

      navigate(`/stocktakes/${savedStocktakeId}`);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="page stocktake-create-page">
      <div className="stocktake-create-page-header">
        <div>
          <h1>{isEditMode ? "재고실사 수정" : "재고실사 등록"}</h1>
          <p>
            {isEditMode
              ? "작성중인 실사 문서의 내용을 수정합니다."
              : "실제로 확인한 재고 수량을 기준으로 재고실사 문서를 작성합니다."}
          </p>
        </div>

        <Link className="stocktake-list-link" to="/stocktakes">
          목록으로
        </Link>
      </div>

      {error && (
        <p className="stocktake-create-message error" role="alert">
          {error}
        </p>
      )}

      <form className="stocktake-create-form" onSubmit={handleSubmit}>
        <section className="stocktake-create-section">
          <h2>실사 기본 정보</h2>
          <p>실사할 창고와 메모를 입력합니다.</p>

          <label className="stocktake-field stocktake-warehouse-field">
            <span>실사 창고</span>
            <select
              value={warehouseId}
              onChange={handleWarehouseChange}
              disabled={optionsLoading}
              required
            >
              <option value="">창고를 선택하세요</option>
              {warehouseOptions.map((warehouse) => (
                <option key={warehouse.warehouseId} value={warehouse.warehouseId}>
                  {warehouse.warehouseName}
                </option>
              ))}
            </select>
          </label>

          <label className="stocktake-field">
            <span>메모</span>
            <textarea
              value={memo}
              onChange={(event) => setMemo(event.target.value)}
              placeholder="실사 사유 또는 특이사항을 입력하세요."
              maxLength="1000"
              rows="3"
            />
          </label>

          <p className="stocktake-id-guide">
            창고와 상품을 선택하면 LOT 관리 여부에 맞춰 입력 항목이 바뀝니다.
          </p>
        </section>

        <section className="stocktake-create-section">
          <div className="stocktake-item-section-header">
            <div>
              <h2>실사 품목</h2>
              <p>LOT 관리 상품은 선택한 창고에 보관된 LOT 중 하나를 선택합니다.</p>
            </div>

            <button
              type="button"
              className="stocktake-add-item-button"
              onClick={addItem}
              disabled={optionsLoading}
            >
              + 품목 추가
            </button>
          </div>

          <div className="stocktake-item-list">
            {items.map((item, index) => {
              const selectedProduct = findProductOption(item.productId);
              const isLotManaged = selectedProduct?.lotManagedYn === "Y";
              const lotOptions = lotOptionsByProduct[item.productId] ?? [];
              const isLotLoading = lotLoadingByProduct[item.productId];

              return (
                <section className="stocktake-item-card" key={index}>
                  <div className="stocktake-item-card-header">
                    <strong>실사 품목 {index + 1}</strong>
                    <button
                      type="button"
                      className="stocktake-remove-item-button"
                      onClick={() => removeItem(index)}
                    >
                      삭제
                    </button>
                  </div>

                  <div className="stocktake-item-fields">
                    <label className="stocktake-field">
                      <span>상품</span>
                      <select
                        value={item.productId}
                        onChange={(event) => handleProductChange(index, event)}
                        disabled={optionsLoading || !warehouseId}
                        required
                      >
                        <option value="">
                          {warehouseId ? "상품을 선택하세요" : "먼저 창고를 선택하세요"}
                        </option>
                        {productOptions.map((product) => (
                          <option key={product.productId} value={product.productId}>
                            {product.productName}
                            {product.lotManagedYn === "Y" ? " (LOT 관리)" : ""}
                          </option>
                        ))}
                      </select>
                    </label>

                    {isLotManaged ? (
                      <label className="stocktake-field">
                        <span>LOT</span>
                        <select
                          name="lotId"
                          value={item.lotId}
                          onChange={(event) => handleItemChange(index, event)}
                          disabled={isLotLoading || !item.productId}
                          required
                        >
                          <option value="">
                            {isLotLoading
                              ? "LOT를 불러오는 중..."
                              : lotOptions.length === 0
                                ? "선택 가능한 LOT가 없습니다"
                                : "LOT를 선택하세요"}
                          </option>
                          {lotOptions.map((lot) => (
                            <option key={lot.lotId} value={lot.lotId}>
                              {lot.lotNo} (전산 재고: {formatQuantity(lot.quantity)})
                            </option>
                          ))}
                        </select>
                      </label>
                    ) : (
                      <div className="stocktake-lot-guide">
                        <span>LOT</span>
                        <p>
                          {item.productId
                            ? "LOT 비관리 상품입니다."
                            : "상품 선택 후 표시됩니다."}
                        </p>
                      </div>
                    )}

                    <label className="stocktake-field">
                      <span>실사 수량</span>
                      <input
                        type="number"
                        min="0"
                        step="0.001"
                        name="actualQty"
                        value={item.actualQty}
                        onChange={(event) => handleItemChange(index, event)}
                        placeholder="예: 25"
                        required
                      />
                    </label>

                    <label className="stocktake-field stocktake-reason-field">
                      <span>차이 사유</span>
                      <input
                        type="text"
                        name="reason"
                        value={item.reason}
                        onChange={(event) => handleItemChange(index, event)}
                        placeholder="예: 파손, 분실, 계수 오류"
                      />
                    </label>
                  </div>
                </section>
              );
            })}
          </div>
        </section>

        <div className="stocktake-create-actions">
          <Link className="stocktake-cancel-link" to="/stocktakes">
            취소
          </Link>
          <button type="submit" disabled={saving || optionsLoading}>
            {saving
              ? isEditMode
                ? "수정 중..."
                : "등록 중..."
              : isEditMode
                ? "재고실사 수정"
                : "재고실사 등록"}
          </button>
        </div>
      </form>
    </section>
  );
}

export default StocktakeCreatePage;
