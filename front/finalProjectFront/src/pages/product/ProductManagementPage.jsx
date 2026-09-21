import {
    useCallback,
    useEffect,
    useState,
} from "react";
import {
    requestAvailableUnits,
    requestCreateProduct,
    requestCreateProductUnit,
    requestDeactivateProduct,
    requestManagementProducts,
    requestManagementProductUnits,
    requestUpdateProduct,
    requestUpdateProductUnit,
    requestDeactivateProductUnit,
} from "../../api/productManagementApi.js";
import ProductForm from "./ProductForm.jsx";
import ProductSearchForm from "./ProductSearchForm.jsx";
import ProductUnitPanel from "./ProductUnitPanel.jsx";
import ProductUnitForm from "./ProductUnitForm.jsx";
import ManagementModal
    from "../../components/common/ManagementModal.jsx";
import ManagementAccessDenied
    from "../../components/common/ManagementAccessDenied.jsx";
import { hasAuthority }
    from "../../storage/authStorage.js";
import "./ProductManagementPage.css";


const TAX_TYPE_LABELS = {
    TAXABLE: "과세",
    TAX_FREE: "면세",
};

const INITIAL_FILTERS = {
    keyword: "",
    lotManagedYn: "",
    taxType: "",
    storageType: "",
    useYn: "",
};

const INITIAL_FORM = {
    productCode: "",
    productName: "",
    lotManagedYn: "N",
    taxType: "TAXABLE",
    storageType: "AMBIENT",
};

const INITIAL_PRODUCT_UNIT_FORM = {
    unitId: "",
    conversionQty: "1",
    isBaseYn: "N",
};

const STORAGE_TYPE_LABELS = {
    AMBIENT: "상온",
    CHILLED: "냉장",
    FROZEN: "냉동",
};

function ProductManagementPage() {
    const canRead = hasAuthority("PRODUCT_READ");
    const canCreate = hasAuthority("PRODUCT_CREATE");
    const canUpdate = hasAuthority("PRODUCT_UPDATE");
    const canDeactivate = hasAuthority(
        "PRODUCT_DEACTIVATE",
    );
    const canManageProductUnits =
        canCreate || canUpdate;

    const [filters, setFilters] =
        useState(INITIAL_FILTERS);
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [errorMessage, setErrorMessage] =
        useState("");

    const [formOpen, setFormOpen] =
        useState(false);

    const [editingProductId, setEditingProductId] =
        useState(null);

    const [form, setForm] =
        useState(INITIAL_FORM);

    const [saving, setSaving] =
        useState(false);

    const [successMessage, setSuccessMessage] =
        useState("");

    const [selectedProduct, setSelectedProduct] =
        useState(null);

    const [productUnits, setProductUnits] =
        useState([]);

    const [productUnitsLoading, setProductUnitsLoading] =
        useState(false);

    const [
        productUnitErrorMessage,
        setProductUnitErrorMessage,
    ] = useState("");

    const [availableUnits, setAvailableUnits] =
        useState([]);

    const [
        productUnitFormOpen,
        setProductUnitFormOpen,
    ] = useState(false);

    const [
        productUnitForm,
        setProductUnitForm,
    ] = useState(INITIAL_PRODUCT_UNIT_FORM);

    const [productUnitSaving, setProductUnitSaving] =
        useState(false);

    const [
        editingProductUnitId,
        setEditingProductUnitId,
    ] = useState(null);

    const loadProducts = useCallback(
        async (nextFilters) => {
            setLoading(true);
            setErrorMessage("");

            try {
                const data =
                    await requestManagementProducts(
                        nextFilters,
                    );

                setProducts(data);
            } catch (error) {
                setProducts([]);

                setErrorMessage(
                    error instanceof Error
                        ? error.message
                        : "상품 목록을 불러오지 못했습니다.",
                );
            } finally {
                setLoading(false);
            }
        },
        [],
    );

    useEffect(() => {
        if (!canRead) {
            return undefined;
        }

        let cancelled = false;

        requestManagementProducts(INITIAL_FILTERS)
            .then((data) => {
                if (!cancelled) {
                    setProducts(data);
                }
            })
            .catch((error) => {
                if (!cancelled) {
                    setProducts([]);

                    setErrorMessage(
                        error instanceof Error
                            ? error.message
                            : "상품 목록을 불러오지 못했습니다.",
                    );
                }
            })
            .finally(() => {
                if (!cancelled) {
                    setLoading(false);
                }
            });

        return () => {
            cancelled = true;
        };
    }, [canRead]);

    function handleFilterChange(event) {
        const { name, value } = event.target;

        setFilters((previousFilters) => ({
            ...previousFilters,
            [name]: value,
        }));
    }

    function handleSearch(event) {
        event.preventDefault();

        loadProducts(filters);
    }

    function handleReset() {
        setFilters(INITIAL_FILTERS);

        loadProducts(INITIAL_FILTERS);
    }

    function openCreateForm() {
        setEditingProductId(null);
        setForm(INITIAL_FORM);
        setErrorMessage("");
        setSuccessMessage("");
        setFormOpen(true);
    }

    function openEditForm(product) {
        setEditingProductId(product.productId);

        setForm({
            productCode: product.productCode,
            productName: product.productName,
            lotManagedYn: product.lotManagedYn,
            taxType: product.taxType,
            storageType: product.storageType,
        });

        setErrorMessage("");
        setSuccessMessage("");
        setFormOpen(true);
    }

    function closeForm() {
        if (saving) {
            return;
        }

        setFormOpen(false);
        setEditingProductId(null);
        setForm(INITIAL_FORM);
    }

    function handleFormChange(event) {
        const { name, value } = event.target;

        setForm((previousForm) => ({
            ...previousForm,
            [name]: value,
        }));
    }

    async function handleSaveProduct(event) {
        event.preventDefault();

        const productData = {
            ...form,
            productCode: form.productCode.trim(),
            productName: form.productName.trim(),
        };

        if (
            !productData.productCode ||
            !productData.productName
        ) {
            setErrorMessage(
                "상품코드와 상품명을 모두 입력해주세요.",
            );

            return;
        }

        setSaving(true);
        setErrorMessage("");
        setSuccessMessage("");

        try {
            if (editingProductId) {
                await requestUpdateProduct(
                    editingProductId,
                    productData,
                );

                setSuccessMessage(
                    "상품 정보가 수정되었습니다.",
                );
            } else {
                await requestCreateProduct(productData);

                setSuccessMessage(
                    "상품이 등록되었습니다.",
                );
            }

            setFormOpen(false);
            setEditingProductId(null);
            setForm(INITIAL_FORM);

            await loadProducts(filters);
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "상품 정보를 저장하지 못했습니다.",
            );
        } finally {
            setSaving(false);
        }
    }

    async function handleDeactivateProduct(product) {
        if (product.useYn === "N") {
            return;
        }

        const confirmed = window.confirm(
            `'${product.productName}' 상품을 비활성화하시겠습니까?`,
        );

        if (!confirmed) {
            return;
        }

        setErrorMessage("");
        setSuccessMessage("");

        try {
            await requestDeactivateProduct(
                product.productId,
            );

            setSuccessMessage(
                "상품이 비활성화되었습니다.",
            );

            await loadProducts(filters);
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "상품을 비활성화하지 못했습니다.",
            );
        }
    }

    async function openProductUnits(product) {
        setSelectedProduct(product);
        setProductUnits([]);
        setProductUnitErrorMessage("");
        setProductUnitsLoading(true);

        try {
            const data =
                await requestManagementProductUnits(
                    product.productId,
                );

            setProductUnits(data);
        } catch (error) {
            setProductUnitErrorMessage(
                error instanceof Error
                    ? error.message
                    : "상품단위 목록을 불러오지 못했습니다.",
            );
        } finally {
            setProductUnitsLoading(false);
        }
    }

    function closeProductUnits() {
        setSelectedProduct(null);
        setProductUnits([]);
        setProductUnitErrorMessage("");
    }

    async function openProductUnitCreateForm() {
        setProductUnitErrorMessage("");
        setSuccessMessage("");
        setEditingProductUnitId(null);

        try {
            const data = await requestAvailableUnits();

            setAvailableUnits(data);
            setProductUnitForm(
                INITIAL_PRODUCT_UNIT_FORM,
            );
            setProductUnitFormOpen(true);
        } catch (error) {
            setProductUnitErrorMessage(
                error instanceof Error
                    ? error.message
                    : "단위 목록을 불러오지 못했습니다.",
            );
        }
    }

    async function openProductUnitEditForm(
        productUnit,
    ) {
        setProductUnitErrorMessage("");
        setSuccessMessage("");

        try {
            const data = await requestAvailableUnits();

            setAvailableUnits(data);

            setEditingProductUnitId(
                productUnit.productUnitId,
            );

            setProductUnitForm({
                unitId: String(productUnit.unitId),
                conversionQty: String(
                    productUnit.conversionQty,
                ),
                isBaseYn: productUnit.isBaseYn,
            });

            setProductUnitFormOpen(true);
        } catch (error) {
            setProductUnitErrorMessage(
                error instanceof Error
                    ? error.message
                    : "단위 목록을 불러오지 못했습니다.",
            );
        }
    }

    function handleProductUnitFormChange(event) {
        const { name, value } = event.target;

        setProductUnitForm((previousForm) => ({
            ...previousForm,
            [name]: value,
        }));
    }

    function closeProductUnitForm() {
        if (productUnitSaving) {
            return;
        }

        setProductUnitFormOpen(false);
        setProductUnitForm(
            INITIAL_PRODUCT_UNIT_FORM,
        );
        setEditingProductUnitId(null);
    }

    async function handleSaveProductUnit(event) {
        event.preventDefault();

        const unitId = Number(
            productUnitForm.unitId,
        );

        const conversionQty = Number(
            productUnitForm.conversionQty,
        );

        if (
            !Number.isInteger(unitId) ||
            unitId <= 0
        ) {
            setProductUnitErrorMessage(
                "단위를 선택해주세요.",
            );

            return;
        }

        if (
            !Number.isFinite(conversionQty) ||
            conversionQty <= 0
        ) {
            setProductUnitErrorMessage(
                "환산수량은 0보다 커야 합니다.",
            );

            return;
        }

        const requestData = {
            unitId,
            conversionQty,
            isBaseYn: productUnitForm.isBaseYn,
        };

        setProductUnitSaving(true);
        setProductUnitErrorMessage("");
        setSuccessMessage("");

        try {
            if (editingProductUnitId) {
                await requestUpdateProductUnit(
                    selectedProduct.productId,
                    editingProductUnitId,
                    requestData,
                );

                setSuccessMessage(
                    "상품단위가 수정되었습니다.",
                );
            } else {
                await requestCreateProductUnit(
                    selectedProduct.productId,
                    requestData,
                );

                setSuccessMessage(
                    "상품단위가 등록되었습니다.",
                );
            }

            const data =
                await requestManagementProductUnits(
                    selectedProduct.productId,
                );

            setProductUnits(data);
            setProductUnitFormOpen(false);
            setEditingProductUnitId(null);
            setProductUnitForm(
                INITIAL_PRODUCT_UNIT_FORM,
            );
        } catch (error) {
            setProductUnitErrorMessage(
                error instanceof Error
                    ? error.message
                    : "상품단위를 저장하지 못했습니다.",
            );
        } finally {
            setProductUnitSaving(false);
        }
    }

    async function handleDeactivateProductUnit(
        productUnit,
    ) {
        if (productUnit.useYn === "N") {
            return;
        }

        const confirmed = window.confirm(
            `'${productUnit.unitName}' 상품단위를 비활성화하시겠습니까?`,
        );

        if (!confirmed) {
            return;
        }

        setProductUnitErrorMessage("");
        setSuccessMessage("");

        try {
            await requestDeactivateProductUnit(
                selectedProduct.productId,
                productUnit.productUnitId,
            );

            const data =
                await requestManagementProductUnits(
                    selectedProduct.productId,
                );

            setProductUnits(data);

            setSuccessMessage(
                "상품단위가 비활성화되었습니다.",
            );
        } catch (error) {
            setProductUnitErrorMessage(
                error instanceof Error
                    ? error.message
                    : "상품단위를 비활성화하지 못했습니다.",
            );
        }
    }

    if (!canRead) {
        return (
            <div className="page product-management-page">
                <ManagementAccessDenied resourceName="상품" />
            </div>
        );
    }

    return (
        <div className="page product-management-page">
            <div className="product-management-header">
                <div>
                    <h1>상품관리</h1>
                    <p>
                        회사에서 사용하는 상품의 기본정보를
                        관리합니다.
                    </p>
                </div>

                <button
                    type="button"
                    className={
                        canCreate
                            ? "product-primary-button"
                            : "product-primary-button permission-disabled"
                    }
                    onClick={openCreateForm}
                    disabled={!canCreate}
                >
                    상품 등록
                </button>
            </div>

            <ProductSearchForm
                filters={filters}
                loading={loading}
                onChange={handleFilterChange}
                onSearch={handleSearch}
                onReset={handleReset}
            />

            {formOpen && (
                <ManagementModal
                    onClose={closeForm}
                    closeDisabled={saving}
                >
                    <ProductForm
                        form={form}
                        saving={saving}
                        editing={editingProductId !== null}
                        onChange={handleFormChange}
                        onSubmit={handleSaveProduct}
                        onCancel={closeForm}
                    />
                </ManagementModal>
            )}

            {errorMessage && (
                <p
                    className="product-message error"
                    role="alert"
                >
                    {errorMessage}
                </p>
            )}

            {successMessage && (
                <p className="product-message success">
                    {successMessage}
                </p>
            )}

            <div className="product-result-summary">
                조회 결과 {products.length}건
            </div>

            <div className="product-table-wrap">
                <table className="product-table">
                    <thead>
                    <tr>
                        <th>상품코드</th>
                        <th>상품명</th>
                        <th>LOT 관리</th>
                        <th>과세구분</th>
                        <th>보관유형</th>
                        <th>사용상태</th>
                        <th>관리</th>
                    </tr>
                    </thead>

                    <tbody>
                    {loading ? (
                        <tr>
                            <td
                                colSpan="7"
                                className="product-empty-row"
                            >
                                상품 목록을 불러오는 중입니다.
                            </td>
                        </tr>
                    ) : products.length === 0 ? (
                        <tr>
                            <td
                                colSpan="7"
                                className="product-empty-row"
                            >
                                조회된 상품이 없습니다.
                            </td>
                        </tr>
                    ) : (
                        products.map((product) => (
                            <tr key={product.productId}>
                                <td>{product.productCode}</td>

                                <td>{product.productName}</td>

                                <td>
                                    {product.lotManagedYn === "Y"
                                        ? "사용"
                                        : "미사용"}
                                </td>

                                <td>
                                    {TAX_TYPE_LABELS[product.taxType]
                                        ?? product.taxType}
                                </td>

                                <td>
                                    {STORAGE_TYPE_LABELS[
                                        product.storageType
                                        ] ?? product.storageType}
                                </td>

                                <td>
      <span
          className={
              product.useYn === "Y"
                  ? "product-status active"
                  : "product-status inactive"
          }
      >
        {product.useYn === "Y"
            ? "사용"
            : "미사용"}
      </span>
                                </td>

                                <td>
                                    <div className="product-row-actions">
                                        <button
                                            type="button"
                                            className="product-unit-button"
                                            onClick={() =>
                                                openProductUnits(product)
                                            }
                                        >
                                            단위관리
                                        </button>

                                        <button
                                            type="button"
                                            className={
                                                canUpdate
                                                    ? "product-edit-button"
                                                    : "product-edit-button permission-disabled"
                                            }
                                            onClick={() =>
                                                openEditForm(product)
                                            }
                                            disabled={!canUpdate}
                                        >
                                            수정
                                        </button>

                                        <button
                                            type="button"
                                            className={
                                                canDeactivate
                                                    ? "product-deactivate-button"
                                                    : "product-deactivate-button permission-disabled"
                                            }
                                            onClick={() =>
                                                handleDeactivateProduct(product)
                                            }
                                            disabled={
                                                !canDeactivate ||
                                                product.useYn === "N"
                                            }
                                        >
                                            비활성화
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
            {selectedProduct && (
                <>
                    <div className="product-unit-create-area">
                        <button
                            type="button"
                            className={
                                canManageProductUnits
                                    ? "product-primary-button"
                                    : "product-primary-button permission-disabled"
                            }
                            onClick={openProductUnitCreateForm}
                            disabled={!canManageProductUnits}
                        >
                            상품단위 등록
                        </button>
                    </div>

                    {productUnitFormOpen && (
                        <ManagementModal
                            onClose={closeProductUnitForm}
                            closeDisabled={productUnitSaving}
                        >
                            <ProductUnitForm
                                form={productUnitForm}
                                units={availableUnits}
                                saving={productUnitSaving}
                                editing={editingProductUnitId !== null}
                                onChange={handleProductUnitFormChange}
                                onSubmit={handleSaveProductUnit}
                                onCancel={closeProductUnitForm}
                            />
                        </ManagementModal>
                    )}

                    <ProductUnitPanel
                        product={selectedProduct}
                        productUnits={productUnits}
                        loading={productUnitsLoading}
                        errorMessage={productUnitErrorMessage}
                        onEdit={openProductUnitEditForm}
                        onDeactivate={
                            handleDeactivateProductUnit
                        }
                        canUpdate={canUpdate}
                        onClose={closeProductUnits}
                    />
                </>
            )}
        </div>
    );
}

export default ProductManagementPage;
