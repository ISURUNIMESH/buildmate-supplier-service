import { useEffect, useMemo, useState } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import {
  Boxes,
  CalendarRange,
  ClipboardList,
  Crown,
  Download,
  RefreshCw,
  TrendingUp,
  Users,
  Wallet,
} from 'lucide-react'
import EmptyState from '../components/common/EmptyState'
import { SkeletonCard } from '../components/common/Skeleton'
import { paymentApi } from '../services/paymentApi'
import { supplierApi } from '../services/supplierApi'
import { materialApi } from '../services/materialApi'
import { orderApi } from '../services/orderApi'
import { getErrorMessage } from '../services/api'
import { useToast } from '../components/common/Toast'
import { formatMoney, ORDER_STATUSES, SUPPLIER_STATUSES } from '../utils/format'
import { DISPLAY_ENTITY, ensureDisplayId } from '../utils/displayId'
import DisplayId from '../components/common/DisplayId'
import '../styles/reports.css'

const CHART_COLORS = [
  'var(--accent)',
  'var(--info)',
  'var(--success)',
  'var(--warning)',
  'var(--danger)',
  '#6b8cae',
  '#c99434',
]

function settle(promise) {
  return promise
    .then((data) => ({ data, error: null }))
    .catch((error) => ({ data: null, error: getErrorMessage(error) }))
}

function itemDate(item) {
  return item?.createdAt || item?.createdDate || null
}

function inDateRange(item, from, to) {
  const raw = itemDate(item)
  if (!raw) return true
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return true
  if (from) {
    const start = new Date(from)
    start.setHours(0, 0, 0, 0)
    if (d < start) return false
  }
  if (to) {
    const end = new Date(to)
    end.setHours(23, 59, 59, 999)
    if (d > end) return false
  }
  return true
}

function countByStatus(items, statuses, statusKey = 'status') {
  const buckets = Object.fromEntries(statuses.map((s) => [s, 0]))
  items.forEach((item) => {
    const key = String(item[statusKey] || '').toUpperCase()
    if (buckets[key] !== undefined) buckets[key] += 1
  })
  return statuses.map((status) => ({ status, count: buckets[status] }))
}

export default function Reports() {
  const toast = useToast()
  const [loading, setLoading] = useState(true)
  const [revenue, setRevenue] = useState(null)
  const [monthly, setMonthly] = useState(null)
  const [topCustomer, setTopCustomer] = useState(null)
  const [suppliers, setSuppliers] = useState([])
  const [materials, setMaterials] = useState([])
  const [lowStock, setLowStock] = useState([])
  const [inventory, setInventory] = useState([])
  const [orders, setOrders] = useState([])
  const [errors, setErrors] = useState([])
  const [highlight, setHighlight] = useState('revenue')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')

  const load = async () => {
    setLoading(true)
    const results = await Promise.all([
      settle(paymentApi.revenue()),
      settle(paymentApi.monthly()),
      settle(paymentApi.topCustomers()),
      settle(supplierApi.list()),
      settle(materialApi.list()),
      settle(materialApi.lowStock()),
      settle(orderApi.listInventory()),
      settle(orderApi.list()),
    ])

    setRevenue(results[0].data)
    setMonthly(results[1].data)
    setTopCustomer(results[2].data)
    setSuppliers(results[3].data || [])
    setMaterials(results[4].data || [])
    setLowStock(results[5].data || [])
    setInventory(results[6].data || [])
    setOrders(results[7].data || [])

    const errs = results.map((r) => r.error).filter(Boolean)
    setErrors([...new Set(errs)].slice(0, 4))
    if (errs.length) toast.error('Unable to load some report data.')
    setLoading(false)
  }

  useEffect(() => { load() }, [])

  const filteredOrders = useMemo(
    () => orders.filter((o) => inDateRange(o, dateFrom, dateTo)),
    [orders, dateFrom, dateTo],
  )

  const filteredMaterials = useMemo(
    () => materials.filter((m) => inDateRange(m, dateFrom, dateTo)),
    [materials, dateFrom, dateTo],
  )

  const filteredInventory = useMemo(
    () => inventory.filter((i) => inDateRange(i, dateFrom, dateTo)),
    [inventory, dateFrom, dateTo],
  )

  const filteredSuppliers = useMemo(
    () => suppliers.filter((s) => inDateRange(s, dateFrom, dateTo)),
    [suppliers, dateFrom, dateTo],
  )

  const revenueChartData = useMemo(() => [
    {
      label: monthly?.month || 'Current month',
      revenue: Number(monthly?.revenue ?? 0),
      payments: Number(monthly?.totalPayments ?? 0),
    },
  ], [monthly])

  const supplierChartData = useMemo(
    () => countByStatus(filteredSuppliers, SUPPLIER_STATUSES),
    [filteredSuppliers],
  )

  const orderChartData = useMemo(
    () => countByStatus(filteredOrders, ORDER_STATUSES),
    [filteredOrders],
  )

  const inventoryChartData = useMemo(() => {
    const top = [...filteredInventory]
      .sort((a, b) => Number(b.availableQuantity ?? 0) - Number(a.availableQuantity ?? 0))
      .slice(0, 6)
      .map((item) => ({
        name: ensureDisplayId(DISPLAY_ENTITY.MATERIAL, item.materialId || item.id) || String(item.materialId || item.id || 'Item').slice(0, 10),
        available: Number(item.availableQuantity ?? 0),
        reserved: Number(item.reservedQuantity ?? 0),
      }))
    return top
  }, [filteredInventory])

  const exportPayload = useMemo(() => ({
    exportedAt: new Date().toISOString(),
    dateFilter: { from: dateFrom || null, to: dateTo || null },
    revenue: { revenue, monthly, topCustomer },
    suppliers: { total: filteredSuppliers.length, breakdown: supplierChartData, items: filteredSuppliers },
    materials: { total: filteredMaterials.length, lowStockCount: lowStock.length, items: filteredMaterials },
    inventory: { total: filteredInventory.length, items: filteredInventory },
    orders: { total: filteredOrders.length, breakdown: orderChartData, items: filteredOrders },
  }), [
    dateFrom, dateTo, revenue, monthly, topCustomer,
    filteredSuppliers, supplierChartData, filteredMaterials, lowStock,
    filteredInventory, filteredOrders, orderChartData,
  ])

  const exportCsv = () => {
    const rows = [
      ['domain', 'metric', 'value'],
      ['revenue', 'totalRevenue', revenue?.totalRevenue ?? 0],
      ['revenue', 'totalPayments', revenue?.totalPayments ?? 0],
      ['revenue', 'monthlyRevenue', monthly?.revenue ?? 0],
      ['revenue', 'monthlyPayments', monthly?.totalPayments ?? 0],
      ['revenue', 'month', monthly?.month ?? ''],
      ['revenue', 'topCustomerUserId', topCustomer?.userId ?? ''],
      ['revenue', 'topCustomerSpent', topCustomer?.totalSpent ?? 0],
      ['suppliers', 'total', filteredSuppliers.length],
      ...supplierChartData.map((r) => ['suppliers', `status_${r.status}`, r.count]),
      ['materials', 'total', filteredMaterials.length],
      ['materials', 'lowStockAlerts', lowStock.length],
      ['inventory', 'total', filteredInventory.length],
      ['orders', 'total', filteredOrders.length],
      ...orderChartData.map((r) => ['orders', `status_${r.status}`, r.count]),
    ]
    const blob = new Blob([rows.map((r) => r.map((c) => JSON.stringify(c)).join(',')).join('\n')], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `buildmate-reports-${Date.now()}.csv`
    a.click()
    URL.revokeObjectURL(url)
    toast.success('Report exported.')
  }

  const exportJson = () => {
    const blob = new Blob([JSON.stringify(exportPayload, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `buildmate-reports-${Date.now()}.json`
    a.click()
    URL.revokeObjectURL(url)
    toast.success('Report exported.')
  }

  const clearDates = () => {
    setDateFrom('')
    setDateTo('')
  }

  if (loading && !revenue && !suppliers.length && !orders.length) {
    return (
      <div className="rpt-loading">
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
      </div>
    )
  }

  return (
    <div className="rpt">
      <section className="rpt-hero">
        <div>
          <p className="rpt-kicker">Executive analytics</p>
          <h2>Operations reports</h2>
          <p>Revenue, suppliers, inventory, and order insights across all services.</p>
        </div>
        <div className="rpt-hero-actions">
          <button type="button" className="btn btn-secondary" onClick={exportCsv}>
            <Download size={16} /> Export CSV
          </button>
          <button type="button" className="btn btn-secondary" onClick={exportJson}>
            <Download size={16} /> Export JSON
          </button>
          <button type="button" className="btn btn-primary" onClick={load} disabled={loading}>
            <RefreshCw size={16} className={loading ? 'rpt-spin' : ''} /> Refresh
          </button>
        </div>
      </section>

      <section className="rpt-filters">
        <div className="rpt-filter-label">
          <CalendarRange size={16} />
          <span>Date range</span>
          <small>(filters orders, materials, inventory & suppliers)</small>
        </div>
        <div className="rpt-filter-inputs">
          <input className="input" type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} aria-label="From date" />
          <span className="rpt-filter-sep">to</span>
          <input className="input" type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} aria-label="To date" />
          {(dateFrom || dateTo) && (
            <button type="button" className="btn btn-ghost btn-sm" onClick={clearDates}>Clear</button>
          )}
        </div>
      </section>

      {errors.length > 0 && (
        <div className="rpt-banner">{errors.join(' · ')}</div>
      )}

      <section className="rpt-section">
        <header className="rpt-section-head">
          <Wallet size={18} />
          <h3>Revenue analytics</h3>
          <span className="rpt-section-tag">Unfiltered KPIs</span>
        </header>
        <div className="rpt-kpi">
          <div
            className={`rpt-kpi-card ${highlight === 'revenue' ? 'highlight' : ''}`}
            role="button"
            tabIndex={0}
            onClick={() => setHighlight('revenue')}
            onKeyDown={(e) => e.key === 'Enter' && setHighlight('revenue')}
          >
            <div className="rpt-kpi-icon"><Wallet size={20} /></div>
            <div>
              <span>Total Revenue</span>
              <strong>{formatMoney(revenue?.totalRevenue ?? 0)}</strong>
            </div>
          </div>
          <div
            className={`rpt-kpi-card ${highlight === 'transactions' ? 'highlight' : ''}`}
            role="button"
            tabIndex={0}
            onClick={() => setHighlight('transactions')}
            onKeyDown={(e) => e.key === 'Enter' && setHighlight('transactions')}
          >
            <div className="rpt-kpi-icon"><TrendingUp size={20} /></div>
            <div>
              <span>Total Transactions</span>
              <strong>{revenue?.totalPayments ?? 0}</strong>
            </div>
          </div>
          <div
            className={`rpt-kpi-card ${highlight === 'monthly' ? 'highlight' : ''}`}
            role="button"
            tabIndex={0}
            onClick={() => setHighlight('monthly')}
            onKeyDown={(e) => e.key === 'Enter' && setHighlight('monthly')}
          >
            <div className="rpt-kpi-icon"><BarChartIcon /></div>
            <div>
              <span>Monthly Revenue</span>
              <strong>{formatMoney(monthly?.revenue ?? 0)}</strong>
              <small>{monthly?.month || '—'}</small>
            </div>
          </div>
          <div
            className={`rpt-kpi-card ${highlight === 'customer' ? 'highlight' : ''}`}
            role="button"
            tabIndex={0}
            onClick={() => setHighlight('customer')}
            onKeyDown={(e) => e.key === 'Enter' && setHighlight('customer')}
          >
            <div className="rpt-kpi-icon"><Crown size={20} /></div>
            <div>
              <span>Top Customer</span>
              <strong className="rpt-kpi-compact"><DisplayId type={DISPLAY_ENTITY.USER} id={topCustomer?.userId} /></strong>
              <small>{formatMoney(topCustomer?.totalSpent ?? 0)} · {topCustomer?.totalPayments ?? 0} payments</small>
            </div>
          </div>
        </div>
      </section>

      <section className="rpt-grid rpt-grid-2">
        <article className="rpt-chart-card">
          <header className="rpt-chart-head">
            <h3>{highlight === 'transactions' ? 'Monthly Payments' : 'Monthly Revenue'}</h3>
            <span className="muted rpt-chart-sub">{monthly?.month || 'Current period'}</span>
          </header>
          <div className="rpt-chart-body">
            {!monthly || (Number(monthly.revenue || 0) === 0 && Number(monthly.totalPayments || 0) === 0) ? (
              <EmptyState title="No monthly revenue data." />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={revenueChartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                  <XAxis dataKey="label" tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                  <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                  <Tooltip contentStyle={{ background: 'var(--surface-solid)', border: '1px solid var(--border)', borderRadius: 10 }} />
                  <Bar
                    dataKey={highlight === 'transactions' ? 'payments' : 'revenue'}
                    fill="var(--accent)"
                    radius={[6, 6, 0, 0]}
                    name={highlight === 'transactions' ? 'Payments' : 'Revenue'}
                  />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </article>

        <article className="rpt-chart-card">
          <header className="rpt-chart-head">
            <h3>Payment statistics</h3>
            <Users size={16} style={{ color: 'var(--text-muted)' }} />
          </header>
          <div className="rpt-stats-list">
            <div className="rpt-stat-row"><span>Total revenue</span><strong className="money">{formatMoney(revenue?.totalRevenue ?? 0)}</strong></div>
            <div className="rpt-stat-row"><span>Successful payments</span><strong>{revenue?.totalPayments ?? 0}</strong></div>
            <div className="rpt-stat-row"><span>Month</span><strong>{monthly?.month || '—'}</strong></div>
            <div className="rpt-stat-row"><span>Monthly payments</span><strong>{monthly?.totalPayments ?? 0}</strong></div>
            <div className="rpt-stat-row"><span>Top customer</span><strong><DisplayId type={DISPLAY_ENTITY.USER} id={topCustomer?.userId} /></strong></div>
            <div className="rpt-stat-row"><span>Top customer spent</span><strong className="money">{formatMoney(topCustomer?.totalSpent ?? 0)}</strong></div>
          </div>
        </article>
      </section>

      <section className="rpt-section">
        <header className="rpt-section-head">
          <Users size={18} />
          <h3>Supplier analytics</h3>
          <span className="rpt-section-tag">{filteredSuppliers.length} suppliers</span>
        </header>
        <div className="rpt-mini-kpi">
          <div className="rpt-mini-card">
            <span>Approved</span>
            <strong>{filteredSuppliers.filter((s) => s.status === 'APPROVED').length}</strong>
          </div>
          <div className="rpt-mini-card">
            <span>Pending</span>
            <strong>{filteredSuppliers.filter((s) => s.status === 'PENDING').length}</strong>
          </div>
          <div className="rpt-mini-card">
            <span>Rejected</span>
            <strong>{filteredSuppliers.filter((s) => s.status === 'REJECTED').length}</strong>
          </div>
        </div>
        <article className="rpt-chart-card">
          <header className="rpt-chart-head"><h3>Status breakdown</h3></header>
          <div className="rpt-chart-body rpt-chart-body-sm">
            {filteredSuppliers.length === 0 ? (
              <EmptyState title="No supplier data." description="Adjust date filters or refresh." />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={supplierChartData.filter((d) => d.count > 0)}
                    dataKey="count"
                    nameKey="status"
                    cx="50%"
                    cy="50%"
                    innerRadius={52}
                    outerRadius={88}
                    paddingAngle={3}
                  >
                    {supplierChartData.map((entry, idx) => (
                      <Cell key={entry.status} fill={CHART_COLORS[idx % CHART_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ background: 'var(--surface-solid)', border: '1px solid var(--border)', borderRadius: 10 }} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </article>
      </section>

      <section className="rpt-section">
        <header className="rpt-section-head">
          <Boxes size={18} />
          <h3>Inventory analytics</h3>
          <span className="rpt-section-tag">{filteredInventory.length} records</span>
        </header>
        <div className="rpt-mini-kpi">
          <div className="rpt-mini-card">
            <span>Materials</span>
            <strong>{filteredMaterials.length}</strong>
          </div>
          <div className="rpt-mini-card">
            <span>Low stock alerts</span>
            <strong>{lowStock.length}</strong>
          </div>
          <div className="rpt-mini-card">
            <span>Inventory items</span>
            <strong>{filteredInventory.length}</strong>
          </div>
        </div>
        <article className="rpt-chart-card">
          <header className="rpt-chart-head"><h3>Top available stock</h3></header>
          <div className="rpt-chart-body">
            {inventoryChartData.length === 0 ? (
              <EmptyState title="No inventory data." description="Adjust date filters or refresh." />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={inventoryChartData} layout="vertical" margin={{ left: 8, right: 16 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                  <XAxis type="number" tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                  <YAxis type="category" dataKey="name" width={72} tick={{ fill: 'var(--text-muted)', fontSize: 10 }} />
                  <Tooltip contentStyle={{ background: 'var(--surface-solid)', border: '1px solid var(--border)', borderRadius: 10 }} />
                  <Legend />
                  <Bar dataKey="available" fill="var(--success)" radius={[0, 4, 4, 0]} name="Available" stackId="a" />
                  <Bar dataKey="reserved" fill="var(--warning)" radius={[0, 4, 4, 0]} name="Reserved" stackId="a" />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </article>
      </section>

      <section className="rpt-section">
        <header className="rpt-section-head">
          <ClipboardList size={18} />
          <h3>Order analytics</h3>
          <span className="rpt-section-tag">{filteredOrders.length} orders</span>
        </header>
        <div className="rpt-mini-kpi">
          <div className="rpt-mini-card">
            <span>Pending</span>
            <strong>{filteredOrders.filter((o) => o.status === 'PENDING').length}</strong>
          </div>
          <div className="rpt-mini-card">
            <span>Confirmed</span>
            <strong>{filteredOrders.filter((o) => o.status === 'CONFIRMED').length}</strong>
          </div>
          <div className="rpt-mini-card">
            <span>Delivered</span>
            <strong>{filteredOrders.filter((o) => o.status === 'DELIVERED').length}</strong>
          </div>
        </div>
        <article className="rpt-chart-card">
          <header className="rpt-chart-head"><h3>Order status breakdown</h3></header>
          <div className="rpt-chart-body">
            {filteredOrders.length === 0 ? (
              <EmptyState title="No order data." description="Adjust date filters or refresh." />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={orderChartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                  <XAxis dataKey="status" tick={{ fill: 'var(--text-muted)', fontSize: 10 }} />
                  <YAxis allowDecimals={false} tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                  <Tooltip contentStyle={{ background: 'var(--surface-solid)', border: '1px solid var(--border)', borderRadius: 10 }} />
                  <Bar dataKey="count" name="Orders" radius={[6, 6, 0, 0]}>
                    {orderChartData.map((entry, idx) => (
                      <Cell key={entry.status} fill={CHART_COLORS[idx % CHART_COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </article>
      </section>
    </div>
  )
}

function BarChartIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <line x1="12" y1="20" x2="12" y2="10" />
      <line x1="18" y1="20" x2="18" y2="4" />
      <line x1="6" y1="20" x2="6" y2="16" />
    </svg>
  )
}
