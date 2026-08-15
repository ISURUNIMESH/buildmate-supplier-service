import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import Sidebar from './Sidebar'
import Header from './Header'

const titles = {
  '/': ['Dashboard', 'Live command center across BuildHub services'],
  '/materials': ['Materials', 'Catalog, categories, brands and pricing'],
  '/suppliers': ['Suppliers', 'Partner companies, status and documents'],
  '/orders': ['Orders', 'Lifecycle, fulfilment and status progress'],
  '/cart': ['Cart', 'Active shopping sessions'],
  '/inventory': ['Inventory', 'Warehouse stock, reservations and alerts'],
  '/payments': ['Payments', 'Revenue, recovery and payment timeline'],
  '/invoices': ['Invoices', 'Billing documents and lookup'],
  '/reports': ['Reports', 'Interactive revenue and customer analytics'],
}

const COLLAPSE_KEY = 'buildmate_sidebar_collapsed'

export default function MainLayout() {
  const [mobileOpen, setMobileOpen] = useState(false)
  const [collapsed, setCollapsed] = useState(() => localStorage.getItem(COLLAPSE_KEY) === '1')
  const { pathname } = useLocation()
  const [title, subtitle] = titles[pathname] || ['BuildHub', 'Construction Management Platform']

  useEffect(() => {
    localStorage.setItem(COLLAPSE_KEY, collapsed ? '1' : '0')
  }, [collapsed])

  useEffect(() => {
    setMobileOpen(false)
  }, [pathname])

  return (
    <div className={`app-shell ${collapsed ? 'collapsed' : ''}`}>
      {mobileOpen ? <div className="sidebar-backdrop" onClick={() => setMobileOpen(false)} /> : null}
      <Sidebar
        mobileOpen={mobileOpen}
        collapsed={collapsed}
        onNavigate={() => setMobileOpen(false)}
        onToggleCollapse={() => setCollapsed((v) => !v)}
      />
      <div className="main-area">
        <Header title={title} subtitle={subtitle} onMenu={() => setMobileOpen(true)} />
        <main className="page-content page-enter" key={pathname}>
          <Outlet />
        </main>
      </div>
    </div>
  )
}
