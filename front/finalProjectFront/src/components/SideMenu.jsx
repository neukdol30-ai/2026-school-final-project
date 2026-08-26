import { useState } from 'react'

import {
    LayoutDashboard,
    Building2,
    Users,
    Package,
    RefreshCcw,
    Warehouse,
    ShoppingCart,
    ClipboardCheck,
    Truck,
    Boxes,
    FileText,
    Search,
    ChevronDown,
    ChevronRight,
    X,
} from 'lucide-react'


function Sidemenu({
                      isOpen,
                      onClose,
                      currentPage,
                      onMenuClick,
                  }) {

    const [openMenus, setOpenMenus] = useState({
        company: true,
        master: true,
        purchase: true,
        receiving: true,
        inventory: true,
        sales: true,
        shipping: true,
        search: true,
    })


    const toggleMenu = (menu) => {

        setOpenMenus((prev) => ({
            ...prev,
            [menu]: !prev[menu],
        }))

    }


    const handleMenuClick = (page) => {

        if (onMenuClick) {
            onMenuClick(page)
        }

        if (onClose) {
            onClose()
        }

    }


    return (
        <>

            {/* 모바일 배경 */}
            <div
                className={`sidebar-overlay ${
                    isOpen ? 'show' : ''
                }`}
                onClick={onClose}
            />


            {/* 사이드 메뉴 */}
            <aside
                className={`sidebar ${
                    isOpen ? 'open' : ''
                }`}
            >

                {/* =========================
            사이드바 헤더
        ========================== */}

                <div className="sidebar-header">

                    <div className="sidebar-logo">

                        <div className="logo-icon">
                            ERP
                        </div>

                        <div className="logo-text">

                            <strong>
                                Food ERP
                            </strong>

                            <span>
                식자재 관리 시스템
              </span>

                        </div>

                    </div>


                    <button
                        type="button"
                        className="sidebar-close"
                        onClick={onClose}
                        aria-label="메뉴 닫기"
                    >
                        <X size={20} />
                    </button>

                </div>


                {/* =========================
            회사 정보
        ========================== */}

                <div className="sidebar-company">

                    <div className="company-icon">
                        <Building2 size={18} />
                    </div>

                    <div>

                        <strong>
                            우리식자재
                        </strong>

                        <span>
              식자재 관리
            </span>

                    </div>

                </div>


                {/* =========================
            메뉴
        ========================== */}

                <nav className="sidebar-nav">


                    {/* =========================
              대시보드
          ========================== */}

                    <button
                        type="button"
                        className={`menu-item ${
                            currentPage === 'dashboard'
                                ? 'active'
                                : ''
                        }`}
                        onClick={() =>
                            handleMenuClick('dashboard')
                        }
                    >

                        <LayoutDashboard size={19} />

                        <span>
              대시보드
            </span>

                    </button>


                    {/* =========================
              회사 / 사용자
          ========================== */}

                    <MenuGroup
                        title="회사 / 사용자"
                        icon={<Building2 size={19} />}
                        isOpen={openMenus.company}
                        onToggle={() => toggleMenu('company')}
                    >

                        <SubMenu
                            icon={<Building2 size={16} />}
                            title="회사"
                            active={currentPage === 'company'}
                            onClick={() =>
                                handleMenuClick('company')
                            }
                        />

                        <SubMenu
                            icon={<Users size={16} />}
                            title="사용자"
                            active={currentPage === 'users'}
                            onClick={() =>
                                handleMenuClick('users')
                            }
                        />

                    </MenuGroup>


                    {/* =========================
              기준정보
          ========================== */}

                    <MenuGroup
                        title="기준정보"
                        icon={<Package size={19} />}
                        isOpen={openMenus.master}
                        onToggle={() => toggleMenu('master')}
                    >

                        <SubMenu
                            icon={<Users size={16} />}
                            title="거래처"
                            active={currentPage === 'partners'}
                            onClick={() =>
                                handleMenuClick('partners')
                            }
                        />

                        <SubMenu
                            icon={<Package size={16} />}
                            title="상품"
                            active={currentPage === 'products'}
                            onClick={() =>
                                handleMenuClick('products')
                            }
                        />

                        <SubMenu
                            icon={<RefreshCcw size={16} />}
                            title="상품 단위 / 환산"
                            active={currentPage === 'product-units'}
                            onClick={() =>
                                handleMenuClick('product-units')
                            }
                        />

                        <SubMenu
                            icon={<Warehouse size={16} />}
                            title="창고"
                            active={currentPage === 'warehouses'}
                            onClick={() =>
                                handleMenuClick('warehouses')
                            }
                        />

                        <SubMenu
                            icon={<Boxes size={16} />}
                            title="LOT"
                            active={currentPage === 'lots'}
                            onClick={() =>
                                handleMenuClick('lots')
                            }
                        />

                    </MenuGroup>


                    {/* =========================
              구매
          ========================== */}

                    <MenuGroup
                        title="구매"
                        icon={<ShoppingCart size={19} />}
                        isOpen={openMenus.purchase}
                        onToggle={() => toggleMenu('purchase')}
                    >

                        <SubMenu
                            icon={<FileText size={16} />}
                            title="발주"
                            active={currentPage === 'purchase-orders'}
                            onClick={() =>
                                handleMenuClick('purchase-orders')
                            }
                        />

                        <SubMenu
                            icon={<ClipboardCheck size={16} />}
                            title="발주 승인"
                            active={currentPage === 'purchase-approval'}
                            onClick={() =>
                                handleMenuClick('purchase-approval')
                            }
                        />

                    </MenuGroup>


                    {/* =========================
              입고
          ========================== */}

                    <MenuGroup
                        title="입고"
                        icon={<Truck size={19} />}
                        isOpen={openMenus.receiving}
                        onToggle={() => toggleMenu('receiving')}
                    >

                        <SubMenu
                            icon={<Truck size={16} />}
                            title="입고"
                            active={currentPage === 'receiving'}
                            onClick={() =>
                                handleMenuClick('receiving')
                            }
                        />

                        <SubMenu
                            icon={<FileText size={16} />}
                            title="입고 품목"
                            active={currentPage === 'receiving-items'}
                            onClick={() =>
                                handleMenuClick('receiving-items')
                            }
                        />

                        <SubMenu
                            icon={<ClipboardCheck size={16} />}
                            title="입고 확정"
                            active={currentPage === 'receiving-confirm'}
                            onClick={() =>
                                handleMenuClick('receiving-confirm')
                            }
                        />

                    </MenuGroup>


                    {/* =========================
              재고
          ========================== */}

                    <MenuGroup
                        title="재고"
                        icon={<Boxes size={19} />}
                        isOpen={openMenus.inventory}
                        onToggle={() => toggleMenu('inventory')}
                    >

                        <SubMenu
                            icon={<Boxes size={16} />}
                            title="현재 재고"
                            active={currentPage === 'inventory'}
                            onClick={() =>
                                handleMenuClick('inventory')
                            }
                        />

                        <SubMenu
                            icon={<FileText size={16} />}
                            title="재고 이력"
                            active={currentPage === 'inventory-history'}
                            onClick={() =>
                                handleMenuClick('inventory-history')
                            }
                        />

                        <SubMenu
                            icon={<ClipboardCheck size={16} />}
                            title="재고 실사"
                            active={currentPage === 'inventory-count'}
                            onClick={() =>
                                handleMenuClick('inventory-count')
                            }
                        />

                        <SubMenu
                            icon={<FileText size={16} />}
                            title="재고 실사 품목"
                            active={currentPage === 'inventory-count-items'}
                            onClick={() =>
                                handleMenuClick('inventory-count-items')
                            }
                        />

                        <SubMenu
                            icon={<RefreshCcw size={16} />}
                            title="재고 조정"
                            active={currentPage === 'inventory-adjustment'}
                            onClick={() =>
                                handleMenuClick('inventory-adjustment')
                            }
                        />

                    </MenuGroup>


                    {/* =========================
              판매
          ========================== */}

                    <MenuGroup
                        title="판매"
                        icon={<ShoppingCart size={19} />}
                        isOpen={openMenus.sales}
                        onToggle={() => toggleMenu('sales')}
                    >

                        <SubMenu
                            icon={<FileText size={16} />}
                            title="판매 주문"
                            active={currentPage === 'sales-orders'}
                            onClick={() =>
                                handleMenuClick('sales-orders')
                            }
                        />

                        <SubMenu
                            icon={<FileText size={16} />}
                            title="판매 주문 품목"
                            active={currentPage === 'sales-order-items'}
                            onClick={() =>
                                handleMenuClick('sales-order-items')
                            }
                        />

                    </MenuGroup>


                    {/* =========================
              출고
          ========================== */}

                    <MenuGroup
                        title="출고"
                        icon={<Truck size={19} />}
                        isOpen={openMenus.shipping}
                        onToggle={() => toggleMenu('shipping')}
                    >

                        <SubMenu
                            icon={<Truck size={16} />}
                            title="출고"
                            active={currentPage === 'shipments'}
                            onClick={() =>
                                handleMenuClick('shipments')
                            }
                        />

                        <SubMenu
                            icon={<FileText size={16} />}
                            title="출고 품목"
                            active={currentPage === 'shipment-items'}
                            onClick={() =>
                                handleMenuClick('shipment-items')
                            }
                        />

                        <SubMenu
                            icon={<ClipboardCheck size={16} />}
                            title="출고 확정"
                            active={currentPage === 'shipment-confirm'}
                            onClick={() =>
                                handleMenuClick('shipment-confirm')
                            }
                        />

                    </MenuGroup>


                    {/* =========================
              조회
          ========================== */}

                    <MenuGroup
                        title="조회"
                        icon={<Search size={19} />}
                        isOpen={openMenus.search}
                        onToggle={() => toggleMenu('search')}
                    >

                        <SubMenu
                            icon={<Search size={16} />}
                            title="통합 검색"
                            active={currentPage === 'search'}
                            onClick={() =>
                                handleMenuClick('search')
                            }
                        />

                    </MenuGroup>

                </nav>


                {/* =========================
            사이드바 하단
        ========================== */}

                <div className="sidebar-footer">

          <span>
            Food ERP
          </span>

                    <span>
            v1.0.0
          </span>

                </div>

            </aside>

        </>
    )
}


/* =========================================
   메뉴 그룹
========================================= */

function MenuGroup({
                       title,
                       icon,
                       isOpen,
                       onToggle,
                       children,
                   }) {

    return (

        <div className="menu-group">

            <button
                type="button"
                className="menu-group-title"
                onClick={onToggle}
            >

                <div className="menu-group-left">

                    {icon}

                    <span>
            {title}
          </span>

                </div>


                {isOpen ? (
                    <ChevronDown size={17} />
                ) : (
                    <ChevronRight size={17} />
                )}

            </button>


            {isOpen && (

                <div className="submenu">

                    {children}

                </div>

            )}

        </div>

    )
}


/* =========================================
   하위 메뉴
========================================= */

function SubMenu({
                     icon,
                     title,
                     active,
                     onClick,
                 }) {

    return (

        <button
            type="button"
            className={`submenu-item ${
                active ? 'active' : ''
            }`}
            onClick={onClick}
        >

            {icon}

            <span>
        {title}
      </span>

        </button>

    )
}


export default Sidemenu