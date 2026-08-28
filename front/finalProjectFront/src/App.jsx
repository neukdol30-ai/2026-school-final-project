import { Navigate, Route, Routes } from "react-router-dom";
import MainLayout from "./components/layout/MainLayout";
import DashboardPage from "./pages/dashboard/DashboardPage";
import EmptyPage from "./pages/EmptyPage";
import SalesOrderListPage from "./pages/sales/SalesOrderListPage";
import OutboundListPage from "./pages/outbound/OutboundListPage";

function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/products" element={<EmptyPage title="상품관리" />} />
        <Route path="/partners" element={<EmptyPage title="거래처관리" />} />
        <Route path="/warehouses" element={<EmptyPage title="창고관리" />} />
        <Route path="/units" element={<EmptyPage title="단위관리" />} />
        <Route
          path="/purchase-orders"
          element={<EmptyPage title="구매 / 발주" />}
        />
        <Route path="/inbounds" element={<EmptyPage title="입고관리" />} />
        <Route path="/inventory" element={<EmptyPage title="재고 / LOT" />} />
        <Route path="/stocktakes" element={<EmptyPage title="재고실사" />} />
        <Route path="/sales-orders" element={<SalesOrderListPage />} />
        <Route path="/outbounds" element={<OutboundListPage />} />
        <Route
          path="/settings"
          element={<EmptyPage title="사용자 / 권한 설정" />}
        />
      </Route>
    </Routes>
  );
}

export default App;
