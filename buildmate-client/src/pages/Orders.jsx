import { useEffect, useMemo, useState } from 'react'
import { CheckSquare, Eye, Plus, Square, Trash2 } from 'lucide-react'
import EmptyState from '../components/common/EmptyState'
import Modal from '../components/common/Modal'
import ConfirmDialog from '../components/common/ConfirmDialog'
import StatusBadge from '../components/common/StatusBadge'
import { SkeletonCard } from '../components/common/Skeleton'
import { orderApi } from '../services/orderApi'
import { getErrorMessage } from '../services/api'
import { useToast } from '../components/common/Toast'
import EntitySelect from '../components/common/EntitySelect'
import UserIdField from '../components/common/UserIdField'
import DisplayId from '../components/common/DisplayId'
import { useMaterialOptions, useOrderOptions, useSupplierOptions } from '../hooks/useEntityOptions'
import { formatDate, formatMoney, ORDER_STATUSES } from '../utils/format'
import { DISPLAY_ENTITY, ensureDisplayId, ensureDisplayIds } from '../utils/displayId'
import '../styles/orders.css'

const emptyItem = () => ({ materialId: '', quantity: '', price: '', supplierId: '' })
const FLOW = ['PENDING', 'CONFIRMED', 'PAID', 'DELIVERED']

export default function Orders() {
  const toast = useToast()
  const [loading, setLoading] = useState(true)
  const [rows, setRows] = useState([])
  const [userFilter, setUserFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [search, setSearch] = useState('')
  const [selected, setSelected] = useState(() => new Set())
  const [modal, setModal] = useState(null)
  const [detail, setDetail] = useState(null)
  const [confirm, setConfirm] = useState(null)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState({ userId: '', items: [emptyItem()] })
  const [errors, setErrors] = useState({})
  const [statusValue, setStatusValue] = useState('CONFIRMED')
  const { options: orderOptions, loading: ordersLoading } = useOrderOptions()
  const { options: supplierOptions, loading: suppliersLoading } = useSupplierOptions()
  const { options: allMaterialOptions, loading: materialsLoading, all: allMaterials } = useMaterialOptions()

  const loadAll = async () => {
    setLoading(true)
    try {
      const data = await orderApi.list()
      setRows(ensureDisplayIds(DISPLAY_ENTITY.ORDER, data || [], { sortField: 'createdDate' }))
      setSelected(new Set())
    } catch (error) {
      toast.error(getErrorMessage(error))
      setRows([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadAll() }, [])

  const filtered = useMemo(() => {
    let list = rows
    if (userFilter.trim()) {
      const q = userFilter.trim().toLowerCase()
      list = list.filter((o) => {
        const userDisp = ensureDisplayId(DISPLAY_ENTITY.USER, o.userId).toLowerCase()
        return String(o.userId || '').toLowerCase().includes(q) || userDisp.includes(q)
      })
    }
    if (statusFilter) list = list.filter((o) => o.status === statusFilter)
    if (search.trim()) {
      const q = search.trim().toLowerCase()
      list = list.filter((o) => {
        const orderDisp = (o.displayId || ensureDisplayId(DISPLAY_ENTITY.ORDER, o.id)).toLowerCase()
        const userDisp = ensureDisplayId(DISPLAY_ENTITY.USER, o.userId).toLowerCase()
        return orderDisp.includes(q)
          || userDisp.includes(q)
          || String(o.id || '').toLowerCase().includes(q)
          || String(o.userId || '').toLowerCase().includes(q)
      })
    }
    return list
  }, [rows, userFilter, statusFilter, search])

  const applyServerFilters = async () => {
    setLoading(true)
    try {
      let data
      if (statusFilter && !userFilter.trim()) data = await orderApi.byStatus(statusFilter)
      else if (userFilter.trim() && !statusFilter) data = await orderApi.byUser(userFilter.trim())
      else data = await orderApi.list()
      setRows(ensureDisplayIds(DISPLAY_ENTITY.ORDER, data || [], { sortField: 'createdDate' }))
      setSelected(new Set())
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  const validate = () => {
    const e = {}
    if (!form.userId.trim()) e.userId = 'Required'
    if (!form.items.length) e.items = 'Add at least one item'
    form.items.forEach((item, idx) => {
      if (!item.materialId.trim()) e[`materialId_${idx}`] = 'Required'
      if (!item.quantity || Number(item.quantity) <= 0) e[`quantity_${idx}`] = 'Must be positive'
      if (!item.price || Number(item.price) <= 0) e[`price_${idx}`] = 'Must be positive'
    })
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const createOrder = async () => {
    if (!validate()) return
    setSaving(true)
    try {
      await orderApi.create({
        userId: form.userId.trim(),
        items: form.items.map((i) => ({
          materialId: i.materialId.trim(),
          quantity: Number(i.quantity),
          price: Number(i.price),
        })),
      })
      toast.success('Order created successfully.')
      setModal(null)
      await loadAll()
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setSaving(false)
    }
  }

  const toggleSelect = (id) => {
    setSelected((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  const toggleAll = () => {
    if (selected.size === filtered.length) setSelected(new Set())
    else setSelected(new Set(filtered.map((o) => o.id)))
  }

  const bulkMark = async (status) => {
    if (!selected.size) return
    setSaving(true)
    let ok = 0
    for (const id of selected) {
      try {
        await orderApi.updateStatus(id, status)
        ok += 1
      } catch {
        /* continue */
      }
    }
    toast.success(`Updated ${ok} order(s) to ${status}.`)
    setSaving(false)
    await loadAll()
  }

  const subtotal = form.items.reduce((sum, i) => sum + (Number(i.quantity || 0) * Number(i.price || 0)), 0)

  if (loading && rows.length === 0) {
    return <div className="ord-loading"><SkeletonCard /><SkeletonCard /><SkeletonCard /></div>
  }

  return (
    <div className="ord">
      <section className="ord-hero">
        <div>
          <p className="ord-kicker">Fulfilment</p>
          <h2>Orders board</h2>
          <p>Track lifecycle progress, filter by customer, and update fulfilment status.</p>
        </div>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => { setForm({ userId: '', items: [emptyItem()] }); setErrors({}); setModal('create') }}
        >
          <Plus size={16} /> Create Order
        </button>
      </section>

      <section className="ord-filters card">
        <div className="card-body ord-filters-row">
          <div style={{ minWidth: 200, flex: 1 }}>
            <EntitySelect
              value={search}
              onChange={setSearch}
              options={orderOptions}
              loading={ordersLoading}
              placeholder="Filter by order ID"
              searchPlaceholder="Search order ID…"
              emptyMessage="No orders available"
            />
          </div>
          <div style={{ minWidth: 180, flex: 1 }}>
            <UserIdField
              value={userFilter}
              onChange={setUserFilter}
              placeholder="Filter by user"
              emptyMessage="No users available"
            />
          </div>
          <select className="select" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="">All statuses</option>
            {ORDER_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <button type="button" className="btn btn-secondary" onClick={applyServerFilters}>Apply</button>
          <button type="button" className="btn btn-secondary" onClick={() => { setUserFilter(''); setStatusFilter(''); setSearch(''); loadAll() }}>Reset</button>
        </div>
      </section>

      {selected.size > 0 ? (
        <section className="ord-bulk">
          <span>{selected.size} selected</span>
          <button type="button" className="btn btn-sm btn-secondary" disabled={saving} onClick={() => bulkMark('CONFIRMED')}>Mark CONFIRMED</button>
          <button type="button" className="btn btn-sm btn-secondary" disabled={saving} onClick={() => bulkMark('PAID')}>Mark PAID</button>
          <button type="button" className="btn btn-sm btn-secondary" disabled={saving} onClick={() => bulkMark('DELIVERED')}>Mark DELIVERED</button>
          <button type="button" className="btn btn-sm btn-ghost" onClick={() => setSelected(new Set())}>Clear</button>
        </section>
      ) : null}

      <section className="ord-split">
        <div className="card">
          <div className="card-header">
            <h3>Orders</h3>
            <span className="muted">{filtered.length} shown</span>
          </div>
          <div className="card-body">
            {filtered.length === 0 ? (
              <EmptyState title="No orders found." />
            ) : (
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>
                        <button type="button" className="icon-action" onClick={toggleAll} aria-label="Select all">
                          {selected.size && selected.size === filtered.length ? <CheckSquare size={15} /> : <Square size={15} />}
                        </button>
                      </th>
                      <th>Order</th>
                      <th>User</th>
                      <th>Progress</th>
                      <th>Total</th>
                      <th>Status</th>
                      <th>Created</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.map((o) => (
                      <tr key={o.id} className={selected.has(o.id) ? 'ord-row-selected' : ''}>
                        <td>
                          <button type="button" className="icon-action" onClick={() => toggleSelect(o.id)}>
                            {selected.has(o.id) ? <CheckSquare size={15} /> : <Square size={15} />}
                          </button>
                        </td>
                        <td><DisplayId type={DISPLAY_ENTITY.ORDER} id={o.id} /></td>
                        <td><DisplayId type={DISPLAY_ENTITY.USER} id={o.userId} /></td>
                        <td><OrderProgress status={o.status} /></td>
                        <td className="money">{formatMoney(o.totalPrice)}</td>
                        <td><StatusBadge label={o.status} tone={tone(o.status)} /></td>
                        <td>{formatDate(o.createdDate)}</td>
                        <td>
                          <div className="actions-cell">
                            <button type="button" className="icon-action" title="View" onClick={async () => {
                              try {
                                const data = await orderApi.getById(o.id)
                                setDetail(data)
                                setModal('view')
                              } catch (error) {
                                toast.error(getErrorMessage(error))
                              }
                            }}><Eye size={15} /></button>
                            <button type="button" className="btn btn-sm btn-secondary" onClick={() => { setDetail(o); setStatusValue(o.status); setModal('status') }}>Status</button>
                            <button type="button" className="icon-action danger" title="Delete" onClick={() => setConfirm(o)}><Trash2 size={15} /></button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        <div className="card">
          <div className="card-header"><h3>Lifecycle timeline</h3></div>
          <div className="card-body">
            {[...filtered]
              .sort((a, b) => new Date(b.createdDate || 0) - new Date(a.createdDate || 0))
              .slice(0, 6)
              .map((o) => (
                <div key={o.id} className="ord-timeline-item">
                  <OrderProgress status={o.status} large />
                  <div>
                    <strong><DisplayId type={DISPLAY_ENTITY.ORDER} id={o.id} /></strong>
                    <p><DisplayId type={DISPLAY_ENTITY.USER} id={o.userId} /> · {formatMoney(o.totalPrice)}</p>
                    <StatusBadge label={o.status} tone={tone(o.status)} />
                  </div>
                  <time>{formatDate(o.createdDate)}</time>
                </div>
              ))}
            {filtered.length === 0 ? <EmptyState title="No timeline data" /> : null}
          </div>
        </div>
      </section>

      <Modal
        open={modal === 'create'}
        title="Create Order"
        wide
        onClose={() => setModal(null)}
        footer={(
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="button" className="btn btn-primary" disabled={saving} onClick={createOrder}>{saving ? 'Creating…' : 'Create order'}</button>
          </>
        )}
      >
        <div className="form-grid">
          <div className="field full">
            <label>User ID <span className="req">*</span></label>
            <UserIdField
              value={form.userId}
              onChange={(id) => setForm({ ...form, userId: id })}
              error={errors.userId}
              placeholder="Select User ID"
              emptyMessage="No users available"
            />
          </div>
        </div>
        <div className="item-rows" style={{ marginTop: '1rem' }}>
          {form.items.map((item, idx) => {
            const materialOptions = item.supplierId
              ? allMaterialOptions.filter((m) => String(m.raw?.supplierId || '') === String(item.supplierId))
              : allMaterialOptions
            return (
            <div className="item-row" key={idx}>
              <div className="field">
                <label>Supplier (filter)</label>
                <EntitySelect
                  value={item.supplierId || ''}
                  onChange={(supplierId) => {
                    const items = [...form.items]
                    items[idx] = { ...item, supplierId, materialId: '', price: item.price }
                    setForm({ ...form, items })
                  }}
                  options={supplierOptions}
                  loading={suppliersLoading}
                  placeholder="Optional supplier filter"
                  searchPlaceholder="Search supplier…"
                  emptyMessage="No suppliers available"
                />
              </div>
              <div className="field">
                <label>materialId <span className="req">*</span></label>
                <EntitySelect
                  value={item.materialId}
                  onChange={(materialId) => {
                    const mat = allMaterials.find((m) => m.id === materialId)
                    const items = [...form.items]
                    items[idx] = {
                      ...item,
                      materialId,
                      price: mat?.price != null ? String(mat.price) : item.price,
                      supplierId: item.supplierId || mat?.supplierId || '',
                    }
                    setForm({ ...form, items })
                  }}
                  options={materialOptions}
                  loading={materialsLoading}
                  disabled={materialsLoading}
                  placeholder="Select Material ID"
                  searchPlaceholder="Search material…"
                  emptyMessage={item.supplierId ? 'No materials for this supplier' : 'No materials available'}
                />
                {errors[`materialId_${idx}`] && <div className="field-error">{errors[`materialId_${idx}`]}</div>}
              </div>
              <div className="field">
                <label>quantity <span className="req">*</span></label>
                <input className="input" type="number" min="1" value={item.quantity} onChange={(e) => {
                  const items = [...form.items]
                  items[idx] = { ...item, quantity: e.target.value }
                  setForm({ ...form, items })
                }} />
                {errors[`quantity_${idx}`] && <div className="field-error">{errors[`quantity_${idx}`]}</div>}
              </div>
              <div className="field">
                <label>price <span className="req">*</span></label>
                <input className="input" type="number" min="0.01" step="0.01" value={item.price} onChange={(e) => {
                  const items = [...form.items]
                  items[idx] = { ...item, price: e.target.value }
                  setForm({ ...form, items })
                }} />
                {errors[`price_${idx}`] && <div className="field-error">{errors[`price_${idx}`]}</div>}
              </div>
              <button
                type="button"
                className="btn btn-secondary"
                disabled={form.items.length === 1}
                onClick={() => setForm({ ...form, items: form.items.filter((_, i) => i !== idx) })}
              >
                Remove
              </button>
            </div>
            )
          })}
        </div>
        <div className="toolbar" style={{ marginTop: '0.75rem' }}>
          <button type="button" className="btn btn-secondary" onClick={() => setForm({ ...form, items: [...form.items, emptyItem()] })}>+ Add Item</button>
          <div className="spacer" />
          <strong>UI subtotal: {formatMoney(subtotal)}</strong>
        </div>
      </Modal>

      <Modal open={modal === 'view'} title="Order Details" onClose={() => setModal(null)} wide>
        {detail && (
          <>
            <OrderProgress status={detail.status} large />
            <dl className="detail-grid" style={{ marginTop: '1rem' }}>
              <div><dt>ID</dt><dd><DisplayId type={DISPLAY_ENTITY.ORDER} id={detail.id} /></dd></div>
              <div><dt>User</dt><dd><DisplayId type={DISPLAY_ENTITY.USER} id={detail.userId} /></dd></div>
              <div><dt>Status</dt><dd><StatusBadge label={detail.status} tone={tone(detail.status)} /></dd></div>
              <div><dt>Total</dt><dd className="money">{formatMoney(detail.totalPrice)}</dd></div>
              <div><dt>Created</dt><dd>{formatDate(detail.createdDate)}</dd></div>
              <div><dt>Updated</dt><dd>{formatDate(detail.updatedDate)}</dd></div>
            </dl>
            <div className="table-wrap" style={{ marginTop: '1rem' }}>
              <table className="data-table">
                <thead>
                  <tr><th>materialId</th><th>quantity</th><th>price</th></tr>
                </thead>
                <tbody>
                  {(detail.items || []).map((item, idx) => (
                    <tr key={idx}>
                      <td><DisplayId type={DISPLAY_ENTITY.MATERIAL} id={item.materialId} /></td>
                      <td>{item.quantity}</td>
                      <td>{formatMoney(item.price)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </Modal>

      <Modal
        open={modal === 'status'}
        title="Update Order Status"
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
                  await orderApi.updateStatus(detail.id, statusValue)
                  toast.success('Order status updated successfully.')
                  setModal(null)
                  await loadAll()
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
            {ORDER_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
      </Modal>

      <ConfirmDialog
        open={Boolean(confirm)}
        title="Delete order"
        message="Delete this order? Reserved stock will be released unless already CANCELLED."
        confirmLabel="Delete"
        danger
        loading={saving}
        onCancel={() => setConfirm(null)}
        onConfirm={async () => {
          setSaving(true)
          try {
            await orderApi.remove(confirm.id)
            toast.success('Order deleted successfully.')
            setConfirm(null)
            await loadAll()
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

function OrderProgress({ status, large }) {
  const current = String(status || '').toUpperCase()
  const activeIdx = current === 'CANCELLED' ? -1 : Math.max(0, FLOW.indexOf(current === 'CONFIRMED' ? 'CONFIRMED' : current))
  return (
    <div className={`ord-progress ${large ? 'large' : ''}`} title={current}>
      {FLOW.map((step, idx) => (
        <span
          key={step}
          className={`ord-step ${current === 'CANCELLED' ? 'cancelled' : idx <= activeIdx ? 'done' : ''}`}
        />
      ))}
    </div>
  )
}

function tone(status) {
  const s = String(status || '').toUpperCase()
  if (s === 'DELIVERED' || s === 'CONFIRMED' || s === 'PAID') return 'success'
  if (s === 'PENDING') return 'warning'
  if (s === 'CANCELLED') return 'danger'
  return 'neutral'
}
