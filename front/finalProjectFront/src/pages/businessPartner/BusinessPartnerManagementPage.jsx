import {
    useEffect,
    useState,
} from "react";
import {
    requestBusinessPartners,
    requestCreateBusinessPartner,
    requestUpdateBusinessPartner,
    requestDeactivateBusinessPartner,
} from "../../api/businessPartnerManagementApi.js";
import BusinessPartnerSearchForm
    from "./BusinessPartnerSearchForm.jsx";
import BusinessPartnerForm
    from "./BusinessPartnerForm.jsx";
import ManagementModal
    from "../../components/common/ManagementModal.jsx";
import ManagementAccessDenied
    from "../../components/common/ManagementAccessDenied.jsx";
import { hasAuthority }
    from "../../storage/authStorage.js";
import "./BusinessPartnerManagementPage.css";

const INITIAL_FILTERS = {
    keyword: "",
    partnerType: "",
    useYn: "",
};

const INITIAL_FORM = {
    partnerCode: "",
    partnerName: "",
    businessNumber: "",
    representativeName: "",
    contactName: "",
    phone: "",
    email: "",
    postalCode: "",
    address1: "",
    address2: "",
    supplierYn: "Y",
    customerYn: "N",
};

function getPartnerTypeLabel(partner) {
    if (
        partner.supplierYn === "Y" &&
        partner.customerYn === "Y"
    ) {
        return "공급업체 / 판매처";
    }

    if (partner.supplierYn === "Y") {
        return "공급업체";
    }

    if (partner.customerYn === "Y") {
        return "판매처";
    }

    return "-";
}

function BusinessPartnerManagementPage() {
    const canRead = hasAuthority(
        "BUSINESS_PARTNER_READ",
    );
    const canCreate = hasAuthority(
        "BUSINESS_PARTNER_CREATE",
    );
    const canUpdate = hasAuthority(
        "BUSINESS_PARTNER_UPDATE",
    );
    const canDeactivate = hasAuthority(
        "BUSINESS_PARTNER_DEACTIVATE",
    );

    const [filters, setFilters] =
        useState(INITIAL_FILTERS);

    const [partners, setPartners] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [errorMessage, setErrorMessage] =
        useState("");

    const [formOpen, setFormOpen] =
        useState(false);

    const [editingPartnerId, setEditingPartnerId] =
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

        requestBusinessPartners(INITIAL_FILTERS)
            .then((data) => {
                if (!cancelled) {
                    setPartners(data);
                }
            })
            .catch((error) => {
                if (!cancelled) {
                    setPartners([]);

                    setErrorMessage(
                        error instanceof Error
                            ? error.message
                            : "거래처 목록을 불러오지 못했습니다.",
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

    async function loadPartners(nextFilters) {
        setLoading(true);
        setErrorMessage("");

        try {
            const data =
                await requestBusinessPartners(
                    nextFilters,
                );

            setPartners(data);
        } catch (error) {
            setPartners([]);

            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "거래처 목록을 불러오지 못했습니다.",
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

        loadPartners(filters);
    }

    function handleReset() {
        setFilters(INITIAL_FILTERS);

        loadPartners(INITIAL_FILTERS);
    }

    function openCreateForm() {
        setEditingPartnerId(null);
        setForm(INITIAL_FORM);
        setErrorMessage("");
        setSuccessMessage("");
        setFormOpen(true);
    }

    function openEditForm(partner) {
        setEditingPartnerId(partner.partnerId);

        setForm({
            partnerCode: partner.partnerCode ?? "",
            partnerName: partner.partnerName ?? "",
            businessNumber:
                partner.businessNumber ?? "",
            representativeName:
                partner.representativeName ?? "",
            contactName: partner.contactName ?? "",
            phone: partner.phone ?? "",
            email: partner.email ?? "",
            postalCode: partner.postalCode ?? "",
            address1: partner.address1 ?? "",
            address2: partner.address2 ?? "",
            supplierYn: partner.supplierYn ?? "N",
            customerYn: partner.customerYn ?? "N",
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
        setEditingPartnerId(null);
        setForm(INITIAL_FORM);
    }

    function handleFormChange(event) {
        const { name, value } = event.target;

        setForm((previousForm) => ({
            ...previousForm,
            [name]: value,
        }));
    }

    async function handleSaveBusinessPartner(event) {
        event.preventDefault();

        const partnerData = {
            partnerCode: form.partnerCode.trim(),
            partnerName: form.partnerName.trim(),
            businessNumber:
                form.businessNumber.trim() || null,
            representativeName:
                form.representativeName.trim() || null,
            contactName:
                form.contactName.trim() || null,
            phone: form.phone.trim() || null,
            email: form.email.trim() || null,
            postalCode:
                form.postalCode.trim() || null,
            address1:
                form.address1.trim() || null,
            address2:
                form.address2.trim() || null,
            supplierYn: form.supplierYn,
            customerYn: form.customerYn,
        };

        if (
            !partnerData.partnerCode ||
            !partnerData.partnerName
        ) {
            setErrorMessage(
                "거래처코드와 거래처명을 모두 입력해주세요.",
            );

            return;
        }

        if (
            partnerData.supplierYn === "N" &&
            partnerData.customerYn === "N"
        ) {
            setErrorMessage(
                "공급업체 또는 판매처 중 하나 이상을 선택해주세요.",
            );

            return;
        }

        setSaving(true);
        setErrorMessage("");
        setSuccessMessage("");

        try {
            if (editingPartnerId) {
                await requestUpdateBusinessPartner(
                    editingPartnerId,
                    partnerData,
                );

                setSuccessMessage(
                    "거래처 정보가 수정되었습니다.",
                );
            } else {
                await requestCreateBusinessPartner(
                    partnerData,
                );

                setSuccessMessage(
                    "거래처가 등록되었습니다.",
                );
            }

            setFormOpen(false);
            setEditingPartnerId(null);
            setForm(INITIAL_FORM);

            await loadPartners(filters);
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "거래처 정보를 저장하지 못했습니다.",
            );
        } finally {
            setSaving(false);
        }
    }

    async function handleDeactivateBusinessPartner(
        partner,
    ) {
        if (partner.useYn === "N") {
            return;
        }

        const confirmed = window.confirm(
            `'${partner.partnerName}' 거래처를 비활성화하시겠습니까?`,
        );

        if (!confirmed) {
            return;
        }

        setErrorMessage("");
        setSuccessMessage("");

        try {
            await requestDeactivateBusinessPartner(
                partner.partnerId,
            );

            if (
                editingPartnerId === partner.partnerId
            ) {
                setFormOpen(false);
                setEditingPartnerId(null);
                setForm(INITIAL_FORM);
            }

            setSuccessMessage(
                "거래처가 비활성화되었습니다.",
            );

            await loadPartners(filters);
        } catch (error) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "거래처를 비활성화하지 못했습니다.",
            );
        }
    }

    if (!canRead) {
        return (
            <div className="page business-partner-page">
                <ManagementAccessDenied resourceName="거래처" />
            </div>
        );
    }

    return (
        <div className="page business-partner-page">
            <div className="business-partner-header">
                <div>
                    <h1>거래처관리</h1>

                    <p>
                        공급업체와 판매처 정보를 관리합니다.
                    </p>
                </div>

                <button
                    type="button"
                    className={
                        canCreate
                            ? "business-partner-primary-button"
                            : "business-partner-primary-button permission-disabled"
                    }
                    onClick={openCreateForm}
                    disabled={!canCreate}
                >
                    거래처 등록
                </button>
            </div>

            {formOpen && (
                <ManagementModal
                    onClose={closeForm}
                    closeDisabled={saving}
                >
                    <BusinessPartnerForm
                        form={form}
                        saving={saving}
                        editing={editingPartnerId !== null}
                        onChange={handleFormChange}
                        onSubmit={handleSaveBusinessPartner}
                        onCancel={closeForm}
                    />
                </ManagementModal>
            )}

            <BusinessPartnerSearchForm
                filters={filters}
                loading={loading}
                onChange={handleFilterChange}
                onSearch={handleSearch}
                onReset={handleReset}
            />

            {successMessage && (
                <p className="business-partner-message success">
                    {successMessage}
                </p>
            )}

            {errorMessage && (
                <p
                    className="business-partner-message error"
                    role="alert"
                >
                    {errorMessage}
                </p>
            )}

            <div className="business-partner-result-summary">
                조회 결과 {partners.length}건
            </div>

            <div className="business-partner-table-wrap">
                <table className="business-partner-table">
                    <thead>
                    <tr>
                        <th>거래처코드</th>
                        <th>거래처명</th>
                        <th>구분</th>
                        <th>사업자등록번호</th>
                        <th>대표자명</th>
                        <th>담당자 / 연락처</th>
                        <th>사용상태</th>
                        <th>관리</th>
                    </tr>
                    </thead>

                    <tbody>
                    {loading ? (
                        <tr>
                            <td
                                colSpan="8"
                                className="business-partner-empty-row"
                            >
                                거래처 목록을 불러오는 중입니다.
                            </td>
                        </tr>
                    ) : partners.length === 0 ? (
                        <tr>
                            <td
                                colSpan="8"
                                className="business-partner-empty-row"
                            >
                                조회된 거래처가 없습니다.
                            </td>
                        </tr>
                    ) : (
                        partners.map((partner) => (
                            <tr key={partner.partnerId}>
                                <td>{partner.partnerCode}</td>

                                <td>{partner.partnerName}</td>

                                <td>
                                    {getPartnerTypeLabel(partner)}
                                </td>

                                <td>
                                    {partner.businessNumber ?? "-"}
                                </td>

                                <td>
                                    {partner.representativeName ?? "-"}
                                </td>

                                <td>
                                    <div>
                                        {partner.contactName ?? "-"}
                                    </div>

                                    <small>
                                        {partner.phone ?? "-"}
                                    </small>
                                </td>

                                <td>
                                    <span
                                        className={
                                            partner.useYn === "Y"
                                                ? "business-partner-status active"
                                                : "business-partner-status inactive"
                                        }
                                    >
                                      {partner.useYn === "Y"
                                          ? "사용"
                                          : "미사용"}
                                    </span>
                                </td>

                                <td>
                                    <div className="business-partner-row-actions">
                                        <button
                                            type="button"
                                            className={
                                                canUpdate
                                                    ? "business-partner-edit-button"
                                                    : "business-partner-edit-button permission-disabled"
                                            }
                                            onClick={() => openEditForm(partner)}
                                            disabled={!canUpdate}
                                        >
                                            수정
                                        </button>

                                        {partner.useYn === "Y" && (
                                            <button
                                                type="button"
                                                className={
                                                    canDeactivate
                                                        ? "business-partner-deactivate-button"
                                                        : "business-partner-deactivate-button permission-disabled"
                                                }
                                                onClick={() =>
                                                    handleDeactivateBusinessPartner(
                                                        partner,
                                                    )
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

export default BusinessPartnerManagementPage;
