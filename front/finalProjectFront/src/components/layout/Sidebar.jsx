import { NavLink } from "react-router-dom";
import {
  FiBox,
  FiClipboard,
  FiDatabase,
  FiHome,
  FiLogIn,
  FiLogOut,
  FiPackage,
  FiSettings,
  FiShoppingCart,
  FiTruck,
} from "react-icons/fi";

function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <strong>1st ERP</strong>
        <span>물류 / 유통 관리</span>
      </div>

      <nav className="sidebar-nav">
        <NavLink to="/dashboard" className={({isActive}) => 
        isActive ? "sidebar-link active" : "sidebar-link"
      }
      >
        <FiHome />
        <span>Dashboard</span>
      </NavLink>

      <p className="sidebar-group-title">기준정보</p>

       <NavLink
          to="/products"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <FiPackage />
          <span>상품관리</span>
        </NavLink>

        <NavLink
          to="/partners"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <FiDatabase />
          <span>거래처관리</span>
        </NavLink>

        <NavLink
          to="/warehouses"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <FiBox />
          <span>창고관리</span>
        </NavLink>

        <NavLink
          to="/units"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <FiBox />
          <span>단위관리</span>
        </NavLink>

        <p className="sidebar-group-title">물류업무</p>

        <NavLink
          to="/purchase-orders"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <FiShoppingCart />
          <span>구매 / 발주</span>
        </NavLink>

        <NavLink
          to="/inbounds"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <FiLogIn />
          <span>입고</span>
        </NavLink>

        <NavLink
          to="/inventory"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <FiBox />
          <span>재고 / LOT</span>
        </NavLink>

        <NavLink
          to="/stocktakes"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <FiClipboard />
          <span>재고실사</span>
        </NavLink>

        <NavLink
          to="/sales-orders"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <FiTruck />
          <span>판매주문</span>
        </NavLink>

        <NavLink
          to="/outbounds"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <FiLogOut />
          <span>출고</span>
        </NavLink>

        <p className="sidebar-group-title">시스템</p>

        <NavLink
          to="/settings"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <FiSettings />
          <span>사용자 / 권한</span>
        </NavLink>
      </nav>
    </aside>
  );
}

export default Sidebar;