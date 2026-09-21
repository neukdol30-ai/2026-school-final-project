import { Navigate, Route, Routes } from "react-router-dom"
import MainLayout from "./components/layout/MainLayout";
import DashboardPage from "./pages/dashboard/DashboardPage";
import EmptyPage from "./pages/EmptyPage";
import LoginPage from "./pages/auth/LoginPage.jsx";
import ProtectedRoute from "./pages/auth/ProtectedRoute.jsx";
import ProductManagementPage from "./pages/product/ProductManagementPage.jsx";
import BusinessPartnerManagementPage
  from "./pages/businessPartner/BusinessPartnerManagementPage.jsx";
import WarehouseManagementPage
  from "./pages/warehouse/WarehouseManagementPage.jsx";
import UnitManagementPage
  from "./pages/unit/UnitManagementPage.jsx";
import UserPermissionPage
  from "./pages/settings/UserPermissionPage.jsx";


function App() {

  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<MainLayout/>}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/products" element={<ProductManagementPage />}/>
            <Route
                path="/partners"
                element={<BusinessPartnerManagementPage />}
            />
            <Route
                path="/warehouses"
                element={<WarehouseManagementPage />}
            />
            <Route
                path="/units"
                element={<UnitManagementPage />}
            />
            <Route path="/purchase-orders" element={<EmptyPage title="구매 / 발주" />} />
            <Route path="/inbounds" element={<EmptyPage title="입고관리" />} />
            <Route path="/inventory" element={<EmptyPage title="재고 / LOT" />} />
            <Route path="/stocktakes" element={<EmptyPage title="재고실사" />} />
            <Route path="/sales-orders" element={<EmptyPage title="판매주문" />} />
            <Route path="/outbounds" element={<EmptyPage title="출고관리" />} />
            <Route
                path="/settings"
                element={<UserPermissionPage />}
            />
          </Route>
        </Route>
        <Route path="*" element={<Navigate to="/login" replace/>} />
    </Routes>
  )
}

export default App;
