import { useEffect, useMemo, useState } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import {
  LayoutDashboard,
  Boxes,
  Building2,
  ShoppingCart,
  ClipboardList,
  Warehouse,
  CreditCard,
  FileText,
  BarChart3,
  HardHat,
  ChevronRight,
  PanelLeftClose,
  PanelLeftOpen,
  Package,
  Wallet,
} from 'lucide-react'

const sections = [
  {
    id: 'overview',
    label: 'Overview',
    items: [
      { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
    ],
  },
  {
    id: 'catalog',
    label: 'Catalog',
    items: [
      { to: '/suppliers', label: 'Suppliers', icon: Building2 },
      { to: '/materials', label: 'Materials', icon: Boxes },
    ],
  },
  {
    id: 'operations',
    label: 'Operations',
    items: [
      { to: '/inventory', label: 'Inventory', icon: Warehouse },
      { to: '/cart', label: 'Cart', icon: ShoppingCart },
      { to: '/orders', label: 'Orders', icon: ClipboardList },
    ],
  },
  {
    id: 'finance',
    label: 'Finance',
    items: [
      { to: '/payments', label: 'Payments', icon: CreditCard },
      { to: '/invoices', label: 'Invoices', icon: FileText },
    ],
  },
  {
    id: 'insights',
    label: 'Insights',
    items: [
      { to: '/reports', label: 'Reports', icon: BarChart3 },
    ],
  },
]

function sectionContainsPath(section, pathname) {
  return section.items.some((item) => {
    if (item.end) return pathname === item.to
    return pathname === item.to || pathname.startsWith(`${item.to}/`)
  })
}

export default function Sidebar({
  mobileOpen,
  collapsed,
  onNavigate,
  onToggleCollapse,
}) {
  const { pathname } = useLocation()

  const initialOpen = useMemo(() => {
    const map = {}
    sections.forEach((section) => {
      map[section.id] = sectionContainsPath(section, pathname) || section.id === 'overview'
    })
    return map
  }, [pathname])

  const [openGroups, setOpenGroups] = useState(initialOpen)

  useEffect(() => {
    setOpenGroups((prev) => {
      const next = { ...prev }
      sections.forEach((section) => {
        if (sectionContainsPath(section, pathname)) next[section.id] = true
      })
      return next
    })
  }, [pathname])

  const toggleGroup = (id) => {
    if (collapsed) return
    setOpenGroups((prev) => ({ ...prev, [id]: !prev[id] }))
  }

  return (
    <aside className={`sidebar ${mobileOpen ? 'mobile-open' : ''}`}>
      <div className="sidebar-brand">
        <div className="brand-mark"><HardHat size={22} /></div>
        <div className="brand-copy">
          <div className="brand-title">BuildHub</div>
          <div className="brand-subtitle">Construction Cloud</div>
        </div>
      </div>

      <div className="sidebar-scroll">
        {sections.map((section) => {
          const isOpen = collapsed || openGroups[section.id]
          const GroupIcon = section.id === 'finance' ? Wallet : section.id === 'catalog' ? Package : null
          return (
            <div key={section.id} className="nav-section">
              <button
                type="button"
                className={`nav-group-btn ${isOpen ? 'open' : ''}`}
                onClick={() => toggleGroup(section.id)}
                aria-expanded={isOpen}
                title={section.label}
              >
                {GroupIcon ? <GroupIcon size={14} /> : null}
                <span>{section.label}</span>
                <ChevronRight size={14} className="chev" />
              </button>

              <div className={`nav-group-items ${isOpen ? 'open' : ''}`}>
                <div className="nav-group-items-inner">
                  {section.items.map((item) => {
                    const Icon = item.icon
                    return (
                      <NavLink
                        key={item.to}
                        to={item.to}
                        end={item.end}
                        className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
                        onClick={onNavigate}
                        title={item.label}
                      >
                        <Icon />
                        <span>{item.label}</span>
                      </NavLink>
                    )
                  })}
                </div>
              </div>
            </div>
          )
        })}
      </div>

      <button
        type="button"
        className="sidebar-collapse-btn"
        onClick={onToggleCollapse}
        aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
      >
        {collapsed ? <PanelLeftOpen size={16} /> : <PanelLeftClose size={16} />}
        <span>{collapsed ? 'Expand' : 'Collapse'}</span>
      </button>

      <div className="sidebar-footer">
        Enterprise console for materials, fulfilment, and payments.
      </div>
    </aside>
  )
}
