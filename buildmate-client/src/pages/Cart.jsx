import { useCallback, useEffect, useRef, useState } from 'react'
import { Info, Minus, Plus, ShoppingCart, Trash2 } from 'lucide-react'
import ConfirmDialog from '../components/common/ConfirmDialog'
import Modal from '../components/common/Modal'
import { SkeletonCard } from '../components/common/Skeleton'
import EntitySelect from '../components/common/EntitySelect'
import UserIdField from '../components/common/UserIdField'
import DisplayId from '../components/common/DisplayId'
import { useAuth } from '../auth/AuthContext'
import { currentUserId, isAdminUser } from '../auth/roles'
import { orderApi } from '../services/orderApi'
import { getErrorMessage } from '../services/api'
import { useToast } from '../components/common/Toast'
import { useMaterialOptions, useSupplierOptions } from '../hooks/useEntityOptions'
import { formatMoney } from '../utils/format'
import { DISPLAY_ENTITY, ensureDisplayId } from '../utils/displayId'
import '../styles/cart.css'

const VAT_RATE = 0.15

function materialInitials(materialId) {
  const code = ensureDisplayId(DISPLAY_ENTITY.MATERIAL, materialId) || String(materialId || '?')
  const parts = code.split('_')
  if (parts.length > 1) return `${parts[0]}${parts[1]}`.slice(0, 3)
  if (code.length <= 2) return code.toUpperCase()
  return code.slice(0, 2).toUpperCase()
}

function CartEmptyIllustration({ title = 'Your cart is empty', desc = 'Add materials to review totals and checkout.' }) {
  return (
    <div className="cart-empty-illustration" aria-hidden="true">
      <svg viewBox="0 0 240 180" fill="none" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="120" cy="158" rx="72" ry="10" fill="var(--border)" opacity="0.5" />
        <path
          d="M72 58h96l-8 72H80L72 58z"
          fill="var(--surface-muted)"
          stroke="var(--border-strong)"
          strokeWidth="2"
          strokeLinejoin="round"
        />
        <path d="M88 58V48a24 24 0 0 1 48 0v10" stroke="var(--accent)" strokeWidth="2.5" strokeLinecap="round" />
        <circle cx="96" cy="118" r="6" fill="var(--accent-soft)" stroke="var(--accent)" strokeWidth="2" />
        <circle cx="144" cy="118" r="6" fill="var(--accent-soft)" stroke="var(--accent)" strokeWidth="2" />
        <path d="M108 82h24" stroke="var(--border-strong)" strokeWidth="2" strokeLinecap="round" strokeDasharray="4 4" />
      </svg>
      <p className="cart-empty-title">{title}</p>
      <p className="cart-empty-desc">{desc}</p>
    </div>
  )
}

/**
 * Cart backend surface (via Gateway /api):
 * POST /cart  { userId, materialId, quantity, price }  — appends line (real Mongo user id)
 * GET /cart/{userId} — returns cart or empty { userId, items: [] }
 * DELETE /cart/{userId}  — clears cart
 */
export default function Cart() {
  const toast = useToast()
  const { user, isAuthenticated, booting } = useAuth()
  const admin = isAdminUser(user)
  const selfId = currentUserId(user)

  const [userId, setUserId] = useState('')
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(false)
  const [loaded, setLoaded] = useState(false)
  const [loadError, setLoadError] = useState(null) // null | 'auth' | 'api'
  const [modal, setModal] = useState(false)
  const [confirmClear, setConfirmClear] = useState(false)
  const [saving, setSaving] = useState(false)
  const [updatingIdx, setUpdatingIdx] = useState(null)
  const [form, setForm] = useState({ materialId: '', quantity: '', price: '', supplierId: '' })
  const [errors, setErrors] = useState({})
  const { options: supplierOptions, loading: suppliersLoading } = useSupplierOptions()
  const { options: allMaterialOptions, loading: materialsLoading, all: allMaterials } = useMaterialOptions()
  const materialOptions = form.supplierId
    ? allMaterialOptions.filter((m) => String(m.raw?.supplierId || '') === String(form.supplierId))
    : allMaterialOptions

  const loadSeq = useRef(0)

  // Non-admins: always lock to authenticated Mongo/JWT user id (never displayId / demo-user).
  useEffect(() => {
    if (booting) return
    if (!isAuthenticated || !selfId) {
      if (!admin) setUserId('')
      return
    }
    if (!admin) {
      setUserId(selfId)
    } else {
      setUserId((prev) => prev || selfId)
    }
  }, [booting, isAuthenticated, selfId, admin])

  const loadCartFor = useCallback(async (rawId, { silentAuthToast = false } = {}) => {
    const id = String(rawId ?? '').trim()
    if (!id) {
      setLoadError(isAuthenticated ? 'api' : 'auth')
      setLoaded(true)
      setCart(null)
      return
    }

    const seq = ++loadSeq.current
    setLoading(true)
    setLoadError(null)
    try {
      const data = await orderApi.getCart(id)
      if (seq !== loadSeq.current) return
      if (data?.id) ensureDisplayId(DISPLAY_ENTITY.CART, data.id)
      if (data?.userId) ensureDisplayId(DISPLAY_ENTITY.USER, data.userId)
      setCart(data && typeof data === 'object'
        ? { ...data, userId: data.userId || id, items: Array.isArray(data.items) ? data.items : [] }
        : { userId: id, items: [] })
      setLoaded(true)
    } catch (error) {
      if (seq !== loadSeq.current) return
      const status = error?.response?.status
      setCart(null)
      setLoaded(true)
      if (status === 401 || status === 403) {
        setLoadError('auth')
        if (!silentAuthToast) toast.error('Please sign in again.')
      } else {
        setLoadError('api')
        toast.error(getErrorMessage(error) || 'Unable to load your cart')
      }
    } finally {
      if (seq === loadSeq.current) setLoading(false)
    }
  }, [isAuthenticated, toast])

  // Auto-load whenever the real backend userId is ready.
  useEffect(() => {
    if (booting) return
    if (!isAuthenticated) {
      setLoaded(true)
      setLoadError('auth')
      setCart(null)
      return
    }
    if (!userId.trim()) return
    loadCartFor(userId, { silentAuthToast: true })
  }, [booting, isAuthenticated, userId, loadCartFor])

  const validate = () => {
    const e = {}
    if (!form.materialId.trim()) e.materialId = 'Required'
    if (!form.quantity || Number(form.quantity) <= 0) e.quantity = 'Must be positive'
    if (!form.price || Number(form.price) <= 0) e.price = 'Must be positive'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const addItem = async () => {
    const id = userId.trim()
    if (!id) {
      toast.error(isAuthenticated ? 'Unable to resolve your user ID.' : 'Please sign in again.')
      return
    }
    if (!validate()) return
    setSaving(true)
    try {
      const data = await orderApi.addToCart({
        userId: id,
        materialId: form.materialId.trim(),
        quantity: Number(form.quantity),
        price: Number(form.price),
      })
      setCart(data)
      setLoadError(null)
      setLoaded(true)
      toast.success('Item added to cart.')
      setModal(false)
      setForm({ materialId: '', quantity: '', price: '', supplierId: '' })
    } catch (error) {
      const status = error?.response?.status
      if (status === 401 || status === 403) toast.error('Please sign in again.')
      else toast.error(getErrorMessage(error))
    } finally {
      setSaving(false)
    }
  }

  const incrementItem = async (item, idx) => {
    const id = userId.trim()
    if (!id) return
    setUpdatingIdx(idx)
    try {
      const data = await orderApi.addToCart({
        userId: id,
        materialId: item.materialId,
        quantity: 1,
        price: Number(item.price),
      })
      setCart(data)
      toast.success('Quantity increased.')
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setUpdatingIdx(null)
    }
  }

  const decrementItem = () => {
    toast.info('Decreasing quantity is not supported by the API. Use Clear cart to reset.')
  }

  const items = cart?.items || []
  const subtotal = items.reduce((sum, i) => sum + Number(i.quantity || 0) * Number(i.price || 0), 0)
  const tax = subtotal * VAT_RATE
  const total = subtotal + tax
  const itemCount = items.reduce((sum, i) => sum + Number(i.quantity || 0), 0)

  const showAuthGate = !booting && (!isAuthenticated || loadError === 'auth')
  const showApiError = loaded && !loading && loadError === 'api'
  const showEmpty = loaded && !loading && !loadError && cart && items.length === 0
  const showCart = loaded && !loading && !loadError && cart && items.length > 0

  return (
    <div className="cart-page">
      <section className="cart-hero">
        <div>
          <p className="cart-kicker"><ShoppingCart size={14} style={{ verticalAlign: -2, marginRight: 4 }} /> Checkout</p>
          <h2>Shopping cart</h2>
          <p>Review line items and totals before placing an order.</p>
        </div>
        <div className="cart-user-bar">
          <div style={{ minWidth: 220, flex: 1 }}>
            <UserIdField
              value={userId}
              onChange={(id) => {
                setUserId(id)
                setLoaded(false)
                setLoadError(null)
                setCart(null)
              }}
              disabled={loading || !isAuthenticated}
              placeholder="Select User ID"
              emptyMessage="No users available"
            />
          </div>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => loadCartFor(userId)}
            disabled={loading || !userId.trim() || !isAuthenticated}
          >
            {loading ? 'Loading…' : 'Load cart'}
          </button>
        </div>
      </section>

      <div className="cart-notice">
        <Info size={18} style={{ flexShrink: 0, color: 'var(--info)' }} />
        <div>
          <strong>API note</strong> — Backend supports view cart, add line item, and clear cart only.
          Per-line quantity decrease / remove are not exposed by the API. Use + to add one more unit.
        </div>
      </div>

      {booting || loading ? (
        <div className="cart-loading" aria-busy="true" aria-live="polite">
          <p className="cart-empty-hint" style={{ marginBottom: '0.75rem' }}>Loading cart…</p>
          <SkeletonCard />
          <SkeletonCard />
        </div>
      ) : showAuthGate ? (
        <div className="card cart-empty-panel">
          <CartEmptyIllustration
            title="Please sign in again"
            desc="Your session is missing or expired. Sign in to load your cart."
          />
        </div>
      ) : showApiError ? (
        <div className="card cart-empty-panel">
          <CartEmptyIllustration
            title="Unable to load your cart"
            desc="The cart request failed. Check that the Gateway and Order service are running, then try Load cart."
          />
          <button type="button" className="btn btn-secondary" onClick={() => loadCartFor(userId)} disabled={loading || !userId.trim()}>
            Retry
          </button>
        </div>
      ) : showEmpty ? (
        <div className="cart-layout">
          <section className="cart-items-panel">
            <header className="cart-items-head">
              <h3>Line items (0)</h3>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={() => { setErrors({}); setForm({ materialId: '', quantity: '', price: '', supplierId: '' }); setModal(true) }}
              >
                <Plus size={15} /> Add item
              </button>
            </header>
            <div className="cart-empty-panel">
              <CartEmptyIllustration />
            </div>
          </section>
          <aside className="cart-summary">
            <header className="cart-summary-head">
              <h3>Order summary</h3>
            </header>
            <div className="cart-summary-body">
              <div className="cart-summary-row">
                <span>Customer</span>
                <strong><DisplayId type={DISPLAY_ENTITY.USER} id={cart?.userId || userId} /></strong>
              </div>
              <div className="cart-summary-row">
                <span>Items</span>
                <strong>0</strong>
              </div>
              <div className="cart-summary-divider" />
              <div className="cart-summary-row total">
                <span>Total</span>
                <strong>{formatMoney(0)}</strong>
              </div>
            </div>
            <div className="cart-summary-actions">
              <button type="button" className="btn btn-primary" onClick={() => { setErrors({}); setModal(true) }}>
                <Plus size={16} /> Add item
              </button>
            </div>
          </aside>
        </div>
      ) : showCart ? (
        <div className="cart-layout">
          <section className="cart-items-panel">
            <header className="cart-items-head">
              <h3>Line items ({items.length})</h3>
              <button type="button" className="btn btn-primary btn-sm" onClick={() => { setErrors({}); setForm({ materialId: '', quantity: '', price: '', supplierId: '' }); setModal(true) }}>
                <Plus size={15} /> Add item
              </button>
            </header>
            {items.map((item, idx) => {
              const lineTotal = Number(item.quantity) * Number(item.price)
              const busy = updatingIdx === idx
              return (
                <div key={`${item.materialId}-${idx}`} className="cart-line">
                  <div className="cart-line-thumb">{materialInitials(item.materialId)}</div>
                  <div className="cart-line-info">
                    <strong><DisplayId type={DISPLAY_ENTITY.MATERIAL} id={item.materialId} /></strong>
                    <span>{formatMoney(item.price)} each</span>
                  </div>
                  <div className="cart-line-qty">
                    <button
                      type="button"
                      className="cart-qty-btn"
                      title="Decrease (not supported by API)"
                      onClick={decrementItem}
                      disabled={busy}
                    >
                      <Minus size={14} />
                    </button>
                    <span className="cart-qty-value">{item.quantity}</span>
                    <button
                      type="button"
                      className="cart-qty-btn cart-qty-btn--plus"
                      title="Add one more"
                      onClick={() => incrementItem(item, idx)}
                      disabled={busy}
                    >
                      <Plus size={14} />
                    </button>
                  </div>
                  <div className="cart-line-total">{formatMoney(lineTotal)}</div>
                </div>
              )
            })}
          </section>

          <aside className="cart-summary">
            <header className="cart-summary-head">
              <h3>Order summary</h3>
            </header>
            <div className="cart-summary-body">
              <div className="cart-summary-row">
                <span>Customer</span>
                <strong><DisplayId type={DISPLAY_ENTITY.USER} id={cart.userId} /></strong>
              </div>
              <div className="cart-summary-row">
                <span>Items</span>
                <strong>{itemCount}</strong>
              </div>
              <div className="cart-summary-divider" />
              <div className="cart-summary-row">
                <span>Subtotal</span>
                <strong>{formatMoney(subtotal)}</strong>
              </div>
              <div className="cart-summary-row">
                <span>Tax (VAT 15%)</span>
                <strong>{formatMoney(tax)}</strong>
              </div>
              <div className="cart-summary-row total">
                <span>Total</span>
                <strong>{formatMoney(total)}</strong>
              </div>
            </div>
            <div className="cart-summary-actions">
              <button type="button" className="btn btn-primary" onClick={() => { setErrors({}); setModal(true) }}>
                <Plus size={16} /> Add item
              </button>
              <button type="button" className="btn btn-danger" disabled={!cart || items.length === 0} onClick={() => setConfirmClear(true)}>
                <Trash2 size={16} /> Clear cart
              </button>
            </div>
          </aside>
        </div>
      ) : null}

      <Modal
        open={modal}
        title="Add to cart"
        onClose={() => setModal(false)}
        footer={(
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setModal(false)}>Cancel</button>
            <button type="button" className="btn btn-primary" disabled={saving || !userId.trim()} onClick={addItem}>{saving ? 'Adding…' : 'Add item'}</button>
          </>
        )}
      >
        <div className="form-grid">
          <div className="field full">
            <label>Supplier (filter)</label>
            <EntitySelect
              value={form.supplierId}
              onChange={(supplierId) => setForm({ ...form, supplierId, materialId: '' })}
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
                setForm({
                  ...form,
                  materialId,
                  price: mat?.price != null ? String(mat.price) : form.price,
                  supplierId: form.supplierId || mat?.supplierId || '',
                })
              }}
              options={materialOptions}
              loading={materialsLoading}
              disabled={materialsLoading}
              placeholder="Select Material ID"
              searchPlaceholder="Search material…"
              emptyMessage={form.supplierId ? 'No materials for this supplier' : 'No materials available'}
            />
            {errors.materialId && <div className="field-error">{errors.materialId}</div>}
          </div>
          <div className="field">
            <label>quantity <span className="req">*</span></label>
            <input className="input" type="number" min="1" value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })} />
            {errors.quantity && <div className="field-error">{errors.quantity}</div>}
          </div>
          <div className="field">
            <label>price <span className="req">*</span></label>
            <input className="input" type="number" min="0.01" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} />
            {errors.price && <div className="field-error">{errors.price}</div>}
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        open={confirmClear}
        title="Clear cart"
        message={`Clear entire cart for “${ensureDisplayId(DISPLAY_ENTITY.USER, userId) || 'this user'}”?`}
        confirmLabel="Clear"
        danger
        loading={saving}
        onCancel={() => setConfirmClear(false)}
        onConfirm={async () => {
          const id = userId.trim()
          if (!id) return
          setSaving(true)
          try {
            await orderApi.clearCart(id)
            toast.success('Cart cleared successfully.')
            setConfirmClear(false)
            setCart({ userId: id, items: [] })
            setLoadError(null)
            setLoaded(true)
          } catch (error) {
            const status = error?.response?.status
            // Already empty on server — treat as cleared
            if (status === 404) {
              setConfirmClear(false)
              setCart({ userId: id, items: [] })
              setLoadError(null)
              toast.success('Cart is already empty.')
            } else {
              toast.error(getErrorMessage(error))
            }
          } finally {
            setSaving(false)
          }
        }}
      />
    </div>
  )
}
