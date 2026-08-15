import { useCallback, useEffect, useMemo, useState } from 'react'
import { FileText, RefreshCw, Search } from 'lucide-react'
import EmptyState from '../components/common/EmptyState'
import { SkeletonCard } from '../components/common/Skeleton'
import StatusBadge from '../components/common/StatusBadge'
import EntitySelect from '../components/common/EntitySelect'
import UserIdField from '../components/common/UserIdField'
import DisplayId from '../components/common/DisplayId'
import { paymentApi } from '../services/paymentApi'
import { getErrorMessage } from '../services/api'
import { useToast } from '../components/common/Toast'
import { useAuth } from '../auth/AuthContext'
import { currentUserId, isAdminUser } from '../auth/roles'
import { formatDate, formatMoney } from '../utils/format'
import {
  DISPLAY_ENTITY,
  ensureDisplayId,
  ensureDisplayIds,
  resolveRealId,
} from '../utils/displayId'
import '../styles/invoices.css'

function invoiceStatusTone(status) {
  const s = String(status || 'GENERATED').toUpperCase()
  if (['PAID', 'SUCCESS', 'COMPLETED', 'ISSUED'].includes(s)) return 'success'
  if (['PENDING', 'DRAFT', 'GENERATED'].includes(s)) return 'warning'
  if (['CANCELLED', 'FAILED', 'VOID', 'REJECTED'].includes(s)) return 'danger'
  return 'info'
}

function asArray(data) {
  if (Array.isArray(data)) return data
  if (data && Array.isArray(data.content)) return data.content
  return []
}

const emptyDraft = {
  paymentId: '',
  orderId: '',
  userId: '',
  amount: null,
}

export default function Invoices() {
  const toast = useToast()
  const { user } = useAuth()
  const admin = isAdminUser(user)
  const selfId = currentUserId(user)

  const [userFilter, setUserFilter] = useState('')
  const [paymentOptions, setPaymentOptions] = useState([])
  const [paymentsLoading, setPaymentsLoading] = useState(true)
  const [paymentsError, setPaymentsError] = useState('')
  const [draft, setDraft] = useState(emptyDraft)
  const [saving, setSaving] = useState(false)
  const [lookupInput, setLookupInput] = useState('')
  const [lookupLoading, setLookupLoading] = useState(false)
  const [invoice, setInvoice] = useState(null)
  const [formMessage, setFormMessage] = useState('')

  const loadPayments = useCallback(async () => {
    setPaymentsLoading(true)
    setPaymentsError('')
    try {
      let data
      if (!admin) {
        if (!selfId) {
          setPaymentOptions([])
          setPaymentsError('Sign in to load your payments.')
          return
        }
        data = await paymentApi.history(selfId)
      } else if (userFilter.trim()) {
        data = await paymentApi.history(userFilter.trim())
      } else {
        data = await paymentApi.list()
      }

      const rows = ensureDisplayIds(DISPLAY_ENTITY.PAYMENT, asArray(data), { sortField: 'createdAt' })
      setPaymentOptions(
        rows.map((p) => {
          const orderDisplay = p.orderId ? ensureDisplayId(DISPLAY_ENTITY.ORDER, p.orderId) : null
          const userDisplay = p.userId ? ensureDisplayId(DISPLAY_ENTITY.USER, p.userId) : null
          return {
            value: p.id,
            displayId: p.displayId,
            label: `${p.displayId} — ${[
              p.amount != null ? formatMoney(p.amount, p.currency || 'LKR') : null,
              p.status,
            ]
              .filter(Boolean)
              .join(' · ') || 'Payment'}`,
            description: [orderDisplay, userDisplay].filter(Boolean).join(' · ') || undefined,
            searchText: [
              p.displayId,
              p.id,
              p.orderId,
              orderDisplay,
              p.userId,
              userDisplay,
              p.status,
            ]
              .filter(Boolean)
              .join(' '),
            raw: p,
          }
        }),
      )
    } catch (err) {
      setPaymentOptions([])
      setPaymentsError(getErrorMessage(err) || 'Unable to load payments.')
    } finally {
      setPaymentsLoading(false)
    }
  }, [admin, selfId, userFilter])

  useEffect(() => {
    loadPayments()
  }, [loadPayments])

  useEffect(() => {
    if (!admin && selfId) {
      setUserFilter(selfId)
    }
  }, [admin, selfId])

  const selectedPayment = useMemo(
    () => paymentOptions.find((p) => p.value === draft.paymentId)?.raw || null,
    [paymentOptions, draft.paymentId],
  )

  const canGenerate = Boolean(
    draft.paymentId
      && draft.orderId
      && draft.userId
      && draft.amount != null
      && Number(draft.amount) > 0
      && !saving,
  )

  const selectPayment = (paymentId) => {
    setFormMessage('')
    if (!paymentId) {
      setDraft(emptyDraft)
      return
    }
    const payment = paymentOptions.find((p) => p.value === paymentId)?.raw
    if (!payment) {
      setDraft(emptyDraft)
      setFormMessage('Unable to load payment.')
      return
    }
    if (!admin && selfId && String(payment.userId || '') !== String(selfId)) {
      setDraft(emptyDraft)
      setFormMessage('You can only generate invoices for your own payments.')
      toast.error('You can only generate invoices for your own payments.')
      return
    }
    if (!payment.orderId) {
      setDraft({
        paymentId: payment.id,
        orderId: '',
        userId: payment.userId || (!admin ? selfId : ''),
        amount: payment.amount != null ? Number(payment.amount) : null,
      })
      setFormMessage('Selected payment has no associated order.')
      return
    }
    setDraft({
      paymentId: payment.id,
      orderId: payment.orderId,
      userId: payment.userId || (!admin ? selfId : ''),
      amount: payment.amount != null ? Number(payment.amount) : null,
    })
  }

  const generate = async () => {
    setFormMessage('')
    if (!canGenerate) {
      toast.error('Select a valid payment before generating an invoice.')
      return
    }
    setSaving(true)
    try {
      const data = await paymentApi.createInvoice({
        paymentId: draft.paymentId,
        orderId: draft.orderId,
        userId: draft.userId,
        amount: Number(draft.amount),
      })
      ensureDisplayId(DISPLAY_ENTITY.INVOICE, data.id)
      setInvoice(data)
      setLookupInput(ensureDisplayId(DISPLAY_ENTITY.INVOICE, data.id) || data.id || '')
      setFormMessage('Invoice generated successfully.')
      toast.success('Invoice generated successfully.')
    } catch (error) {
      setFormMessage('Invoice could not be generated. Please try again.')
      toast.error(getErrorMessage(error) || 'Invoice could not be generated. Please try again.')
    } finally {
      setSaving(false)
    }
  }

  const lookup = async () => {
    const raw = lookupInput.trim()
    if (!raw) {
      toast.error('Enter an invoice ID.')
      return
    }

    const realId = resolveRealId(DISPLAY_ENTITY.INVOICE, raw)
    if (!realId) {
      toast.error('Unknown invoice display ID. Enter a registered I_### or the real invoice ID.')
      setInvoice(null)
      return
    }

    setLookupLoading(true)
    try {
      const data = await paymentApi.getInvoice(realId)
      if (
        !admin
        && selfId
        && data?.userId
        && String(data.userId) !== String(selfId)
      ) {
        setInvoice(null)
        toast.error('You do not have access to this invoice.')
        return
      }
      ensureDisplayId(DISPLAY_ENTITY.INVOICE, data.id)
      setInvoice(data)
      setLookupInput(ensureDisplayId(DISPLAY_ENTITY.INVOICE, data.id) || data.id)
      toast.success('Invoice loaded successfully.')
    } catch (error) {
      setInvoice(null)
      toast.error(getErrorMessage(error) || 'Invoice not found.')
    } finally {
      setLookupLoading(false)
    }
  }

  const refreshInvoice = async () => {
    if (!invoice?.id) return
    setLookupLoading(true)
    try {
      const data = await paymentApi.getInvoice(invoice.id)
      setInvoice(data)
      toast.success('Invoice refreshed.')
    } catch (error) {
      toast.error(getErrorMessage(error) || 'Unable to refresh invoice.')
    } finally {
      setLookupLoading(false)
    }
  }

  const paymentsEmptyMessage = paymentsLoading
    ? 'Loading payments...'
    : paymentsError || 'No payments available for invoice generation.'

  return (
    <div className="invc">
      <section className="invc-hero">
        <div>
          <p className="invc-kicker">
            <FileText size={14} style={{ verticalAlign: -2, marginRight: 4 }} />
            Billing
          </p>
          <h2>Invoice</h2>
          <p>Generate and look up invoices from payments.</p>
        </div>
      </section>

      <section className="invc-panel">
        <header className="invc-panel-head">
          <h3>Generate Invoice</h3>
        </header>
        <div className="invc-panel-body">
          {admin ? (
            <div className="field">
              <label>Filter payments by user</label>
              <UserIdField
                value={userFilter}
                onChange={(next) => {
                  setUserFilter(next)
                  setDraft(emptyDraft)
                  setFormMessage('')
                }}
                autoAssign={false}
                placeholder="All users (optional)"
                emptyMessage="No users available"
              />
            </div>
          ) : null}

          <div className="field">
            <label>
              Payment <span className="req">*</span>
            </label>
            <EntitySelect
              value={draft.paymentId}
              onChange={selectPayment}
              options={paymentOptions}
              loading={paymentsLoading}
              error={paymentsError}
              placeholder={paymentsLoading ? 'Loading payments...' : 'Select Payment'}
              searchPlaceholder="Search payment…"
              emptyMessage={paymentsEmptyMessage}
              disabled={paymentsLoading}
            />
          </div>

          <div className="invc-autofill-grid">
            <div className="field">
              <label>Order</label>
              <div
                className={`invc-readonly ${!draft.orderId ? 'is-empty' : ''}`}
                title={draft.orderId || undefined}
              >
                {draft.orderId ? (
                  <DisplayId type={DISPLAY_ENTITY.ORDER} id={draft.orderId} />
                ) : (
                  <span>—</span>
                )}
              </div>
            </div>
            <div className="field">
              <label>User</label>
              <div
                className={`invc-readonly ${!draft.userId ? 'is-empty' : ''}`}
                title={draft.userId || undefined}
              >
                {draft.userId ? (
                  <DisplayId type={DISPLAY_ENTITY.USER} id={draft.userId} />
                ) : (
                  <span>—</span>
                )}
              </div>
            </div>
            <div className="field invc-amount-field">
              <label>Amount</label>
              <div className={`invc-readonly invc-amount ${draft.amount == null ? 'is-empty' : ''}`}>
                {draft.amount != null
                  ? formatMoney(draft.amount, selectedPayment?.currency || 'LKR')
                  : '—'}
              </div>
            </div>
          </div>

          {formMessage ? (
            <p
              className={`invc-message ${
                formMessage.includes('successfully') ? 'is-success' : 'is-error'
              }`}
            >
              {formMessage}
            </p>
          ) : null}

          <div className="invc-panel-actions">
            <button
              type="button"
              className="btn btn-primary"
              disabled={!canGenerate}
              onClick={generate}
            >
              {saving ? 'Generating…' : 'Generate Invoice'}
            </button>
          </div>
        </div>
      </section>

      <section className="invc-panel">
        <header className="invc-panel-head">
          <h3>Find Invoice</h3>
        </header>
        <div className="invc-panel-body">
          <div className="invc-lookup">
            <div className="field invc-lookup-field">
              <label htmlFor="invoice-lookup">Invoice ID</label>
              <div className="invc-lookup-row">
                <Search size={16} className="invc-search-icon" aria-hidden />
                <input
                  id="invoice-lookup"
                  className="input"
                  value={lookupInput}
                  onChange={(e) => setLookupInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault()
                      lookup()
                    }
                  }}
                  placeholder="I_001 or invoice ID"
                  disabled={lookupLoading}
                />
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={lookup}
                  disabled={lookupLoading || !lookupInput.trim()}
                >
                  {lookupLoading ? 'Searching…' : 'Search'}
                </button>
              </div>
            </div>
          </div>

          {lookupLoading && !invoice ? (
            <div className="invc-skeleton">
              <SkeletonCard />
            </div>
          ) : !invoice ? (
            <EmptyState
              title="No invoice loaded."
              description="Generate an invoice or search by invoice ID."
            />
          ) : (
            <article className="invc-summary">
              <header className="invc-summary-head">
                <div>
                  <h4>Invoice details</h4>
                  <p>Loaded from payment service</p>
                </div>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={refreshInvoice}
                  disabled={lookupLoading}
                >
                  <RefreshCw size={14} />
                  Refresh
                </button>
              </header>
              <dl className="invc-summary-grid">
                <div>
                  <dt>Invoice</dt>
                  <dd>
                    <DisplayId type={DISPLAY_ENTITY.INVOICE} id={invoice.id} />
                  </dd>
                </div>
                <div>
                  <dt>Payment</dt>
                  <dd>
                    <DisplayId type={DISPLAY_ENTITY.PAYMENT} id={invoice.paymentId} />
                  </dd>
                </div>
                <div>
                  <dt>Order</dt>
                  <dd>
                    <DisplayId type={DISPLAY_ENTITY.ORDER} id={invoice.orderId} />
                  </dd>
                </div>
                <div>
                  <dt>User</dt>
                  <dd>
                    <DisplayId type={DISPLAY_ENTITY.USER} id={invoice.userId} />
                  </dd>
                </div>
                <div>
                  <dt>Amount</dt>
                  <dd>{formatMoney(invoice.amount)}</dd>
                </div>
                <div>
                  <dt>Status</dt>
                  <dd>
                    <StatusBadge
                      label={invoice.status || 'GENERATED'}
                      tone={invoiceStatusTone(invoice.status)}
                    />
                  </dd>
                </div>
                <div>
                  <dt>Created</dt>
                  <dd>{formatDate(invoice.createdAt)}</dd>
                </div>
              </dl>
            </article>
          )}
        </div>
      </section>
    </div>
  )
}
