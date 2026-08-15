import { useEffect, useMemo, useState } from 'react'
import {
  Building2,
  Eye,
  FileText,
  LayoutGrid,
  List,
  Mail,
  MapPin,
  Pencil,
  Phone,
  Plus,
  Search,
  ShieldCheck,
  Star,
  Trash2,
  Upload,
} from 'lucide-react'
import EmptyState from '../components/common/EmptyState'
import Modal from '../components/common/Modal'
import SideDrawer from '../components/common/SideDrawer'
import ConfirmDialog from '../components/common/ConfirmDialog'
import StatusBadge from '../components/common/StatusBadge'
import Skeleton, { SkeletonCard } from '../components/common/Skeleton'
import { supplierApi } from '../services/supplierApi'
import { getErrorMessage } from '../services/api'
import { useToast } from '../components/common/Toast'
import { formatDate, SUPPLIER_STATUSES } from '../utils/format'
import { DISPLAY_ENTITY, ensureDisplayId, ensureDisplayIds } from '../utils/displayId'
import DisplayId from '../components/common/DisplayId'
import '../styles/suppliers.css'

const emptyForm = {
  supplierCode: '',
  companyName: '',
  ownerName: '',
  email: '',
  password: '',
  phone: '',
  address: '',
  district: '',
  businessRegistrationNo: '',
}

const STATUS_FILTERS = ['ALL', ...SUPPLIER_STATUSES]

export default function Suppliers() {
  const toast = useToast()
  const [loading, setLoading] = useState(true)
  const [rows, setRows] = useState([])
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [viewMode, setViewMode] = useState('cards')
  const [topOnly, setTopOnly] = useState(false)
  const [modal, setModal] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [errors, setErrors] = useState({})
  const [saving, setSaving] = useState(false)
  const [detail, setDetail] = useState(null)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [docs, setDocs] = useState([])
  const [docsLoading, setDocsLoading] = useState(false)
  const [confirm, setConfirm] = useState(null)
  const [docForm, setDocForm] = useState({ documentName: '', documentType: '', filePath: '' })
  const [statusValue, setStatusValue] = useState('APPROVED')
  const [ratingValue, setRatingValue] = useState(4.5)

  const load = async (preferredTop = topOnly) => {
    setLoading(true)
    try {
      const data = preferredTop ? await supplierApi.topRated() : await supplierApi.list()
      setRows(ensureDisplayIds(DISPLAY_ENTITY.SUPPLIER, data || [], { sortField: 'createdAt' }))
    } catch (error) {
      toast.error(getErrorMessage(error))
      setRows([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [topOnly])

  const filtered = useMemo(() => {
    let list = rows
    if (statusFilter !== 'ALL') {
      list = list.filter((s) => s.status === statusFilter)
    }
    const q = query.trim().toLowerCase()
    if (q) {
      list = list.filter((s) =>
        [s.companyName, s.email, s.district, s.supplierCode, s.status, s.phone, s.ownerName]
          .join(' ')
          .toLowerCase()
          .includes(q),
      )
    }
    return list
  }, [rows, query, statusFilter])

  const stats = useMemo(() => ({
    total: rows.length,
    approved: rows.filter((s) => s.status === 'APPROVED').length,
    pending: rows.filter((s) => s.status === 'PENDING').length,
    avgRating: rows.length
      ? (rows.reduce((sum, s) => sum + Number(s.rating ?? 0), 0) / rows.length).toFixed(1)
      : '0.0',
  }), [rows])

  const validate = (isCreate) => {
    const e = {}
    const required = isCreate
      ? ['supplierCode', 'companyName', 'ownerName', 'email', 'password', 'phone', 'address', 'district', 'businessRegistrationNo']
      : ['companyName', 'ownerName', 'phone', 'address', 'district']
    required.forEach((key) => {
      if (!String(form[key] || '').trim()) e[key] = 'Required'
    })
    if (isCreate && form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Invalid email'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const save = async () => {
    const isCreate = modal === 'create'
    if (!validate(isCreate)) return
    setSaving(true)
    try {
      if (isCreate) {
        await supplierApi.create({ ...form })
        toast.success('Supplier created successfully.')
      } else {
        await supplierApi.update(form.id, {
          companyName: form.companyName,
          ownerName: form.ownerName,
          phone: form.phone,
          address: form.address,
          district: form.district,
        })
        toast.success('Supplier updated successfully.')
      }
      setModal(null)
      await load()
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setSaving(false)
    }
  }

  const loadDocuments = async (supplierId) => {
    setDocsLoading(true)
    try {
      const data = await supplierApi.listDocuments(supplierId)
      setDocs(data || [])
    } catch (error) {
      toast.error(getErrorMessage(error))
      setDocs([])
    } finally {
      setDocsLoading(false)
    }
  }

  const openDetail = async (supplier) => {
    setDetail(supplier)
    setDrawerOpen(true)
    setDocForm({ documentName: '', documentType: '', filePath: '' })
    setDocs([])
    await loadDocuments(supplier.id)
  }

  const closeDrawer = () => {
    setDrawerOpen(false)
    setDetail(null)
    setDocs([])
  }

  const uploadDoc = async () => {
    if (!docForm.documentName.trim() || !docForm.documentType.trim() || !docForm.filePath.trim()) {
      toast.error('Document name, type and file path are required.')
      return
    }
    setSaving(true)
    try {
      await supplierApi.uploadDocument(detail.id, {
        supplierId: detail.id,
        documentName: docForm.documentName.trim(),
        documentType: docForm.documentType.trim(),
        filePath: docForm.filePath.trim(),
      })
      toast.success('Document uploaded successfully.')
      const data = await supplierApi.listDocuments(detail.id)
      setDocs(data || [])
      setDocForm({ documentName: '', documentType: '', filePath: '' })
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setSaving(false)
    }
  }

  const openEdit = (supplier) => {
    setForm({ ...emptyForm, ...supplier, password: '' })
    setErrors({})
    setModal('edit')
  }

  const openStatus = (supplier) => {
    setDetail(supplier)
    setStatusValue(supplier.status || 'PENDING')
    setModal('status')
  }

  const openRating = (supplier) => {
    setDetail(supplier)
    setRatingValue(Number(supplier.rating ?? 0))
    setModal('rating')
  }

  const renderActions = (supplier, compact = false) => (
    <div className={`sup-actions ${compact ? 'sup-actions-compact' : ''}`}>
      <button type="button" className="icon-action" title="View details" onClick={() => openDetail(supplier)}>
        <Eye size={15} />
      </button>
      <button type="button" className="icon-action" title="Edit" onClick={() => openEdit(supplier)}>
        <Pencil size={15} />
      </button>
      <button type="button" className="icon-action" title="Update status" onClick={() => openStatus(supplier)}>
        <ShieldCheck size={15} />
      </button>
      <button type="button" className="icon-action" title="Update rating" onClick={() => openRating(supplier)}>
        <Star size={15} />
      </button>
      <button type="button" className="icon-action" title="Documents" onClick={() => openDetail(supplier)}>
        <Upload size={15} />
      </button>
      <button type="button" className="icon-action danger" title="Delete" onClick={() => setConfirm(supplier)}>
        <Trash2 size={15} />
      </button>
    </div>
  )

  const emptyTitle = rows.length === 0
    ? (topOnly ? 'No top-rated suppliers yet. Set ratings first, then try again.' : 'No suppliers yet.')
    : 'No suppliers match your filters.'

  if (loading && rows.length === 0) {
    return (
      <div className="sup">
        <section className="sup-hero sup-hero-skeleton">
          <Skeleton width="40%" height={28} />
          <Skeleton width="60%" height={14} style={{ marginTop: 10 }} />
        </section>
        <div className="sup-loading">
          <SkeletonCard />
          <SkeletonCard />
          <SkeletonCard />
        </div>
      </div>
    )
  }

  return (
    <div className="sup">
      <section className="sup-hero">
        <div>
          <p className="sup-kicker"><Building2 size={14} /> Partner network</p>
          <h2>Supplier directory</h2>
          <p>Manage company profiles, verification status, ratings, and compliance documents.</p>
        </div>
        <div className="sup-hero-actions">
          <button
            type="button"
            className={`btn ${topOnly ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setTopOnly((v) => !v)}
          >
            <Star size={15} /> Top rated
          </button>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => { setForm(emptyForm); setErrors({}); setModal('create') }}
          >
            <Plus size={16} /> Add Supplier
          </button>
        </div>
      </section>

      <section className="sup-stats">
        <div className="sup-stat"><span>Total partners</span><strong>{stats.total}</strong></div>
        <div className="sup-stat"><span>Approved</span><strong>{stats.approved}</strong></div>
        <div className="sup-stat"><span>Pending review</span><strong>{stats.pending}</strong></div>
        <div className="sup-stat"><span>Avg. rating</span><strong>{stats.avgRating}</strong></div>
      </section>

      <section className="sup-toolbar">
        <div className="sup-search">
          <Search size={16} aria-hidden="true" />
          <input
            className="input input-sm"
            placeholder="Search by company, email, district, code…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            aria-label="Search suppliers"
          />
        </div>

        <div className="sup-status-filters" role="group" aria-label="Filter by status">
          {STATUS_FILTERS.map((status) => (
            <button
              key={status}
              type="button"
              className={`sup-status-chip ${statusFilter === status ? 'active' : ''}`}
              onClick={() => setStatusFilter(status)}
            >
              {status === 'ALL' ? 'All' : status.charAt(0) + status.slice(1).toLowerCase()}
            </button>
          ))}
        </div>

        <div className="sup-view-toggle" role="group" aria-label="View mode">
          <button
            type="button"
            className={`sup-view-btn ${viewMode === 'cards' ? 'active' : ''}`}
            onClick={() => setViewMode('cards')}
            aria-label="Card view"
            aria-pressed={viewMode === 'cards'}
          >
            <LayoutGrid size={16} />
          </button>
          <button
            type="button"
            className={`sup-view-btn ${viewMode === 'table' ? 'active' : ''}`}
            onClick={() => setViewMode('table')}
            aria-label="Table view"
            aria-pressed={viewMode === 'table'}
          >
            <List size={16} />
          </button>
        </div>
      </section>

      {loading ? (
        viewMode === 'cards' ? (
          <div className="sup-loading">
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
          </div>
        ) : (
          <div className="table-wrap card sup-table-skeleton">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="sup-table-skeleton-row">
                <Skeleton width={36} height={36} radius={10} />
                <Skeleton width="22%" height={14} />
                <Skeleton width="14%" height={14} />
                <Skeleton width="18%" height={14} />
                <Skeleton width="12%" height={14} />
                <Skeleton width="10%" height={14} />
                <Skeleton width={80} height={14} />
              </div>
            ))}
          </div>
        )
      ) : filtered.length === 0 ? (
        <div className="card sup-empty">
          <EmptyState title={emptyTitle} />
        </div>
      ) : viewMode === 'cards' ? (
        <section className="sup-grid">
          {filtered.map((s) => (
            <article key={s.id} className={`sup-card ${String(s.status || '').toLowerCase()}`}>
              <header className="sup-card-head">
                <div className="sup-card-identity">
                  <div className="sup-avatar" aria-hidden="true">{getInitials(s.companyName)}</div>
                  <div>
                    <strong>{s.companyName}</strong>
                    <div className="sup-card-meta">
                      <DisplayId type={DISPLAY_ENTITY.SUPPLIER} id={s.id} />
                      {s.supplierCode ? <span> · {s.supplierCode}</span> : null}
                    </div>
                  </div>
                </div>
                <StatusBadge label={s.status} tone={statusTone(s.status)} />
              </header>

              <div className="sup-card-contact">
                <span><Mail size={13} aria-hidden="true" /> {s.email}</span>
                <span><Phone size={13} aria-hidden="true" /> {s.phone}</span>
                <span><MapPin size={13} aria-hidden="true" /> {s.district}</span>
              </div>

              <dl className="sup-card-body">
                <div><dt>Owner</dt><dd>{s.ownerName}</dd></div>
                <div><dt>BRN</dt><dd>{s.businessRegistrationNo}</dd></div>
              </dl>

              <div className="sup-card-performance">
                <span className="sup-card-performance-label">Performance</span>
                <RatingMeter value={s.rating ?? 0} />
              </div>

              <footer className="sup-card-foot">
                {renderActions(s)}
              </footer>
            </article>
          ))}
        </section>
      ) : (
        <div className="table-wrap card sup-table-wrap">
          <table className="data-table sup-table">
            <thead>
              <tr>
                <th>Company</th>
                <th>ID</th>
                <th>Code</th>
                <th>Contact</th>
                <th>District</th>
                <th>Rating</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((s) => (
                <tr key={s.id} className="sup-table-row" onClick={() => openDetail(s)}>
                  <td>
                    <div className="sup-table-company">
                      <div className="sup-avatar sup-avatar-sm" aria-hidden="true">{getInitials(s.companyName)}</div>
                      <div>
                        <strong>{s.companyName}</strong>
                        <span>{s.ownerName}</span>
                      </div>
                    </div>
                  </td>
                  <td><DisplayId type={DISPLAY_ENTITY.SUPPLIER} id={s.id} /></td>
                  <td>{s.supplierCode}</td>
                  <td>
                    <div className="sup-table-contact">
                      <span>{s.email}</span>
                      <span>{s.phone}</span>
                    </div>
                  </td>
                  <td>{s.district}</td>
                  <td><RatingMeter value={s.rating ?? 0} compact /></td>
                  <td><StatusBadge label={s.status} tone={statusTone(s.status)} /></td>
                  <td onClick={(e) => e.stopPropagation()}>
                    <div className="actions-cell">{renderActions(s, true)}</div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <SideDrawer
        open={drawerOpen}
        title={detail?.companyName || 'Supplier details'}
        subtitle={detail ? `${ensureDisplayId(DISPLAY_ENTITY.SUPPLIER, detail.id)} · ${detail.supplierCode} · ${detail.district}` : ''}
        wide
        onClose={closeDrawer}
        footer={detail ? (
          <>
            <button type="button" className="btn btn-secondary" onClick={closeDrawer}>Close</button>
            <button type="button" className="btn btn-secondary" onClick={() => { closeDrawer(); openEdit(detail) }}>
              <Pencil size={14} /> Edit
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => { closeDrawer(); openRating(detail) }}>
              <Star size={14} /> Rating
            </button>
            <button type="button" className="btn btn-primary" onClick={() => { closeDrawer(); openStatus(detail) }}>
              <ShieldCheck size={14} /> Update status
            </button>
          </>
        ) : null}
      >
        {detail && (
          <div className="sup-drawer-content">
            <section className="sup-drawer-profile">
              <div className="sup-avatar sup-avatar-lg" aria-hidden="true">{getInitials(detail.companyName)}</div>
              <div className="sup-drawer-profile-meta">
                <StatusBadge label={detail.status} tone={statusTone(detail.status)} />
                <RatingMeter value={detail.rating ?? 0} />
              </div>
            </section>

            <section className="sup-drawer-section">
              <h4 className="sup-drawer-section-title">Contact</h4>
              <ul className="sup-drawer-contact-list">
                <li><Mail size={15} aria-hidden="true" /><span>{detail.email}</span></li>
                <li><Phone size={15} aria-hidden="true" /><span>{detail.phone}</span></li>
                <li><MapPin size={15} aria-hidden="true" /><span>{detail.address}, {detail.district}</span></li>
              </ul>
            </section>

            <section className="sup-drawer-section">
              <h4 className="sup-drawer-section-title">Company details</h4>
              <dl className="detail-grid sup-drawer-details">
                <div><dt>Owner</dt><dd>{detail.ownerName}</dd></div>
                <div><dt>ID</dt><dd><DisplayId type={DISPLAY_ENTITY.SUPPLIER} id={detail.id} /></dd></div>
                <div><dt>Code</dt><dd>{detail.supplierCode}</dd></div>
                <div><dt>BRN</dt><dd>{detail.businessRegistrationNo}</dd></div>
                <div><dt>Created</dt><dd>{formatDate(detail.createdAt)}</dd></div>
                <div><dt>Updated</dt><dd>{formatDate(detail.updatedAt)}</dd></div>
              </dl>
            </section>

            <section className="sup-drawer-section sup-drawer-docs">
              <h4 className="sup-drawer-section-title"><FileText size={16} aria-hidden="true" /> Documents</h4>

              <div className="form-grid sup-doc-form">
                <div className="field">
                  <label>Document name <span className="req">*</span></label>
                  <input
                    className="input"
                    value={docForm.documentName}
                    onChange={(e) => setDocForm({ ...docForm, documentName: e.target.value })}
                  />
                </div>
                <div className="field">
                  <label>Document type <span className="req">*</span></label>
                  <input
                    className="input"
                    value={docForm.documentType}
                    onChange={(e) => setDocForm({ ...docForm, documentType: e.target.value })}
                    placeholder="LICENSE"
                  />
                </div>
                <div className="field full">
                  <label>File path / URL <span className="req">*</span></label>
                  <input
                    className="input"
                    value={docForm.filePath}
                    onChange={(e) => setDocForm({ ...docForm, filePath: e.target.value })}
                    placeholder="/files/license.pdf"
                  />
                </div>
              </div>

              <button type="button" className="btn btn-primary btn-sm" disabled={saving} onClick={uploadDoc}>
                {saving ? 'Uploading…' : 'Upload document'}
              </button>

              <div className="sup-docs-list">
                {docsLoading ? (
                  <div className="sup-docs-loading">
                    <Skeleton width="100%" height={52} radius={12} />
                    <Skeleton width="100%" height={52} radius={12} />
                    <Skeleton width="72%" height={52} radius={12} />
                  </div>
                ) : docs.length === 0 ? (
                  <EmptyState title="No documents found." />
                ) : (
                  docs.map((d) => (
                    <div key={d.id} className="sup-doc">
                      <div className="sup-doc-icon"><FileText size={18} /></div>
                      <div className="sup-doc-info">
                        <strong>{d.documentName}</strong>
                        <span>{d.documentType} · {formatDate(d.uploadedAt)}</span>
                        {/^https?:\/\//i.test(d.filePath)
                          ? <a href={d.filePath} target="_blank" rel="noreferrer">{d.filePath}</a>
                          : <span>{d.filePath}</span>}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </section>
          </div>
        )}
      </SideDrawer>

      <Modal
        open={modal === 'create' || modal === 'edit'}
        title={modal === 'edit' ? 'Edit Supplier' : 'Add Supplier'}
        wide
        onClose={() => setModal(null)}
        footer={(
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="button" className="btn btn-primary" disabled={saving} onClick={save}>{saving ? 'Saving…' : 'Save'}</button>
          </>
        )}
      >
        <SupplierForm form={form} errors={errors} onChange={setForm} isCreate={modal === 'create'} />
        {modal === 'edit' && (
          <p className="muted" style={{ marginTop: '0.75rem' }}>
            Update only changes company name, owner, phone, address and district (backend rule).
          </p>
        )}
      </Modal>

      <Modal
        open={modal === 'status'}
        title="Update Status"
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
                  await supplierApi.updateStatus(detail.id, statusValue)
                  toast.success('Supplier status updated successfully.')
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
          <label>Status <span className="req">*</span></label>
          <select className="select" value={statusValue} onChange={(e) => setStatusValue(e.target.value)}>
            {SUPPLIER_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
      </Modal>

      <Modal
        open={modal === 'rating'}
        title="Update Rating"
        onClose={() => setModal(null)}
        footer={(
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button
              type="button"
              className="btn btn-primary"
              disabled={saving}
              onClick={async () => {
                const rating = Number(ratingValue)
                if (Number.isNaN(rating) || rating < 0 || rating > 5) {
                  toast.error('Rating must be between 0 and 5.')
                  return
                }
                setSaving(true)
                try {
                  await supplierApi.updateRating(detail.id, rating)
                  toast.success('Supplier rating updated successfully.')
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
          <label>Rating (0 – 5) <span className="req">*</span></label>
          <input
            className="input"
            type="number"
            min="0"
            max="5"
            step="0.1"
            value={ratingValue}
            onChange={(e) => setRatingValue(e.target.value)}
          />
          <p className="muted" style={{ marginTop: '0.5rem' }}>
            Top rated shows the 10 highest-rated suppliers.
          </p>
        </div>
      </Modal>

      <ConfirmDialog
        open={Boolean(confirm)}
        title="Delete supplier"
        message={`Delete “${confirm?.companyName}”?`}
        confirmLabel="Delete"
        danger
        loading={saving}
        onCancel={() => setConfirm(null)}
        onConfirm={async () => {
          setSaving(true)
          try {
            await supplierApi.remove(confirm.id)
            toast.success('Supplier deleted successfully.')
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

function SupplierForm({ form, errors, onChange, isCreate }) {
  const set = (key, value) => onChange({ ...form, [key]: value })
  return (
    <div className="form-grid">
      <Field label="Supplier code" required error={errors.supplierCode}><input className="input" value={form.supplierCode} onChange={(e) => set('supplierCode', e.target.value)} disabled={!isCreate} /></Field>
      <Field label="Company name" required error={errors.companyName}><input className="input" value={form.companyName} onChange={(e) => set('companyName', e.target.value)} /></Field>
      <Field label="Owner name" required error={errors.ownerName}><input className="input" value={form.ownerName} onChange={(e) => set('ownerName', e.target.value)} /></Field>
      <Field label="Email" required error={errors.email}><input className="input" type="email" value={form.email} onChange={(e) => set('email', e.target.value)} disabled={!isCreate} /></Field>
      {isCreate && <Field label="Password" required error={errors.password}><input className="input" type="password" value={form.password} onChange={(e) => set('password', e.target.value)} /></Field>}
      <Field label="Phone" required error={errors.phone}><input className="input" value={form.phone} onChange={(e) => set('phone', e.target.value)} /></Field>
      <Field label="District" required error={errors.district}><input className="input" value={form.district} onChange={(e) => set('district', e.target.value)} /></Field>
      <Field label="Business registration no" required error={errors.businessRegistrationNo}><input className="input" value={form.businessRegistrationNo} onChange={(e) => set('businessRegistrationNo', e.target.value)} disabled={!isCreate} /></Field>
      <Field label="Address" required error={errors.address} className="full"><textarea className="textarea" rows={2} value={form.address} onChange={(e) => set('address', e.target.value)} /></Field>
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

function RatingMeter({ value = 0, compact = false }) {
  const numeric = Math.min(5, Math.max(0, Number(value) || 0))
  const pct = (numeric / 5) * 100
  return (
    <div className={`sup-rating-meter ${compact ? 'compact' : ''}`}>
      <div className="sup-rating-meter-track" aria-hidden="true">
        <div className="sup-rating-meter-fill" style={{ width: `${pct}%` }} />
      </div>
      <span className="sup-rating-meter-value">
        <Star size={compact ? 12 : 14} fill="currentColor" aria-hidden="true" />
        {numeric.toFixed(1)}
      </span>
    </div>
  )
}

function getInitials(name) {
  if (!name) return '?'
  const parts = name.trim().split(/\s+/).filter(Boolean)
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}

function statusTone(status) {
  if (status === 'APPROVED') return 'success'
  if (status === 'PENDING') return 'warning'
  if (status === 'REJECTED') return 'danger'
  return 'neutral'
}
