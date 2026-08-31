import { Navigate, Route, Routes } from "react-router-dom"
import MainLayout from "./components/layout/MainLayout";
import DashboardPage from "./pages/dashboard/DashboardPage";
import EmptyPage from "./pages/EmptyPage";
import SupportPage from "./pages/support/SupportPage";
import SupportWritePage from "./pages/support/SupportWritePage";
import SettingsPage from "./pages/settings/SettingsPage"; // 1. 임포트 추가

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
                <Route path="/purchase-orders" element={<EmptyPage title="구매 / 발주" />} />
                <Route path="/inbounds" element={<EmptyPage title="입고관리" />} />
                <Route path="/inventory" element={<EmptyPage title="재고 / LOT" />} />
                <Route path="/stocktakes" element={<EmptyPage title="재고실사" />} />
                <Route path="/sales-orders" element={<EmptyPage title="판매주문" />} />
                <Route path="/outbounds" element={<EmptyPage title="출고관리" />} />

                {/* 2. EmptyPage 대신 SettingsPage 연결 */}
                <Route path="/settings" element={<SettingsPage />} />

                <Route path="/support" element={<SupportPage />} />
                <Route path="/support/write" element={<SupportWritePage />} />
            </Route>
        </Routes>
    )
}

export default App;