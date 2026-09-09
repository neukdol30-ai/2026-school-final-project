import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import SalesOrderCreateForm from "./components/SalesOrderCreateForm";
import { createSalesOrder } from "./js/salesOrderApi";
import "./css/SalesOrderCommon.css";
import "./css/SalesOrderCreateForm.css";

function SalesOrderCreatePage() {
  const navigate = useNavigate();

  const [createLoading, setCreateLoading] = useState(false);
  const [validationErrors, setValidationErrors] = useState([]);
  const [error, setError] = useState("");

  async function handleCreateSalesOrder(requestData) {
    setError("");
    setValidationErrors([]);
    setCreateLoading(true);

    try {
      await createSalesOrder(requestData);

      navigate("/sales-orders", {
        replace: true,
        state: {
          successMessage: "판매주문이 등록되었습니다.",
        },
      });

      return true;
    } catch (error) {
      const nextValidationErrors = error.validationErrors || [];

      setValidationErrors(nextValidationErrors);

      if (nextValidationErrors.length === 0) {
        setError(error.message);
      }

      return false;
    } finally {
      setCreateLoading(false);
    }
  }

  return (
    <section className="page sales-order-page">
      <div className="page-header sales-order-page-header">
        <div>
          <h1>판매주문 등록</h1>
          <p>고객의 주문 품목과 판매단가를 등록합니다.</p>
        </div>

        <Link className="sales-order-list-link" to="/sales-orders">
          목록으로
        </Link>
      </div>

      {error && (
        <p className="sales-order-api-error" role="alert">
          {error}
        </p>
      )}

      <SalesOrderCreateForm
        onCreate={handleCreateSalesOrder}
        createLoading={createLoading}
        validationErrors={validationErrors}
      />
    </section>
  );
}

export default SalesOrderCreatePage;
