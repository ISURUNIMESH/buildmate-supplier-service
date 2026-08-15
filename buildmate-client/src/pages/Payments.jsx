import { useEffect, useMemo, useState } from 'react'
import {
  Download,
  Eye,
  Filter,
  Plus,
  RefreshCw,
  RotateCcw,
  Wallet,
  Clock3,
  CircleCheck,
  Ban,
} from 'lucide-react'
import {
  Area,
  AreaChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import EmptyState from '../components/common/EmptyState'
import Modal from '../components/common/Modal'
import ConfirmDialog from '../components/common/ConfirmDialog'
import StatusBadge from '../components/common/StatusBadge'
import { SkeletonCard } from '../components/common/Skeleton'
import { paymentApi } from '../services/paymentApi'
import { getErrorMessage } from '../services/api'
import { useToast } from '../components/common/Toast'
import EntitySelect from '../components/common/EntitySelect'
import UserIdField from '../components/common/UserIdField'
import DisplayId from '../components/common/DisplayId'
import { useOrderOptions } from '../hooks/useEntityOptions'
import { formatDate, formatMoney, PAYMENT_STATUS_OPTIONS } from '../utils/format'
import { DISPLAY_ENTITY, ensureDisplayId, ensureDisplayIds } from '../utils/displayId'
import '../styles/payments.css'

const emptyForm = {
  orderId: '',
  userId: '',
  amount: '',
  currency: 'LKR',
  paymentMethod: 'CARD',
  status: 'SUCCESS',
}

const PIE_COLORS = ['#d4a017', '#1f7a4d', '#b45309', '#b42318', '#1d4f91']

export default function Payments() {
  const toast = useToast()
  const [loading, setLoading] = useState(true)
  const [rows, setRows] = useState([])
  const [revenue, setRevenue] = useState(null)
  const [userFilter, setUserFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [pendingOnly, setPendingOnly] = useState(false)
  const [modal, setModal] = useState(null)
  const [detail, setDetail] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [errors, setErrors] = useState({})
  const [saving, setSaving] = useState(false)
  const [confirm, setConfirm] = useState(null)
  const [statusValue, setStatusValue] = useState('SUCCESS')
  const { options: orderOptions, loading: ordersLoading, error: ordersError } = useOrderOptions()

  const filteredOrderOptions = useMemo(() => {
    if (!form.userId.trim()) return orderOptions
    return orderOptions.filter((o) => String(o.raw?.userId || '') === String(form.userId))
  }, [orderOptions, form.userId])

  const load = async () => {
    setLoading(true)
    try {
      let data
      if (pendingOnly) data = await paymentApi.pending()
      else if (statusFilter) data = await paymentApi.byStatus(statusFilter)
      else if (userFilter.trim()) data = await paymentApi.history(userFilter.trim())
      else data = await paymentApi.list()
      setRows(ensureDisplayIds(DISPLAY_ENTITY.PAYMENT, data || [], { sortField: 'createdAt' }))
      try {
        setRevenue(await paymentApi.revenue())
      } catch {
        setRevenue(null)
      }
    } catch (error) {
      toast.error(getErrorMessage(error))
      setRows([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [pendingOnly, statusFilter])

  const displayed = useMemo(() => {
    if (!userFilter.trim() || pendingOnly || statusFilter) return rows
    const q = userFilter.trim().toLowerCase()
    return rows.filter((p) => String(p.userId || '').toLowerCase().includes(q))
  }, [rows, userFilter, pendingOnly, statusFilter])

  const statusCounts = useMemo(() => {
    const map = { SUCCESS: 0, PENDING: 0, REFUNDED: 0, FAILED: 0, OTHER: 0 }
    rows.forEach((p) => {
      const s = String(p.status || '').toUpperCase()
      if (map[s] !== undefined) map[s] += 1
      else map.OTHER += 1
    })
    return map
  }, [rows])

  const pieData = useMemo(
    () => Object.entries(statusCounts)
      .filter(([, v]) => v > 0)
      .map(([name, value]) => ({ name, value })),
    [statusCounts],
  )

  const trend = useMemo(() => {
    const map = new Map()
    rows.forEach((p) => {
      const d = new Date(p.createdAt || 0)
      if (Number.isNaN(d.getTime())) return
      const key = `${d.getMonth() + 1}/${d.getDate()}`
      map.set(key, (map.get(key) || 0) + Number(p.amount || 0))
    })
    return [...map.entries()].slice(-10).map(([day, amount]) => ({ day, amount }))
  }, [rows])

  const timeline = useMemo(
    () => [...displayed]
      .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
      .slice(0, 8),
    [displayed],
  )

  const validate = () => {
    const e = {}
    if (!form.orderId.trim()) e.orderId = 'Required'
    if (!form.userId.trim()) e.userId = 'Required'
    if (!form.amount || Number(form.amount) <= 0) e.amount = 'Must be positive'
    if (!form.currency.trim()) e.currency = 'Required'
    if (!form.paymentMethod.trim()) e.paymentMethod = 'Required'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const createPayment = async () => {
    if (!validate()) return
    setSaving(true)
    try {
      await paymentApi.create({
        orderId: form.orderId.trim(),
        userId: form.userId.trim(),
        amount: Number(form.amount),
        currency: form.currency.trim(),
        paymentMethod: form.paymentMethod.trim(),
        status: form.status || 'PENDING',
      })
      toast.success('Payment created successfully.')
      setModal(null)
      await load()
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setSaving(false)
    }
  }

  const exportCsv = () => {
    const header = ['id', 'orderId', 'userId', 'amount', 'currency', 'paymentMethod', 'status', 'createdAt']
    const lines = [header.join(',')]
    displayed.forEach((p) => {
      lines.push(header.map((k) => JSON.stringify(p[k] ?? '')).join(','))
    })
    const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `buildmate-payments-${Date.now()}.csv`
    a.click()
    URL.revokeObjectURL(url)
    toast.success('Export ready.')
  }

  if (loading && rows.length === 0) {
    return (
      <div className="pay-loading">
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
      </div>
    )
  }

  return (
    <div className="pay">
      <section className="pay-hero">
        <div>
          <p className="pay-kicker">Finance showcase</p>
          <h2>Payments desk</h2>
          <p>Revenue, recovery, and ledger activity across the payment microservice.</p>
        </div>
        <div className="pay-hero-actions">
          <button type="button" className="btn btn-secondary" onClick={exportCsv}>
            <Download size={16} /> Export
          </button>
          <button type="button" className="btn btn-primary" onClick={() => { setForm(emptyForm); setErrors({}); setModal('create') }}>
            <Plus size={16} /> Create Payment
          </button>
        </div>
      </section>

      <section className="pay-kpi">
        <div className="pay-kpi-card revenue">
          <Wallet size={18} />
          <div>
            <span>Revenue</span>
            <strong>{formatMoney(revenue?.totalRevenue ?? 0)}</strong>
            <small>{revenue?.totalPayments ?? 0} successful</small>
          </div>
        </div>
        <div className="pay-kpi-card">
          <CircleCheck size={18} />
          <div>
            <span>Success</span>
            <strong>{statusCounts.SUCCESS}</strong>
          </div>
        </div>
        <div className="pay-kpi-card warn">
          <Clock3 size={18} />
          <div>
            <span>Pending</span>
            <strong>{statusCounts.PENDING}</strong>
          </div>
        </div>
        <div className="pay-kpi-card danger">
          <Ban size={18} />
          <div>
            <span>Refunded / Failed</span>
            <strong>{statusCounts.REFUNDED + statusCounts.FAILED}</strong>
          </div>
        </div>
      </section>

      <section className="pay-filters card">
        <div className="card-body pay-filters-row">
          <Filter size={16} className="muted" />
          <div style={{ minWidth: 200, flex: 1 }}>
            <UserIdField
              value={userFilter}
              onChange={setUserFilter}
              placeholder="Filter by user"
              emptyMessage="No users available"
            />
          </div>
          <button type="button" className="btn btn-secondary" onClick={load}>Apply / Refresh</button>
          <select
            className="select"
            value={statusFilter}
            onChange={(e) => { setPendingOnly(false); setStatusFilter(e.target.value) }}
          >
            <option value="">All statuses</option>
            {PAYMENT_STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <button
            type="button"
            className={`btn ${pendingOnly ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => { setStatusFilter(''); setPendingOnly((v) => !v) }}
          >
            Pending only
          </button>
        </div>
      </section>

      <section className="pay-grid">
        <div className="card">
          <div className="card-header"><h3>Volume trend</h3></div>
          <div className="card-body" style={{ height: 240 }}>
            {trend.length === 0 ? (
              <EmptyState title="No chart data" />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={trend}>
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
        <div className="card">
          <div className="card-header"><h3>Status mix</h3></div>
          <div className="card-body" style={{ height: 240 }}>
            {pieData.length === 0 ? (
              <EmptyState title="No statuses" />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={pieData} dataKey="value" nameKey="name" innerRadius={55} outerRadius={80} paddingAngle={3}>
                    {pieData.map((entry, i) => (
                      <Cell key={entry.name} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      </section>

      <section className="pay-split">
        <div className="card">
          <div className="card-header">
            <h3>Ledger</h3>
            <span className="muted">{displayed.length} records</span>
          </div>
          <div className="card-body">
            {displayed.length === 0 ? (
              <EmptyState title="No payments found." description="Adjust filters or create a payment." />
            ) : (
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Payment</th>
                      <th>Order</th>
                      <th>User</th>
                      <th>Amount</th>
                      <th>Method</th>
                      <th>Status</th>
                      <th>Created</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {displayed.map((p) => {
                      const status = String(p.status || '').toUpperCase()
                      return (
                        <tr key={p.id}>
                          <td><DisplayId type={DISPLAY_ENTITY.PAYMENT} id={p.id} /></td>
                          <td><DisplayId type={DISPLAY_ENTITY.ORDER} id={p.orderId} /></td>
                          <td><DisplayId type={DISPLAY_ENTITY.USER} id={p.userId} /></td>
                          <td className="money">{formatMoney(p.amount, p.currency)}</td>
                          <td><span className="pay-method">{p.paymentMethod}</span></td>
                          <td><StatusBadge label={p.status} tone={tone(status)} /></td>
                          <td>{formatDate(p.createdAt)}</td>
                          <td>
                            <div className="actions-cell">
                              <button type="button" className="icon-action" title="View" onClick={async () => {
                                try {
                                  setDetail(await paymentApi.getById(p.id))
                                  setModal('view')
                                } catch (error) {
                                  toast.error(getErrorMessage(error))
                                }
                              }}><Eye size={15} /></button>
                              <button type="button" className="btn btn-sm btn-secondary" onClick={() => { setDetail(p); setStatusValue(p.status || 'PENDING'); setModal('status') }}>Status</button>
                              {status === 'SUCCESS' && (
                                <button type="button" className="btn btn-sm btn-secondary" onClick={() => setConfirm({ type: 'refund', payment: p })}>
                                  <RotateCcw size={14} /> Refund
                                </button>
                              )}
                              {status !== 'SUCCESS' && status !== 'REFUNDED' && (
                                <button type="button" className="btn btn-sm btn-secondary" onClick={() => setConfirm({ type: 'retry', payment: p })}>
                                  <RefreshCw size={14} /> Retry
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        <div className="card pay-timeline-card">
          <div className="card-header"><h3>Payment timeline</h3></div>
          <div className="card-body">
            {timeline.length === 0 ? (
              <EmptyState title="No timeline events" />
            ) : (
              <ol className="pay-timeline">
                {timeline.map((p, idx) => (
                  <li key={p.id}>
                    <span className={`pay-dot ${tone(p.status)}`} />
                    <div>
                      <strong>{formatMoney(p.amount, p.currency)}</strong>
                      <p><DisplayId type={DISPLAY_ENTITY.USER} id={p.userId} /> · {p.paymentMethod}</p>
                      <StatusBadge label={p.status} tone={tone(p.status)} />
                    </div>
                    <time>{formatDate(p.createdAt)}</time>
                    {idx < timeline.length - 1 ? <span className="pay-line" /> : null}
                  </li>
                ))}
              </ol>
            )}
          </div>
        </div>
      </section>

      <Modal
        open={modal === 'create'}
        title="Create Payment"
        onClose={() => setModal(null)}
        footer={(
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="button" className="btn btn-primary" disabled={saving} onClick={createPayment}>{saving ? 'Creating…' : 'Create'}</button>
          </>
        )}
      >
        <div className="form-grid">
          <Field label="userId" required error={errors.userId}>
            <UserIdField
              value={form.userId}
              onChange={(userId) => {
                const orderStillValid = form.orderId
                  && orderOptions.some((o) => o.value === form.orderId && String(o.raw?.userId || '') === String(userId))
                setForm({
                  ...form,
                  userId,
                  orderId: orderStillValid ? form.orderId : '',
                })
              }}
              error={errors.userId}
              placeholder="Select User ID"
              emptyMessage="No users available"
            />
          </Field>
          <Field label="orderId" required error={errors.orderId || ordersError}>
            <EntitySelect
              value={form.orderId}
              onChange={(orderId) => {
                const order = orderOptions.find((o) => o.value === orderId)?.raw
                setForm({
                  ...form,
                  orderId,
                  userId: order?.userId || form.userId,
                  amount: order?.totalPrice != null ? String(order.totalPrice) : form.amount,
                })
              }}
              options={filteredOrderOptions}
              loading={ordersLoading}
              placeholder={form.userId ? 'Select Order ID' : 'Select Order ID (or pick user first)'}
              searchPlaceholder="Search order…"
              emptyMessage={form.userId ? 'No orders for this user' : 'No orders available'}
            />
          </Field>
          <Field label="amount" required error={errors.amount}><input className="input" type="number" min="0.01" step="0.01" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} /></Field>
          <Field label="currency" required error={errors.currency}><input className="input" value={form.currency} onChange={(e) => setForm({ ...form, currency: e.target.value })} /></Field>
          <Field label="paymentMethod" required error={errors.paymentMethod}>
            <input className="input" value={form.paymentMethod} onChange={(e) => setForm({ ...form, paymentMethod: e.target.value })} placeholder="CARD / CASH / BANK" />
          </Field>
          <Field label="status">
            <select className="select" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
              {PAYMENT_STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </Field>
        </div>
      </Modal>

      <Modal open={modal === 'view'} title="Payment Details" onClose={() => setModal(null)}>
        {detail && (
          <dl className="detail-grid">
            <div><dt>ID</dt><dd><DisplayId type={DISPLAY_ENTITY.PAYMENT} id={detail.id} /></dd></div>
            <div><dt>Order</dt><dd><DisplayId type={DISPLAY_ENTITY.ORDER} id={detail.orderId} /></dd></div>
            <div><dt>User</dt><dd><DisplayId type={DISPLAY_ENTITY.USER} id={detail.userId} /></dd></div>
            <div><dt>Amount</dt><dd className="money">{formatMoney(detail.amount, detail.currency)}</dd></div>
            <div><dt>Method</dt><dd>{detail.paymentMethod}</dd></div>
            <div><dt>Status</dt><dd><StatusBadge label={detail.status} tone={tone(detail.status)} /></dd></div>
            <div><dt>Created</dt><dd>{formatDate(detail.createdAt)}</dd></div>
          </dl>
        )}
      </Modal>

      <Modal
        open={modal === 'status'}
        title="Update Payment Status"
        onClose={() => setModal(null)}
        footer={(
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button
              type="button"
              className="btn btn-primary"
              disabled={saving}
              onClick={async () => {
                setSaving(true)
                try {
                  await paymentApi.updateStatus(detail.id, statusValue)
                  toast.success('Payment status updated successfully.')
                  setModal(null)
                  await load()
                } catch (error) {
                  toast.error(getErrorMessage(error))
                } finally {
                  setSaving(false)
                }
              }}
            >
              {saving ? 'Saving…' : 'Update'}
            </button>
          </>
        )}
      >
        <div className="field">
          <label>Status</label>
          <select className="select" value={statusValue} onChange={(e) => setStatusValue(e.target.value)}>
            {PAYMENT_STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
      </Modal>

      <ConfirmDialog
        open={Boolean(confirm)}
        title={confirm?.type === 'refund' ? 'Refund payment' : 'Retry payment'}
        message={confirm?.type === 'refund'
          ? 'Refund this SUCCESS payment? Status will become REFUNDED.'
          : 'Retry this payment? Status will be set to PENDING.'}
        confirmLabel={confirm?.type === 'refund' ? 'Refund' : 'Retry'}
        danger={confirm?.type === 'refund'}
        loading={saving}
        onCancel={() => setConfirm(null)}
        onConfirm={async () => {
          setSaving(true)
          try {
            if (confirm.type === 'refund') await paymentApi.refund(confirm.payment.id)
            else await paymentApi.retry(confirm.payment.id)
            toast.success(confirm.type === 'refund' ? 'Payment refunded successfully.' : 'Payment retry accepted.')
            setConfirm(null)
            await load()
          } catch (error) {
            toast.error(getErrorMessage(error))
          } finally {
            setSaving(false)
          }
        }}
      />
    </div>
  )
}

function Field({ label, required, error, children, className = '' }) {
  return (
    <div className={`field ${className}`}>
      <label>{label}{required ? <span className="req"> *</span> : null}</label>
      {children}
      {error ? <div className="field-error">{error}</div> : null}
    </div>
  )
}

function tone(status) {
  const s = String(status || '').toUpperCase()
  if (s === 'SUCCESS' || s === 'COMPLETED' || s === 'PAID') return 'success'
  if (s === 'PENDING') return 'warning'
  if (s === 'REFUNDED' || s === 'FAILED') return 'danger'
  return 'neutral'
}
