import { useEffect, useMemo, useState } from 'react'
import { AlertTriangle, Plus, Warehouse } from 'lucide-react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import EmptyState from '../components/common/EmptyState'
import Modal from '../components/common/Modal'
import StatusBadge from '../components/common/StatusBadge'
import { SkeletonCard } from '../components/common/Skeleton'
import { orderApi } from '../services/orderApi'
import { getErrorMessage } from '../services/api'
import { useToast } from '../components/common/Toast'
import EntitySelect from '../components/common/EntitySelect'
import DisplayId from '../components/common/DisplayId'
import { useMaterialOptions, useSupplierOptions } from '../hooks/useEntityOptions'
import { formatDate, inventoryStatus } from '../utils/format'
import { DISPLAY_ENTITY, ensureDisplayId } from '../utils/displayId'
import '../styles/inventory.css'

export default function Inventory() {
  const toast = useToast()
  const [tab, setTab] = useState('stock')
  const [loading, setLoading] = useState(true)
  const [rows, setRows] = useState([])
  const [history, setHistory] = useState([])
  const [modal, setModal] = useState(null)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState({
    materialId: '',
    availableQuantity: '',
    reservedQuantity: '0',
    minimumStock: '',
    supplierId: '',
  })
  const [actionForm, setActionForm] = useState({ materialId: '', quantity: '', supplierId: '' })
  const { options: supplierOptions, loading: suppliersLoading } = useSupplierOptions()
  const {
    options: allMaterialOptions,
    loading: materialsLoading,
    error: materialsError,
    reload: reloadMaterials,
    all: allMaterials,
  } = useMaterialOptions()
  const inventoryByMaterialId = useMemo(() => {
    const map = new Map()
    rows.forEach((item) => {
      const key = String(item.materialId ?? '').trim()
      if (key) map.set(key, item)
    })
    return map
  }, [rows])
  const formMaterialOptions = form.supplierId
    ? allMaterialOptions.filter((m) => String(m.raw?.supplierId || '') === String(form.supplierId))
    : allMaterialOptions
  const actionMaterialOptions = actionForm.supplierId
    ? allMaterialOptions.filter((m) => String(m.raw?.supplierId || '') === String(actionForm.supplierId))
    : allMaterialOptions
  const existingInventory = form.materialId
    ? inventoryByMaterialId.get(String(form.materialId).trim())
    : null
  const isUpdatingInventory = Boolean(existingInventory)

  const load = async () => {
    setLoading(true)
    try {
      const [inv, hist] = await Promise.all([
        orderApi.listInventory(),
        orderApi.inventoryHistory(),
      ])
      setRows(inv || [])
      setHistory(hist || [])
    } catch (error) {
      toast.error(getErrorMessage(error))
      setRows([])
      setHistory([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    reloadMaterials()
  }, [reloadMaterials])

  useEffect(() => {
    if (modal === 'create') reloadMaterials()
  }, [modal, reloadMaterials])

  const low = useMemo(
    () => rows.filter((item) => inventoryStatus(item).tone === 'warning' || inventoryStatus(item).tone === 'danger'),
    [rows],
  )

  const chartData = useMemo(
    () => rows.slice(0, 8).map((item) => ({
      name: ensureDisplayId(DISPLAY_ENTITY.MATERIAL, item.materialId) || String(item.materialId || '').slice(0, 10),
      available: Number(item.availableQuantity || 0),
      reserved: Number(item.reservedQuantity || 0),
    })),
    [rows],
  )

  const saveInventory = async () => {
    if (!form.materialId.trim()) {
      toast.error('materialId is required.')
      return
    }
    const payload = {
      materialId: form.materialId.trim(),
      availableQuantity: Number(form.availableQuantity || 0),
      reservedQuantity: Number(form.reservedQuantity || 0),
      minimumStock: Number(form.minimumStock || 0),
    }
    setSaving(true)
    try {
      if (isUpdatingInventory) {
        await orderApi.updateInventory(payload.materialId, payload)
        toast.success('Inventory levels updated.')
      } else {
        await orderApi.createInventory(payload)
        toast.success('Inventory record created.')
      }
      setModal(null)
      await load()
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setSaving(false)
    }
  }

  const runAction = async (type) => {
    if (!actionForm.materialId.trim() || !actionForm.quantity || Number(actionForm.quantity) <= 0) {
      toast.error('materialId and positive quantity are required.')
      return
    }
    setSaving(true)
    try {
      if (type === 'reserve') {
        await orderApi.reserve(actionForm.materialId.trim(), Number(actionForm.quantity))
        toast.success('Inventory reserved successfully.')
      } else {
        await orderApi.release(actionForm.materialId.trim(), Number(actionForm.quantity))
        toast.success('Inventory released successfully.')
      }
      setModal(null)
      await load()
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setSaving(false)
    }
  }

  if (loading && rows.length === 0) {
    return <div className="inv-loading"><SkeletonCard /><SkeletonCard /><SkeletonCard /></div>
  }

  return (
    <div className="inv">
      <section className="inv-hero">
        <div>
          <p className="inv-kicker"><Warehouse size={14} /> Warehouse</p>
          <h2>Inventory floor</h2>
          <p>Stock levels, reservations, and movement history from order-inventory service.</p>
        </div>
        <div className="inv-hero-actions">
          <button type="button" className="btn btn-secondary" onClick={() => { setActionForm({ materialId: '', quantity: '', supplierId: '' }); setModal('reserve') }}>Reserve</button>
          <button type="button" className="btn btn-secondary" onClick={() => { setActionForm({ materialId: '', quantity: '', supplierId: '' }); setModal('release') }}>Release</button>
          <button type="button" className="btn btn-primary" onClick={() => {
            reloadMaterials()
            setForm({ materialId: '', availableQuantity: '', reservedQuantity: '0', minimumStock: '', supplierId: '' })
            setModal('create')
          }}>
            <Plus size={16} /> Add Inventory
          </button>
        </div>
      </section>

      {low.length > 0 ? (
        <section className="inv-alert">
          <AlertTriangle size={18} />
          <div>
            <strong>{low.length} SKUs need attention</strong>
            <p>{low.slice(0, 4).map((i) => ensureDisplayId(DISPLAY_ENTITY.MATERIAL, i.materialId) || i.materialId).join(', ')}{low.length > 4 ? '…' : ''}</p>
          </div>
        </section>
      ) : null}

      <section className="inv-stats">
        <div className="inv-stat"><span>Locations</span><strong>{rows.length}</strong></div>
        <div className="inv-stat"><span>Low / out</span><strong>{low.length}</strong></div>
        <div className="inv-stat"><span>History events</span><strong>{history.length}</strong></div>
      </section>

      <div className="tabs">
        <button type="button" className={`tab ${tab === 'stock' ? 'active' : ''}`} onClick={() => setTab('stock')}>Stock floor</button>
        <button type="button" className={`tab ${tab === 'history' ? 'active' : ''}`} onClick={() => setTab('history')}>Movement history</button>
      </div>

      {tab === 'stock' && (
        <section className="inv-grid">
          <div className="card">
            <div className="card-header"><h3>Availability chart</h3></div>
            <div className="card-body" style={{ height: 240 }}>
              {chartData.length === 0 ? (
                <EmptyState title="No inventory found." />
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                    <XAxis dataKey="name" tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                    <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                    <Tooltip />
                    <Bar dataKey="available" fill="var(--accent)" radius={[6, 6, 0, 0]} />
                    <Bar dataKey="reserved" fill="var(--info)" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>

          <div className="inv-bins">
            {rows.length === 0 ? (
              <div className="card"><EmptyState title="No inventory found." /></div>
            ) : rows.map((item) => {
              const st = inventoryStatus(item)
              const avail = Number(item.availableQuantity || 0)
              const reserved = Number(item.reservedQuantity || 0)
              const total = Math.max(avail + reserved, 1)
              return (
                <article key={item.id || item.materialId} className={`inv-bin ${st.tone}`}>
                  <header>
                    <strong><DisplayId type={DISPLAY_ENTITY.MATERIAL} id={item.materialId} /></strong>
                    <StatusBadge label={st.label} tone={st.tone} />
                  </header>
                  <div className="inv-meter" aria-hidden="true">
                    <span style={{ width: `${(avail / total) * 100}%` }} className="avail" />
                    <span style={{ width: `${(reserved / total) * 100}%` }} className="reserved" />
                  </div>
                  <dl>
                    <div><dt>Available</dt><dd>{item.availableQuantity}</dd></div>
                    <div><dt>Reserved</dt><dd>{item.reservedQuantity}</dd></div>
                    <div><dt>Minimum</dt><dd>{item.minimumStock}</dd></div>
                  </dl>
                  <div className="actions-cell">
                    <button type="button" className="btn btn-sm btn-secondary" onClick={() => {
                      const mat = allMaterials.find((m) => m.id === item.materialId)
                      setActionForm({ materialId: item.materialId, quantity: '', supplierId: mat?.supplierId || '' })
                      setModal('reserve')
                    }}>Reserve</button>
                    <button type="button" className="btn btn-sm btn-secondary" onClick={() => {
                      const mat = allMaterials.find((m) => m.id === item.materialId)
                      setActionForm({ materialId: item.materialId, quantity: '', supplierId: mat?.supplierId || '' })
                      setModal('release')
                    }}>Release</button>
                  </div>
                </article>
              )
            })}
          </div>
        </section>
      )}

      {tab === 'history' && (
        history.length === 0 ? (
          <div className="card"><EmptyState title="No inventory history available." /></div>
        ) : (
          <div className="table-wrap card">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Material</th>
                  <th>Action</th>
                  <th>Quantity</th>
                  <th>Reference</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {history.map((h) => (
                  <tr key={h.id}>
                    <td><DisplayId type={DISPLAY_ENTITY.MATERIAL} id={h.materialId} /></td>
                    <td><StatusBadge label={h.action} tone={h.action === 'RESERVED' ? 'warning' : 'info'} /></td>
                    <td>{h.quantity}</td>
                    <td>{h.reference}</td>
                    <td>{formatDate(h.date)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}

      <Modal
        open={modal === 'create'}
        title={isUpdatingInventory ? 'Update Inventory' : 'Add Inventory'}
        onClose={() => setModal(null)}
        footer={(
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="button" className="btn btn-primary" disabled={saving} onClick={saveInventory}>
              {saving ? 'Saving…' : isUpdatingInventory ? 'Update' : 'Create'}
            </button>
          </>
        )}
      >
        <div className="form-grid">
          <div className="field full">
            <label>Supplier (filter)</label>
            <EntitySelect
              value={form.supplierId}
              onChange={(supplierId) => setForm({ ...form, supplierId, materialId: '', availableQuantity: '', reservedQuantity: '0', minimumStock: '' })}
              options={supplierOptions}
              loading={suppliersLoading}
              placeholder="Optional supplier filter"
              searchPlaceholder="Search supplier…"
              emptyMessage="No suppliers available"
            />
          </div>
          <div className="field full">
            <label>materialId <span className="req">*</span></label>
            <EntitySelect
              value={form.materialId}
              onChange={(materialId) => {
                const mat = allMaterials.find((m) => m.id === materialId)
                const existing = inventoryByMaterialId.get(String(materialId || '').trim())
                setForm({
                  ...form,
                  materialId,
                  supplierId: form.supplierId || mat?.supplierId || '',
                  availableQuantity: existing ? String(existing.availableQuantity ?? 0) : '',
                  reservedQuantity: existing ? String(existing.reservedQuantity ?? 0) : '0',
                  minimumStock: existing ? String(existing.minimumStock ?? 0) : '',
                })
              }}
              options={formMaterialOptions}
              loading={materialsLoading}
              loadingMessage="Loading materials…"
              error={materialsError ? 'Unable to load materials' : ''}
              disabled={materialsLoading}
              placeholder="Select Material ID"
              searchPlaceholder="Search material…"
              emptyMessage={form.supplierId ? 'No materials for this supplier' : 'No materials available'}
            />
            {isUpdatingInventory ? (
              <p className="muted" style={{ marginTop: 8, fontSize: '0.85rem' }}>
                Inventory already exists for this material — saving will update stock levels.
              </p>
            ) : null}
          </div>
          <div className="field">
            <label>availableQuantity</label>
            <input className="input" type="number" min="0" value={form.availableQuantity} onChange={(e) => setForm({ ...form, availableQuantity: e.target.value })} />
          </div>
          <div className="field">
            <label>reservedQuantity</label>
            <input className="input" type="number" min="0" value={form.reservedQuantity} onChange={(e) => setForm({ ...form, reservedQuantity: e.target.value })} />
          </div>
          <div className="field full">
            <label>minimumStock</label>
            <input className="input" type="number" min="0" value={form.minimumStock} onChange={(e) => setForm({ ...form, minimumStock: e.target.value })} />
          </div>
        </div>
      </Modal>

      <Modal
        open={modal === 'reserve' || modal === 'release'}
        title={modal === 'reserve' ? 'Reserve inventory' : 'Release inventory'}
        onClose={() => setModal(null)}
        footer={(
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="button" className="btn btn-primary" disabled={saving} onClick={() => runAction(modal)}>
              {saving ? 'Working…' : modal === 'reserve' ? 'Reserve' : 'Release'}
            </button>
          </>
        )}
      >
        <div className="form-grid">
          <div className="field full">
            <label>Supplier (filter)</label>
            <EntitySelect
              value={actionForm.supplierId}
              onChange={(supplierId) => setActionForm({ ...actionForm, supplierId, materialId: '' })}
              options={supplierOptions}
              loading={suppliersLoading}
              placeholder="Optional supplier filter"
              searchPlaceholder="Search supplier…"
              emptyMessage="No suppliers available"
            />
          </div>
          <div className="field full">
            <label>materialId <span className="req">*</span></label>
            <EntitySelect
              value={actionForm.materialId}
              onChange={(materialId) => {
                const mat = allMaterials.find((m) => m.id === materialId)
                setActionForm({ ...actionForm, materialId, supplierId: actionForm.supplierId || mat?.supplierId || '' })
              }}
              options={actionMaterialOptions}
              loading={materialsLoading}
              loadingMessage="Loading materials…"
              error={materialsError ? 'Unable to load materials' : ''}
              disabled={materialsLoading}
              placeholder="Select Material ID"
              searchPlaceholder="Search material…"
              emptyMessage={actionForm.supplierId ? 'No materials for this supplier' : 'No materials available'}
            />
          </div>
          <div className="field full">
            <label>quantity <span className="req">*</span></label>
            <input className="input" type="number" min="1" value={actionForm.quantity} onChange={(e) => setActionForm({ ...actionForm, quantity: e.target.value })} />
          </div>
        </div>
      </Modal>
    </div>
  )
}
