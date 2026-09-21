import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import SalesOrderCreateForm from "./components/SalesOrderCreateForm";
import {
  getSalesOrderDetail,
  updateSalesOrder,
} from "./js/salesOrderApi";
import "./css/SalesOrderCommon.css";
import "./css/SalesOrderCreateForm.css";

function SalesOrderEditPage() {
  const { salesOrderId } = useParams();
  const navigate = useNavigate();
  const [initialData, setInitialData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [validationErrors, setValidationErrors] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadSalesOrder() {
      try {
        setLoading(true);
        setError("");

        const detail = await getSalesOrderDetail(salesOrderId);

        if (detail.orderStatus !== "DRAFT") {
          throw new Error("작성중인 판매주문만 수정할 수 있습니다.");
        }

        setInitialData({
          customerId: String(detail.customerId),
          items: detail.items.map((item) => ({
            productUnitId: String(item.productUnitId),
            orderedQty: String(item.orderedQty),
            unitPrice: String(item.unitPrice),
          })),
        });
      } catch (requestError) {
        setError(requestError.message);
      } finally {
        setLoading(false);
      }
    }

    loadSalesOrder();
  }, [salesOrderId]);

  async function handleUpdate(requestData) {
    try {
      setSaving(true);
      setError("");
      setValidationErrors([]);

      await updateSalesOrder(salesOrderId, requestData);

      navigate("/sales-orders", {
        replace: true,
        state: {
          successMessage: "판매주문이 수정되었습니다.",
        },
      });

      return true;
    } catch (requestError) {
      const nextValidationErrors = requestError.validationErrors || [];
      setValidationErrors(nextValidationErrors);

      if (nextValidationErrors.length === 0) {
        setError(requestError.message);
      }

      return false;
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="page sales-order-page">
      <div className="page-header sales-order-page-header">
        <div>
          <h1>판매주문 수정</h1>
          <p>작성중인 판매주문의 거래처, 수량과 판매단가를 수정합니다.</p>
        </div>

        <Link className="sales-order-list-link" to="/sales-orders">
          목록으로
        </Link>
      </div>

      {error && <p className="sales-order-api-error" role="alert">{error}</p>}

      {loading && <p className="empty-message">판매주문 정보를 불러오는 중입니다.</p>}

      {!loading && initialData && (
        <SalesOrderCreateForm
          onCreate={handleUpdate}
          createLoading={saving}
          validationErrors={validationErrors}
          initialData={initialData}
          title="판매주문 수정"
          description="작성중 상태에서만 주문 품목과 판매단가를 수정할 수 있습니다."
          submitLabel="판매주문 수정"
        />
      )}
    </section>
  );
}

export default SalesOrderEditPage;
