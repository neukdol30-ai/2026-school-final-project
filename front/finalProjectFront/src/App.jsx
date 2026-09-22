import { Navigate, Route, Routes } from "react-router-dom";
import MainLayout from "./components/layout/MainLayout";
import DashboardPage from "./pages/dashboard/DashboardPage";
import LoginPage from "./pages/auth/LoginPage.jsx";
import ProtectedRoute from "./pages/auth/ProtectedRoute.jsx";

import PurchaseOrderListPage from "./pages/purchase/PurchaseOrderListPage.jsx";
import PurchaseOrderDetailPage from "./pages/purchase/PurchaseOrderDetailPage.jsx";
import PurchaseOrderCreatePage from "./pages/purchase/PurchaseOrderCreatePage.jsx";

import ProductManagementPage from "./pages/product/ProductManagementPage.jsx";
import BusinessPartnerManagementPage from "./pages/businessPartner/BusinessPartnerManagementPage.jsx";
import WarehouseManagementPage from "./pages/warehouse/WarehouseManagementPage.jsx";
import UnitManagementPage from "./pages/unit/UnitManagementPage.jsx";
import UserPermissionPage from "./pages/settings/UserPermissionPage.jsx";

import SalesOrderListPage from "./pages/sales/SalesOrderListPage";
import SalesOrderCreatePage from "./pages/sales/SalesOrderCreatePage";
import SalesOrderEditPage from "./pages/sales/SalesOrderEditPage";

import OutboundListPage from "./pages/outbound/OutboundListPage";
import OutboundCreatePage from "./pages/outbound/OutboundCreatePage";
import OutboundDetailPage from "./pages/outbound/OutboundDetailPage";

import StocktakeListPage from "./pages/stocktake/StocktakeListPage";
import StocktakeCreatePage from "./pages/stocktake/StocktakeCreatePage";
import StocktakeDetailPage from "./pages/stocktake/StocktakeDetailPage";

import InboundListPage from "./pages/inbound/InboundListPage.jsx";
import InboundEditPage from "./pages/inbound/InboundEditPage.jsx";

import InventoryPage from "./pages/inventory/InventoryPage.jsx";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />

          <Route path="/products" element={<ProductManagementPage />} />
          <Route path="/partners" element={<BusinessPartnerManagementPage />} />
          <Route path="/warehouses" element={<WarehouseManagementPage />} />
          <Route path="/units" element={<UnitManagementPage />} />

          <Route path="/purchase-orders" element={<PurchaseOrderListPage />} />
          <Route
            path="/purchase-orders/new"
            element={<PurchaseOrderCreatePage />}
          />
          <Route
            path="/purchase-orders/:purchaseOrderId/edit"
            element={<PurchaseOrderCreatePage mode="edit" />}
          />
          <Route
            path="/purchase-orders/:purchaseOrderId"
            element={<PurchaseOrderDetailPage />}
          />

          <Route path="/inbounds" element={<InboundListPage />} />
          <Route path="/inbounds/new" element={<InboundEditPage />} />
          <Route path="/inbounds/:inboundId" element={<InboundEditPage />} />

          <Route path="/inventory" element={<InventoryPage />} />

          <Route path="/stocktakes" element={<StocktakeListPage />} />
          <Route path="/stocktakes/new" element={<StocktakeCreatePage />} />
          <Route
            path="/stocktakes/:stocktakeId/edit"
            element={<StocktakeCreatePage />}
          />
          <Route
            path="/stocktakes/:stocktakeId"
            element={<StocktakeDetailPage />}
          />

          <Route path="/sales-orders" element={<SalesOrderListPage />} />
          <Route path="/sales-orders/new" element={<SalesOrderCreatePage />} />
          <Route
            path="/sales-orders/:salesOrderId/edit"
            element={<SalesOrderEditPage />}
          />

          <Route path="/outbounds" element={<OutboundListPage />} />
          <Route path="/outbounds/new" element={<OutboundCreatePage />} />
          <Route
            path="/outbounds/:outboundId/edit"
            element={<OutboundCreatePage />}
          />
          <Route
            path="/outbounds/:outboundId"
            element={<OutboundDetailPage />}
          />

          <Route path="/settings" element={<UserPermissionPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
