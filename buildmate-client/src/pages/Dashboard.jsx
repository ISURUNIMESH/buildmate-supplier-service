import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  ArrowUpRight,
  Boxes,
  Building2,
  ClipboardList,
  CreditCard,
  PackagePlus,
  TriangleAlert,
  Wallet,
  Warehouse,
  Sparkles,
} from 'lucide-react'
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import EmptyState from '../components/common/EmptyState'
import StatusBadge from '../components/common/StatusBadge'
import { SkeletonCard } from '../components/common/Skeleton'
import { materialApi } from '../services/materialApi'
import { supplierApi } from '../services/supplierApi'
import { orderApi } from '../services/orderApi'
import { paymentApi } from '../services/paymentApi'
import { formatDate, formatMoney, inventoryStatus } from '../utils/format'
import { getErrorMessage } from '../services/api'
import { DISPLAY_ENTITY, ensureDisplayId } from '../utils/displayId'
import DisplayId from '../components/common/DisplayId'
import '../styles/dashboard.css'

async function settle(promise) {
  try {
    const data = await promise
    return { data, error: null }
  } catch (error) {
    return { data: null, error: getErrorMessage(error) }
  }
}

export default function Dashboard() {
  const [loading, setLoading] = useState(true)
  const [errors, setErrors] = useState([])
  const [materials, setMaterials] = useState([])
  const [lowStock, setLowStock] = useState([])
  const [suppliers, setSuppliers] = useState([])
  const [orders, setOrders] = useState([])
  const [payments, setPayments] = useState([])
  const [pendingPayments, setPendingPayments] = useState([])
  const [inventory, setInventory] = useState([])
  const [revenue, setRevenue] = useState(null)

  useEffect(() => {
    let alive = true
    ;(async () => {
      setLoading(true)
      const results = await Promise.all([
        settle(materialApi.list()),
        settle(materialApi.lowStock()),
        settle(supplierApi.list()),
        settle(orderApi.list()),
        settle(paymentApi.list()),
        settle(paymentApi.pending()),
        settle(orderApi.listInventory()),
        settle(paymentApi.revenue()),
      ])
      if (!alive) return

      const errs = results.map((r) => r.error).filter(Boolean)
      setErrors([...new Set(errs)].slice(0, 3))
      setMaterials(results[0].data || [])
      setLowStock(results[1].data || [])
      setSuppliers(results[2].data || [])
      setOrders(results[3].data || [])
      setPayments(results[4].data || [])
      setPendingPayments(results[5].data || [])
      setInventory(results[6].data || [])
      setRevenue(results[7].data)
      setLoading(false)
    })()
    return () => { alive = false }
  }, [])

  const chartData = useMemo(() => {
    const buckets = { PENDING: 0, CONFIRMED: 0, PAID: 0, CANCELLED: 0, DELIVERED: 0 }
    orders.forEach((o) => {
      const key = String(o.status || '').toUpperCase()
      if (buckets[key] !== undefined) buckets[key] += 1
      else buckets.PENDING += 0
    })
    return Object.entries(buckets).map(([status, count]) => ({ status, count }))
  }, [orders])

  const paymentTrend = useMemo(() => {
    const map = new Map()
    payments.forEach((p) => {
      const d = new Date(p.createdAt || 0)
      if (Number.isNaN(d.getTime())) return
      const key = `${d.getMonth() + 1}/${d.getDate()}`
      const prev = map.get(key) || 0
      map.set(key, prev + Number(p.amount || 0))
    })
    return [...map.entries()].slice(-8).map(([day, amount]) => ({ day, amount }))
  }, [payments])

  const activity = useMemo(() => {
    const orderEvents = orders.map((o) => ({
      id: `o-${o.id}`,
      kind: 'Order',
      title: `Order ${ensureDisplayId(DISPLAY_ENTITY.ORDER, o.id) || '—'}`,
      meta: `${o.status} · ${formatMoney(o.totalPrice)}`,
      at: o.createdDate,
      tone: orderTone(o.status),
    }))
    const payEvents = payments.map((p) => ({
      id: `p-${p.id}`,
      kind: 'Payment',
      title: `Payment ${ensureDisplayId(DISPLAY_ENTITY.PAYMENT, p.id) || '—'}`,
      meta: `${p.status} · ${formatMoney(p.amount, p.currency || 'LKR')}`,
      at: p.createdAt,
      tone: paymentTone(p.status),
    }))
    return [...orderEvents, ...payEvents]
      .sort((a, b) => new Date(b.at || 0) - new Date(a.at || 0))
      .slice(0, 8)
  }, [orders, payments])

  if (loading) {
    return (
      <div className="dash-loading">
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
      </div>
    )
  }

  return (
    <div className="dash">
      {errors.length > 0 && (
        <div className="dash-banner warn">
          Some services did not respond: {errors.join(' · ')}
        </div>
      )}

      <section className="dash-hero">
        <div>
          <p className="dash-kicker"><Sparkles size={14} /> Command center</p>
          <h2>Operations overview</h2>
          <p className="dash-hero-copy">
            Live snapshot of materials, fulfilment, warehouse stock, and finance across BuildHub.
          </p>
        </div>
        <div className="dash-hero-revenue">
          <span>Total revenue</span>
          <strong>{formatMoney(revenue?.totalRevenue ?? 0)}</strong>
          <small>{revenue?.totalPayments ?? 0} successful payments</small>
        </div>
      </section>

      <section className="dash-bento">
        <Metric tone="gold" icon={<Wallet size={18} />} label="Revenue" value={formatMoney(revenue?.totalRevenue ?? 0)} hint="From payment reports" />
        <Metric icon={<ClipboardList size={18} />} label="Orders" value={orders.length} hint="All statuses" to="/orders" />
        <Metric icon={<Building2 size={18} />} label="Suppliers" value={suppliers.length} hint="Registered partners" to="/suppliers" />
        <Metric icon={<Boxes size={18} />} label="Materials" value={materials.length} hint="Catalog SKUs" to="/materials" />
        <Metric icon={<Warehouse size={18} />} label="Inventory rows" value={inventory.length} hint="Warehouse ledger" to="/inventory" />
        <Metric
          tone="warn"
          icon={<CreditCard size={18} />}
          label="Pending payments"
          value={pendingPayments.length}
          hint="Needs attention"
          to="/payments"
        />
      </section>

      <section className="dash-actions">
        <h3>Quick actions</h3>
        <div className="dash-action-row">
          <Link className="dash-action" to="/orders"><ClipboardList size={16} /> View orders <ArrowUpRight size={14} /></Link>
          <Link className="dash-action" to="/payments"><CreditCard size={16} /> Open payments <ArrowUpRight size={14} /></Link>
          <Link className="dash-action" to="/inventory"><Warehouse size={16} /> Check inventory <ArrowUpRight size={14} /></Link>
          <Link className="dash-action" to="/materials"><PackagePlus size={16} /> Browse materials <ArrowUpRight size={14} /></Link>
          <Link className="dash-action" to="/reports"><Wallet size={16} /> Analytics <ArrowUpRight size={14} /></Link>
        </div>
      </section>

      {lowStock.length > 0 ? (
        <section className="dash-alert">
          <TriangleAlert size={18} />
          <div>
            <strong>{lowStock.length} materials are low on stock</strong>
            <p>{lowStock.slice(0, 3).map((m) => m.name).join(', ')}{lowStock.length > 3 ? '…' : ''}</p>
          </div>
          <Link to="/materials" className="btn btn-secondary btn-sm">Review</Link>
        </section>
      ) : null}

      <section className="dash-main">
        <div className="card dash-panel">
          <div className="card-header">
            <h3>Analytics</h3>
            <span className="muted">Order mix · payment momentum</span>
          </div>
          <div className="card-body dash-charts">
            <div className="dash-chart">
              <h4>Order status</h4>
              {orders.length === 0 ? (
                <EmptyState title="No orders yet" description="Orders will appear as they are created." />
              ) : (
                <ResponsiveContainer width="100%" height={220}>
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                    <XAxis dataKey="status" tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                    <YAxis allowDecimals={false} tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                    <Tooltip />
                    <Bar dataKey="count" fill="var(--accent)" radius={[8, 8, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
            <div className="dash-chart">
              <h4>Payment volume</h4>
              {paymentTrend.length === 0 ? (
                <EmptyState title="No payment trend yet" />
              ) : (
                <ResponsiveContainer width="100%" height={220}>
                  <AreaChart data={paymentTrend}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                    <XAxis dataKey="day" tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                    <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                    <Tooltip />
                    <Area type="monotone" dataKey="amount" stroke="var(--accent-strong)" fill="var(--accent-soft)" />
                  </AreaChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>
        </div>

        <div className="card dash-panel">
          <div className="card-header"><h3>Recent activity</h3></div>
          <div className="card-body">
            {activity.length === 0 ? (
              <EmptyState title="No recent activity" />
            ) : (
              <ul className="dash-activity">
                {activity.map((item) => (
                  <li key={item.id}>
                    <StatusBadge label={item.kind} tone={item.tone} />
                    <div>
                      <strong>{item.title}</strong>
                      <p>{item.meta}</p>
                    </div>
                    <time>{formatDate(item.at)}</time>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </section>

      <section className="dash-bottom">
        <div className="card">
          <div className="card-header"><h3>Inventory pulse</h3></div>
          <div className="card-body">
            {inventory.length === 0 ? (
              <EmptyState title="No inventory records" />
            ) : (
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Material</th>
                      <th>Available</th>
                      <th>Reserved</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {inventory.slice(0, 6).map((item) => {
                      const st = inventoryStatus(item)
                      return (
                        <tr key={item.id || item.materialId}>
                          <td><DisplayId type={DISPLAY_ENTITY.MATERIAL} id={item.materialId} /></td>
                          <td>{item.availableQuantity}</td>
                          <td>{item.reservedQuantity}</td>
                          <td><StatusBadge label={st.label} tone={st.tone} /></td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        <div className="card">
          <div className="card-header"><h3>Payments queue</h3></div>
          <div className="card-body">
            {(pendingPayments.length ? pendingPayments : payments).length === 0 ? (
              <EmptyState title="No payments found" />
            ) : (
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Payment</th>
                      <th>Amount</th>
                      <th>Status</th>
                      <th>When</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(pendingPayments.length ? pendingPayments : payments).slice(0, 6).map((p) => (
                      <tr key={p.id}>
                        <td><DisplayId type={DISPLAY_ENTITY.PAYMENT} id={p.id} /></td>
                        <td className="money">{formatMoney(p.amount, p.currency || 'LKR')}</td>
                        <td><StatusBadge label={p.status} tone={paymentTone(p.status)} /></td>
                        <td>{formatDate(p.createdAt)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </section>
    </div>
  )
}

function Metric({ icon, label, value, hint, to, tone }) {
  const body = (
    <>
      <div className="dash-metric-icon">{icon}</div>
      <div className="dash-metric-label">{label}</div>
      <div className="dash-metric-value">{value}</div>
      {hint ? <div className="dash-metric-hint">{hint}</div> : null}
    </>
  )
  if (to) {
    return <Link to={to} className={`dash-metric ${tone || ''}`}>{body}</Link>
  }
  return <div className={`dash-metric ${tone || ''}`}>{body}</div>
}

function orderTone(status) {
  const s = String(status || '').toUpperCase()
  if (s === 'DELIVERED' || s === 'CONFIRMED' || s === 'PAID') return 'success'
  if (s === 'PENDING') return 'warning'
  if (s === 'CANCELLED') return 'danger'
  return 'neutral'
}

function paymentTone(status) {
  const s = String(status || '').toUpperCase()
  if (s === 'SUCCESS' || s === 'COMPLETED' || s === 'PAID') return 'success'
  if (s === 'PENDING') return 'warning'
  if (s === 'REFUNDED' || s === 'FAILED') return 'danger'
  return 'neutral'
}
