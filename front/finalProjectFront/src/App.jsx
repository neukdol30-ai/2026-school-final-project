import { Navigate, Route, Routes } from "react-router-dom";
import MainLayout from "./components/layout/MainLayout";
import DashboardPage from "./pages/dashboard/DashboardPage";
import EmptyPage from "./pages/EmptyPage";
import LoginPage from "./pages/auth/LoginPage.jsx";
import ProtectedRoute from "./pages/auth/ProtectedRoute.jsx";
import PurchaseOrderListPage from "./pages/purchase/PurchaseOrderListPage.jsx";
import PurchaseOrderDetailPage from "./pages/purchase/PurchaseOrderDetailPage.jsx";
import PurchaseOrderCreatePage from "./pages/purchase/PurchaseOrderCreatePage.jsx";

// React 애플리케이션의 Route 구조를 정의하는 컴포넌트다.
function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />

      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />

          <Route path="/products" element={<EmptyPage title="상품관리" />} />

          <Route path="/partners" element={<EmptyPage title="거래처관리" />} />

          <Route path="/warehouses" element={<EmptyPage title="창고관리" />} />

          <Route path="/units" element={<EmptyPage title="단위관리" />} />

          {/* /purchase-orders는 발주 여러 건을 보여주는 목록 화면이다. */}
          <Route path="/purchase-orders" element={<PurchaseOrderListPage />} />

          {/* /purchase-orders/new는 새 발주를 작성하는 등록 화면이다. */}
          <Route
            path="/purchase-orders/new"
            element={<PurchaseOrderCreatePage />}
          />

          {/* :purchaseOrderId는 매번 값이 바뀌는 URL 부분이다.
              예: /purchase-orders/1에서 purchaseOrderId = "1" */}
          <Route
            path="/purchase-orders/:purchaseOrderId"
            element={<PurchaseOrderDetailPage />}
          />

          <Route path="/inbounds" element={<EmptyPage title="입고관리" />} />

          <Route path="/inventory" element={<EmptyPage title="재고 / LOT" />} />

          <Route path="/stocktakes" element={<EmptyPage title="재고실사" />} />

          <Route
            path="/sales-orders"
            element={<EmptyPage title="판매주문" />}
          />

          <Route path="/outbounds" element={<EmptyPage title="출고관리" />} />

          <Route
            path="/settings"
            element={<EmptyPage title="사용자 / 권한 설정" />}
          />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

// main.jsx에서 App을 가져다 사용할 수 있도록 내보낸다.
export default App;
