import { Navigate, Route, Routes } from "react-router-dom";
import MainLayout from "./components/layout/MainLayout";
import DashboardPage from "./pages/dashboard/DashboardPage";
import EmptyPage from "./pages/EmptyPage";

import LoginPage from "./pages/auth/LoginPage.jsx";
import ProtectedRoute from "./pages/auth/ProtectedRoute.jsx";

import PurchaseOrderListPage from "./pages/purchase/PurchaseOrderListPage.jsx";
import PurchaseOrderDetailPage from "./pages/purchase/PurchaseOrderDetailPage.jsx";

import SalesOrderListPage from "./pages/sales/SalesOrderListPage";
import SalesOrderCreatePage from "./pages/sales/SalesOrderCreatePage";

import OutboundListPage from "./pages/outbound/OutboundListPage";
import OutboundCreatePage from "./pages/outbound/OutboundCreatePage";
import OutboundDetailPage from "./pages/outbound/OutboundDetailPage";

function App() {
  return (
    <Routes>
      {/* 로그인하지 않은 사용자가 보는 화면 */}
      <Route path="/login" element={<LoginPage />} />

      {/* 로그인한 사용자만 아래 ERP 화면에 들어올 수 있음 */}
      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />

          <Route path="/products" element={<EmptyPage title="상품관리" />} />
          <Route path="/partners" element={<EmptyPage title="거래처관리" />} />
          <Route path="/warehouses" element={<EmptyPage title="창고관리" />} />
          <Route path="/units" element={<EmptyPage title="단위관리" />} />

          <Route path="/purchase-orders" element={<PurchaseOrderListPage />} />
          <Route
            path="/purchase-orders/:purchaseOrderId"
            element={<PurchaseOrderDetailPage />}
          />

          <Route path="/inbounds" element={<EmptyPage title="입고관리" />} />
          <Route path="/inventory" element={<EmptyPage title="재고 / LOT" />} />
          <Route path="/stocktakes" element={<EmptyPage title="재고실사" />} />

          <Route path="/sales-orders" element={<SalesOrderListPage />} />
          <Route path="/sales-orders/new" element={<SalesOrderCreatePage />} />

          <Route path="/outbounds" element={<OutboundListPage />} />
          <Route path="/outbounds/new" element={<OutboundCreatePage />} />
          <Route
            path="/outbounds/:outboundId"
            element={<OutboundDetailPage />}
          />

          <Route
            path="/settings"
            element={<EmptyPage title="사용자 / 권한 설정" />}
          />
        </Route>
      </Route>

      {/* 존재하지 않는 주소는 로그인 화면으로 */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
