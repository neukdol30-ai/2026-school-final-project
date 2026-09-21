function ProductUnitPanel({
                              product,
                              productUnits,
                              loading,
                              errorMessage,
                              onEdit,
                              onClose,
                              onDeactivate,
                              canUpdate,
                          }) {
    if (!product) {
        return null;
    }

    return (
        <section className="product-unit-panel">
            <div className="product-unit-heading">
                <div>
                    <h2>상품단위 관리</h2>

                    <p>
                        {product.productCode} ·{" "}
                        {product.productName}
                    </p>
                </div>

                <button
                    type="button"
                    onClick={onClose}
                >
                    닫기
                </button>
            </div>

            {errorMessage && (
                <p
                    className="product-message error"
                    role="alert"
                >
                    {errorMessage}
                </p>
            )}

            <div className="product-unit-table-wrap">
                <table className="product-unit-table">
                    <thead>
                    <tr>
                        <th>단위코드</th>
                        <th>단위명</th>
                        <th>환산수량</th>
                        <th>기준단위</th>
                        <th>사용상태</th>
                        <th>관리</th>
                    </tr>
                    </thead>

                    <tbody>
                    {loading ? (
                        <tr>
                            <td
                                colSpan="6"
                                className="product-empty-row"
                            >
                                상품단위 목록을 불러오는 중입니다.
                            </td>
                        </tr>
                    ) : productUnits.length === 0 ? (
                        <tr>
                            <td
                                colSpan="6"
                                className="product-empty-row"
                            >
                                등록된 상품단위가 없습니다.
                            </td>
                        </tr>
                    ) : (
                        productUnits.map((productUnit) => (
                            <tr
                                key={productUnit.productUnitId}
                            >
                                <td>
                                    {productUnit.unitCode}
                                </td>

                                <td>
                                    {productUnit.unitName}
                                </td>

                                <td>
                                    {Number(
                                        productUnit.conversionQty,
                                    ).toLocaleString("ko-KR")}
                                </td>

                                <td>
                                    {productUnit.isBaseYn === "Y"
                                        ? "기준단위"
                                        : "환산단위"}
                                </td>

                                <td>
                    <span
                        className={
                            productUnit.useYn === "Y"
                                ? "product-status active"
                                : "product-status inactive"
                        }
                    >
                      {productUnit.useYn === "Y"
                          ? "사용"
                          : "미사용"}
                    </span>
                                </td>

                                <td>
                                    <div className="product-unit-row-actions">
                                        <button
                                            type="button"
                                            className={
                                                canUpdate
                                                    ? "product-unit-edit-button"
                                                    : "product-unit-edit-button permission-disabled"
                                            }
                                            onClick={() =>
                                                onEdit(productUnit)
                                            }
                                            disabled={
                                                !canUpdate ||
                                                productUnit.useYn === "N"
                                            }
                                        >
                                            수정
                                        </button>

                                        <button
                                            type="button"
                                            className={
                                                canUpdate
                                                    ? "product-unit-deactivate-button"
                                                    : "product-unit-deactivate-button permission-disabled"
                                            }
                                            onClick={() =>
                                                onDeactivate(productUnit)
                                            }
                                            disabled={
                                                !canUpdate ||
                                                productUnit.useYn === "N"
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
        </section>
    );
}

export default ProductUnitPanel;
