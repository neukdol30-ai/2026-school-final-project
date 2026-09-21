import {
    useEffect,
    useState,
} from "react";
import {
    requestWarehouses,
    requestCreateWarehouse,
    requestUpdateWarehouse,
    requestDeactivateWarehouse,
} from "../../api/warehouseManagementApi.js";
import WarehouseSearchForm
    from "./WarehouseSearchForm.jsx";
import WarehouseForm
    from "./WarehouseForm.jsx";
import "./WarehouseManagementPage.css";

const INITIAL_FILTERS = {
    keyword: "",
    useYn: "",
};

const INITIAL_FORM = {
    warehouseCode: "",
    warehouseName: "",
    postalCode: "",
    address1: "",
    address2: "",
    description: "",
};

function getWarehouseAddress(warehouse) {
    const address = [
        warehouse.address1,
        warehouse.address2,
    ]
        .filter(Boolean)
        .join(" ");

    return address || "-";
}

function WarehouseManagementPage() {
    const [filters, setFilters] =
        useState(INITIAL_FILTERS);

    const [warehouses, setWarehouses] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [errorMessage, setErrorMessage] =
        useState("");

    const [formOpen, setFormOpen] =
        useState(false);

    const [editingWarehouseId, setEditingWarehouseId] =
        useState(null);

    const [form, setForm] =
        useState(INITIAL_FORM);

    const [saving, setSaving] =
        useState(false);

    const [successMessage, setSuccessMessage] =
        useState("");

    useEffect(() => {
        let cancelled = false;

        requestWarehouses(INITIAL_FILTERS)
            .then((data) => {
                if (!cancelled) {
                    setWarehouses(data);
                }
            })
            .catch((error) => {
                if (!cancelled) {
                    setWarehouses([]);

                    setErrorMessage(
                        error instanceof Error
                            ? error.message
                            : "창고 목록을 불러오지 못했습니다.",
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
    }, []);

    async function loadWarehouses(nextFilters) {
        setLoading(true);
        setErrorMessage("");

        try {
            const data =
                await requestWarehouses(
                    nextFilters,
                );

            setWarehouses(data);
        } catch (error) {
            setWarehouses([]);

            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "창고 목록을 불러오지 못했습니다.",
            );
        } finally {
            setLoading(false);
        }
    }

    function handleFilterChange(event) {
        const { name, value } = event.target;

        setFilters((previousFilters) => ({
            ...previousFilters,
            [name]: value,
        }));
    }

    function handleSearch(event) {
        event.preventDefault();

        loadWarehouses(filters);
    }

    function handleReset() {
        setFilters(INITIAL_FILTERS);

        loadWarehouses(INITIAL_FILTERS);
    }

    function openCreateForm() {
        setEditingWarehouseId(null);
        setForm(INITIAL_FORM);
        setErrorMessage("");
        setSuccessMessage("");
        setFormOpen(true);
    }

    function openEditForm(warehouse) {
        setEditingWarehouseId(
            warehouse.warehouseId,
        );

        setForm({
            warehouseCode:
                warehouse.warehouseCode ?? "",
            warehouseName:
                warehouse.warehouseName ?? "",
            postalCode:
                warehouse.postalCode ?? "",
            address1:
                warehouse.address1 ?? "",
            address2:
                warehouse.address2 ?? "",
            description:
                warehouse.description ?? "",
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
        setEditingWarehouseId(null);
        setForm(INITIAL_FORM);
    }

    function handleFormChange(event) {
        const { name, value } = event.target;

        setForm((previousForm) => ({
            ...previousForm,
            [name]: value,
        }));
    }

    async function handleSaveWarehouse(event) {
        event.preventDefault();

        const warehouseData = {
            warehouseCode:
                form.warehouseCode.trim(),
            warehouseName:
                form.warehouseName.trim(),
            postalCode:
                form.postalCode.trim() || null,
            address1:
                form.address1.trim() || null,
            address2:
                form.address2.trim() || null,
            description:
                form.description.trim() || null,
        };

        if (
            !warehouseData.warehouseCode ||
            !warehouseData.warehouseName
        ) {
            setErrorMessage(
                "창고코드와 창고명을 모두 입력해주세요.",
            );

            return;
        }

        setSaving(true);
        setErrorMessage("");
        setSuccessMessage("");

        try {
            if (editingWarehouseId) {
                await requestUpdateWarehouse(
                    editingWarehouseId,
                    warehouseData,
                );

                setSuccessMessage(
                    "창고 정보가 수정되었습니다.",
                );
            } else {
                await requestCreateWarehouse(
                    warehouseData,
                );

                setSuccessMessage(
                    "창고가 등록되었습니다.",
                );
            }

            setFormOpen(false);
            setEditingWarehouseId(null);
            setForm(INITIAL_FORM);

            await loadWarehouses(filters);
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "창고 정보를 저장하지 못했습니다.",
            );
        } finally {
            setSaving(false);
        }
    }

    async function handleDeactivateWarehouse(
        warehouse,
    ) {
        if (warehouse.useYn === "N") {
            return;
        }

        const confirmed = window.confirm(
            `'${warehouse.warehouseName}' 창고를 비활성화하시겠습니까?`,
        );

        if (!confirmed) {
            return;
        }

        setErrorMessage("");
        setSuccessMessage("");

        try {
            await requestDeactivateWarehouse(
                warehouse.warehouseId,
            );

            if (
                editingWarehouseId ===
                warehouse.warehouseId
            ) {
                setFormOpen(false);
                setEditingWarehouseId(null);
                setForm(INITIAL_FORM);
            }

            setSuccessMessage(
                "창고가 비활성화되었습니다.",
            );

            await loadWarehouses(filters);
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "창고를 비활성화하지 못했습니다.",
            );
        }
    }

    return (
        <div className="page warehouse-page">
            <div className="warehouse-header">
                <div>
                    <h1>창고관리</h1>

                    <p>
                        회사에서 사용하는 창고 정보를 관리합니다.
                    </p>
                </div>

                <button
                    type="button"
                    className="warehouse-primary-button"
                    onClick={openCreateForm}
                >
                    창고 등록
                </button>
            </div>

            {formOpen && (
                <WarehouseForm
                    form={form}
                    saving={saving}
                    editing={editingWarehouseId !== null}
                    onChange={handleFormChange}
                    onSubmit={handleSaveWarehouse}
                    onCancel={closeForm}
                />
            )}

            <WarehouseSearchForm
                filters={filters}
                loading={loading}
                onChange={handleFilterChange}
                onSearch={handleSearch}
                onReset={handleReset}
            />

            {successMessage && (
                <p className="warehouse-message success">
                    {successMessage}
                </p>
            )}

            {errorMessage && (
                <p
                    className="warehouse-message error"
                    role="alert"
                >
                    {errorMessage}
                </p>
            )}

            <div className="warehouse-result-summary">
                조회 결과 {warehouses.length}건
            </div>

            <div className="warehouse-table-wrap">
                <table className="warehouse-table">
                    <thead>
                    <tr>
                        <th>창고코드</th>
                        <th>창고명</th>
                        <th>우편번호</th>
                        <th>주소</th>
                        <th>설명</th>
                        <th>사용상태</th>
                        <th>관리</th>
                    </tr>
                    </thead>

                    <tbody>
                    {loading ? (
                        <tr>
                            <td
                                colSpan="7"
                                className="warehouse-empty-row"
                            >
                                창고 목록을 불러오는 중입니다.
                            </td>
                        </tr>
                    ) : warehouses.length === 0 ? (
                        <tr>
                            <td
                                colSpan="7"
                                className="warehouse-empty-row"
                            >
                                조회된 창고가 없습니다.
                            </td>
                        </tr>
                    ) : (
                        warehouses.map((warehouse) => (
                            <tr key={warehouse.warehouseId}>
                                <td>
                                    {warehouse.warehouseCode}
                                </td>

                                <td>
                                    {warehouse.warehouseName}
                                </td>

                                <td>
                                    {warehouse.postalCode ?? "-"}
                                </td>

                                <td>
                                    {getWarehouseAddress(
                                        warehouse,
                                    )}
                                </td>

                                <td>
                                    {warehouse.description ?? "-"}
                                </td>

                                <td>
                                        <span
                                            className={
                                                warehouse.useYn === "Y"
                                                    ? "warehouse-status active"
                                                    : "warehouse-status inactive"
                                            }
                                        >
                                            {warehouse.useYn === "Y"
                                                ? "사용"
                                                : "미사용"}
                                        </span>
                                </td>

                                <td>
                                    <div className="warehouse-row-actions">
                                        <button
                                            type="button"
                                            className="warehouse-edit-button"
                                            onClick={() =>
                                                openEditForm(warehouse)
                                            }
                                        >
                                            수정
                                        </button>

                                        {warehouse.useYn === "Y" && (
                                            <button
                                                type="button"
                                                className="warehouse-deactivate-button"
                                                onClick={() =>
                                                    handleDeactivateWarehouse(
                                                        warehouse,
                                                    )
                                                }
                                            >
                                                비활성화
                                            </button>
                                        )}
                                    </div>
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

export default WarehouseManagementPage;