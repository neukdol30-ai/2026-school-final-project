import {
    useEffect,
    useState,
} from "react";
import {
    requestUnits,
    requestCreateUnit,
    requestUpdateUnit,
    requestDeactivateUnit,
} from "../../api/unitManagementApi.js";
import UnitSearchForm
    from "./UnitSearchForm.jsx";
import UnitForm from "./UnitForm.jsx";
import ManagementModal
    from "../../components/common/ManagementModal.jsx";
import ManagementAccessDenied
    from "../../components/common/ManagementAccessDenied.jsx";
import { hasAuthority }
    from "../../storage/authStorage.js";
import "./UnitManagementPage.css";

const INITIAL_FILTERS = {
    keyword: "",
    useYn: "",
};

const INITIAL_FORM = {
    unitCode: "",
    unitName: "",
    description: "",
};

function UnitManagementPage() {
    const canRead = hasAuthority("UNIT_READ");
    const canCreate = hasAuthority("UNIT_CREATE");
    const canUpdate = hasAuthority("UNIT_UPDATE");
    const canDeactivate = hasAuthority(
        "UNIT_DEACTIVATE",
    );

    const [filters, setFilters] =
        useState(INITIAL_FILTERS);

    const [units, setUnits] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [errorMessage, setErrorMessage] =
        useState("");

    const [formOpen, setFormOpen] =
        useState(false);

    const [editingUnitId, setEditingUnitId] =
        useState(null);

    const [form, setForm] =
        useState(INITIAL_FORM);

    const [saving, setSaving] =
        useState(false);

    const [successMessage, setSuccessMessage] =
        useState("");

    useEffect(() => {
        if (!canRead) {
            return undefined;
        }

        let cancelled = false;

        requestUnits(INITIAL_FILTERS)
            .then((data) => {
                if (!cancelled) {
                    setUnits(data);
                }
            })
            .catch((error) => {
                if (!cancelled) {
                    setUnits([]);

                    setErrorMessage(
                        error instanceof Error
                            ? error.message
                            : "단위 목록을 불러오지 못했습니다.",
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

    async function loadUnits(nextFilters) {
        setLoading(true);
        setErrorMessage("");

        try {
            const data =
                await requestUnits(nextFilters);

            setUnits(data);
        } catch (error) {
            setUnits([]);

            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "단위 목록을 불러오지 못했습니다.",
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

        loadUnits(filters);
    }

    function handleReset() {
        setFilters(INITIAL_FILTERS);

        loadUnits(INITIAL_FILTERS);
    }

    function openCreateForm() {
        setEditingUnitId(null);
        setForm(INITIAL_FORM);
        setErrorMessage("");
        setSuccessMessage("");
        setFormOpen(true);
    }

    function openEditForm(unit) {
        setEditingUnitId(unit.unitId);

        setForm({
            unitCode: unit.unitCode ?? "",
            unitName: unit.unitName ?? "",
            description: unit.description ?? "",
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
        setEditingUnitId(null);
        setForm(INITIAL_FORM);
    }

    function handleFormChange(event) {
        const { name, value } = event.target;

        setForm((previousForm) => ({
            ...previousForm,
            [name]: value,
        }));
    }

    async function handleSaveUnit(event) {
        event.preventDefault();

        const unitData = {
            unitCode: form.unitCode.trim(),
            unitName: form.unitName.trim(),
            description:
                form.description.trim() || null,
        };

        if (
            !unitData.unitCode ||
            !unitData.unitName
        ) {
            setErrorMessage(
                "단위코드와 단위명을 모두 입력해주세요.",
            );

            return;
        }

        setSaving(true);
        setErrorMessage("");
        setSuccessMessage("");

        try {
            if (editingUnitId) {
                await requestUpdateUnit(
                    editingUnitId,
                    unitData,
                );

                setSuccessMessage(
                    "단위 정보가 수정되었습니다.",
                );
            } else {
                await requestCreateUnit(unitData);

                setSuccessMessage(
                    "단위가 등록되었습니다.",
                );
            }

            setFormOpen(false);
            setEditingUnitId(null);
            setForm(INITIAL_FORM);

            await loadUnits(filters);
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "단위 정보를 저장하지 못했습니다.",
            );
        } finally {
            setSaving(false);
        }
    }

    async function handleDeactivateUnit(unit) {
        if (unit.useYn === "N") {
            return;
        }

        const confirmed = window.confirm(
            `'${unit.unitName}' 단위를 비활성화하시겠습니까?`,
        );

        if (!confirmed) {
            return;
        }

        setErrorMessage("");
        setSuccessMessage("");

        try {
            await requestDeactivateUnit(
                unit.unitId,
            );

            if (editingUnitId === unit.unitId) {
                setFormOpen(false);
                setEditingUnitId(null);
                setForm(INITIAL_FORM);
            }

            setSuccessMessage(
                "단위가 비활성화되었습니다.",
            );

            await loadUnits(filters);
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "단위를 비활성화하지 못했습니다.",
            );
        }
    }

    if (!canRead) {
        return (
            <div className="page unit-page">
                <ManagementAccessDenied resourceName="단위" />
            </div>
        );
    }

    return (
        <div className="page unit-page">
            <div className="unit-header">
                <div>
                    <h1>단위관리</h1>

                    <p>
                        상품에서 사용하는 측정 단위를 관리합니다.
                    </p>
                </div>

                <button
                    type="button"
                    className={
                        canCreate
                            ? "unit-primary-button"
                            : "unit-primary-button permission-disabled"
                    }
                    onClick={openCreateForm}
                    disabled={!canCreate}
                >
                    단위 등록
                </button>
            </div>

            {formOpen && (
                <ManagementModal
                    onClose={closeForm}
                    closeDisabled={saving}
                >
                    <UnitForm
                        form={form}
                        saving={saving}
                        editing={editingUnitId !== null}
                        onChange={handleFormChange}
                        onSubmit={handleSaveUnit}
                        onCancel={closeForm}
                    />
                </ManagementModal>
            )}

            <UnitSearchForm
                filters={filters}
                loading={loading}
                onChange={handleFilterChange}
                onSearch={handleSearch}
                onReset={handleReset}
            />

            {successMessage && (
                <p className="unit-message success">
                    {successMessage}
                </p>
            )}

            {errorMessage && (
                <p
                    className="unit-message error"
                    role="alert"
                >
                    {errorMessage}
                </p>
            )}

            <div className="unit-result-summary">
                조회 결과 {units.length}건
            </div>

            <div className="unit-table-wrap">
                <table className="unit-table">
                    <thead>
                    <tr>
                        <th>단위코드</th>
                        <th>단위명</th>
                        <th>설명</th>
                        <th>사용상태</th>
                        <th>관리</th>
                    </tr>
                    </thead>

                    <tbody>
                    {loading ? (
                        <tr>
                            <td
                                colSpan="5"
                                className="unit-empty-row"
                            >
                                단위 목록을 불러오는 중입니다.
                            </td>
                        </tr>
                    ) : units.length === 0 ? (
                        <tr>
                            <td
                                colSpan="5"
                                className="unit-empty-row"
                            >
                                조회된 단위가 없습니다.
                            </td>
                        </tr>
                    ) : (
                        units.map((unit) => (
                            <tr key={unit.unitId}>
                                <td>{unit.unitCode}</td>

                                <td>{unit.unitName}</td>

                                <td>
                                    {unit.description ?? "-"}
                                </td>

                                <td>
                                        <span
                                            className={
                                                unit.useYn === "Y"
                                                    ? "unit-status active"
                                                    : "unit-status inactive"
                                            }
                                        >
                                            {unit.useYn === "Y"
                                                ? "사용"
                                                : "미사용"}
                                        </span>
                                </td>

                                <td>
                                    <div className="unit-row-actions">
                                        <button
                                            type="button"
                                            className={
                                                canUpdate
                                                    ? "unit-edit-button"
                                                    : "unit-edit-button permission-disabled"
                                            }
                                            onClick={() => openEditForm(unit)}
                                            disabled={!canUpdate}
                                        >
                                            수정
                                        </button>

                                        {unit.useYn === "Y" && (
                                            <button
                                                type="button"
                                                className={
                                                    canDeactivate
                                                        ? "unit-deactivate-button"
                                                        : "unit-deactivate-button permission-disabled"
                                                }
                                                onClick={() =>
                                                    handleDeactivateUnit(unit)
                                                }
                                                disabled={!canDeactivate}
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

export default UnitManagementPage;
