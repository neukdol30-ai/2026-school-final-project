import {
    useCallback,
    useEffect,
    useState,
} from "react";
import {
    requestCreateProduct,
    requestManagementProducts,
    requestUpdateProduct,
} from "../../api/productManagementApi.js";
import ProductForm from "./ProductForm.jsx";
import ProductSearchForm from "./ProductSearchForm.jsx";
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

const STORAGE_TYPE_LABELS = {
    AMBIENT: "상온",
    CHILLED: "냉장",
    FROZEN: "냉동",
};

function ProductManagementPage() {
    const [filters, setFilters] =
        useState(INITIAL_FILTERS);
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(false);
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
        loadProducts(INITIAL_FILTERS);
    }, [loadProducts]);

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
                    className="product-primary-button"
                    onClick={openCreateForm}
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
                <ProductForm
                    form={form}
                    saving={saving}
                    editing={editingProductId !== null}
                    onChange={handleFormChange}
                    onSubmit={handleSaveProduct}
                    onCancel={closeForm}
                />
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
                                colSpan="6"
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
                                    {TAX_TYPE_LABELS[
                                        product.taxType
                                        ] ?? product.taxType}
                                </td>

                                <td>
                                    {STORAGE_TYPE_LABELS[
                                        product.storageType
                                        ] ?? product.storageType}
                                </td>

                                <td>
                                    {product.useYn === "Y"
                                        ? "사용"
                                        : "미사용"}
                                </td>

                                <td>
                                    <button
                                        type="button"
                                        className="product-edit-button"
                                        onClick={() => openEditForm(product)}
                                    >
                                        수정
                                    </button>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default ProductManagementPage;