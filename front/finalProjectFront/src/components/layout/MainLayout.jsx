import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar"
import Header from "./Header"

function MainLayout() {

  return (
    <div className="erp-layout">
      <Sidebar />

      <div className="erp-main">
        <Header />
        <main className="erp-content">
          <Outlet />
        </main>
      </div>

    </div>
  )
}

export default MainLayout;