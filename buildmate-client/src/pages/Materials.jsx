import { useEffect, useMemo, useState } from 'react'
import {
  CircleDollarSign,
  Eye,
  Layers,
  LayoutGrid,
  List,
  Package,
  PackageMinus,
  Pencil,
  Plus,
  Search,
  Tag,
  Trash2,
} from 'lucide-react'
import EmptyState from '../components/common/EmptyState'
import Modal from '../components/common/Modal'
import ConfirmDialog from '../components/common/ConfirmDialog'
import StatusBadge from '../components/common/StatusBadge'
import { SkeletonCard } from '../components/common/Skeleton'
import { materialApi } from '../services/materialApi'
import { getErrorMessage } from '../services/api'
import { useToast } from '../components/common/Toast'
import EntitySelect from '../components/common/EntitySelect'
import DisplayId from '../components/common/DisplayId'
import { useSupplierOptions } from '../hooks/useEntityOptions'
import { formatMoney, stockStatus } from '../utils/format'
import { DISPLAY_ENTITY, ensureDisplayIds } from '../utils/displayId'
import '../styles/materials.css'

const emptyForm = {
  name: '',
  description: '',
  category: '',
  price: '',
  stock: '',
  unit: '',
  supplierId: '',
}

function MaterialsEmptyState({ onCreate, lowOnly, hasFilters }) {
  return (
    <div className="mat-empty">
      <div className="mat-empty-illustration" aria-hidden="true">
        <svg viewBox="0 0 200 160" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="24" y="48" width="152" height="96" rx="12" fill="var(--surface-muted)" stroke="var(--border)" strokeWidth="2" />
          <rect x="44" y="68" width="48" height="48" rx="8" fill="var(--accent-soft)" />
          <rect x="104" y="72" width="56" height="8" rx="4" fill="var(--border-strong)" />
          <rect x="104" y="88" width="40" height="6" rx="3" fill="var(--border)" />
          <rect x="104" y="100" width="32" height="6" rx="3" fill="var(--border)" />
          <circle cx="100" cy="32" r="20" fill="var(--accent-soft)" stroke="var(--accent)" strokeWidth="2" strokeDasharray="4 3" />
          <path d="M92 32h16M100 24v16" stroke="var(--accent)" strokeWidth="2.5" strokeLinecap="round" />
        </svg>
      </div>
      <EmptyState
        title={lowOnly ? 'No low-stock materials' : hasFilters ? 'No matching materials' : 'Your catalog is empty'}
        description={
          lowOnly
            ? 'All materials are above the low-stock threshold.'
            : hasFilters
              ? 'Try a different search term or category filter.'
              : 'Add your first construction material to get started.'
        }
        action={
          !lowOnly && !hasFilters ? (
            <button type="button" className="btn btn-primary" onClick={onCreate}>
              <Plus size={16} /> Add Material
            </button>
          ) : null
        }
      />
    </div>
  )
}

export default function Materials() {
  const toast = useToast()
  const [tab, setTab] = useState('materials')
  const [viewMode, setViewMode] = useState('grid')
  const [loading, setLoading] = useState(true)
  const [rows, setRows] = useState([])
  const [categories, setCategories] = useState([])
  const [brands, setBrands] = useState([])
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState('')
  const [lowOnly, setLowOnly] = useState(false)
  const [modal, setModal] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [formErrors, setFormErrors] = useState({})
  const [saving, setSaving] = useState(false)
  const [detail, setDetail] = useState(null)
  const [confirm, setConfirm] = useState(null)
  const [stockValue, setStockValue] = useState('')
  const [priceValue, setPriceValue] = useState('')
  const [metaForm, setMetaForm] = useState({ name: '', description: '' })

  const load = async () => {
    setLoading(true)
    try {
      const [materials, cats, brandList] = await Promise.all([
        lowOnly ? materialApi.lowStock() : materialApi.list(),
        materialApi.listCategories(),
        materialApi.listBrands(),
      ])
      setRows(ensureDisplayIds(DISPLAY_ENTITY.MATERIAL, materials || [], { sortField: 'createdAt' }))
      setCategories(cats || [])
      setBrands(brandList || [])
    } catch (error) {
      toast.error(getErrorMessage(error))
      setRows([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [lowOnly])

  const categoryOptions = useMemo(
    () => [...new Set(categories.map((c) => c.name).concat(rows.map((r) => r.category)).filter(Boolean))],
    [categories, rows],
  )

  const filtered = useMemo(() => {
    let list = rows
    if (category) list = list.filter((m) => m.category === category)
    if (keyword.trim()) {
      const q = keyword.trim().toLowerCase()
      list = list.filter((m) =>
        [m.name, m.description, m.category, m.supplierId].join(' ').toLowerCase().includes(q),
      )
    }
    return list
  }, [rows, category, keyword])

  const hasFilters = Boolean(category || keyword.trim())

  const openCreate = () => {
    setForm(emptyForm)
    setFormErrors({})
    setModal('create')
  }

  const openEdit = (item) => {
    setForm({
      name: item.name || '',
      description: item.description || '',
      category: item.category || '',
      price: item.price ?? '',
      stock: item.stock ?? '',
      unit: item.unit || '',
      supplierId: item.supplierId || '',
      id: item.id,
    })
    setFormErrors({})
    setModal('edit')
  }

  const validateMaterial = () => {
    const e = {}
    if (!form.name.trim()) e.name = 'Required'
    if (!form.description.trim()) e.description = 'Required'
    if (!form.category.trim()) e.category = 'Required'
    if (form.price === '' || Number(form.price) <= 0) e.price = 'Must be a positive number'
    if (form.stock === '' || Number(form.stock) < 0) e.stock = 'Must be 0 or greater'
    if (!form.unit.trim()) e.unit = 'Required'
    if (!form.supplierId.trim()) e.supplierId = 'Required'
    setFormErrors(e)
    return Object.keys(e).length === 0
  }

  const saveMaterial = async () => {
    if (!validateMaterial()) return
    setSaving(true)
    const body = {
      name: form.name.trim(),
      description: form.description.trim(),
      category: form.category.trim(),
      price: Number(form.price),
      stock: Number(form.stock),
      unit: form.unit.trim(),
      supplierId: form.supplierId.trim(),
    }
    try {
      if (modal === 'edit') {
        await materialApi.update(form.id, body)
        toast.success('Material updated successfully.')
      } else {
        await materialApi.create(body)
        toast.success('Material created successfully.')
      }
      setModal(null)
      await load()
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setSaving(false)
    }
  }

  const runSearch = async () => {
    if (!keyword.trim()) {
      await load()
      return
    }
    setLoading(true)
    try {
      const data = await materialApi.search(keyword.trim())
      setRows(ensureDisplayIds(DISPLAY_ENTITY.MATERIAL, data || [], { sortField: 'createdAt' }))
      setLowOnly(false)
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  const applyCategoryFilter = async (value) => {
    setCategory(value)
    if (!value) {
      await load()
      return
    }
    setLoading(true)
    try {
      const data = await materialApi.byCategory(value)
      setRows(ensureDisplayIds(DISPLAY_ENTITY.MATERIAL, data || [], { sortField: 'createdAt' }))
      setLowOnly(false)
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  const saveStock = async () => {
    if (stockValue === '' || Number(stockValue) < 0) {
      toast.error('Stock must be 0 or greater.')
      return
    }
    setSaving(true)
    try {
      await materialApi.updateStock(detail.id, Number(stockValue))
      toast.success('Stock updated successfully.')
      setModal(null)
      await load()
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setSaving(false)
    }
  }

  const savePrice = async () => {
    if (priceValue === '' || Number(priceValue) <= 0) {
      toast.error('Price must be positive.')
      return
    }
    setSaving(true)
    try {
      await materialApi.updatePrice(detail.id, Number(priceValue))
      toast.success('Price updated successfully.')
      setModal(null)
      await load()
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setSaving(false)
    }
  }

  const saveMeta = async () => {
    if (!metaForm.name.trim()) {
      toast.error('Name is required.')
      return
    }
    setSaving(true)
    try {
      const body = { name: metaForm.name.trim(), description: metaForm.description.trim() }
      if (modal === 'cat-edit') await materialApi.updateCategory(metaForm.id, body)
      else if (modal === 'cat-create') await materialApi.createCategory(body)
      else if (modal === 'brand-edit') await materialApi.updateBrand(metaForm.id, body)
      else await materialApi.createBrand(body)
      toast.success('Saved successfully.')
      setModal(null)
      await load()
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setSaving(false)
    }
  }

  if (loading && rows.length === 0 && tab === 'materials') {
    return (
      <div className="mat-loading">
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
      </div>
    )
  }

  return (
    <div className="mat">
      <section className="mat-hero">
        <div>
          <p className="mat-kicker"><Package size={14} /> Product catalog</p>
          <h2>Materials library</h2>
          <p>Browse, filter, and manage construction materials, categories, and brands.</p>
        </div>
        {tab === 'materials' && (
          <div className="mat-hero-actions">
            <button type="button" className={`btn ${lowOnly ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setLowOnly((v) => !v)}>
              {lowOnly ? 'Showing low stock' : 'Low stock view'}
            </button>
            <button type="button" className="btn btn-primary" onClick={openCreate}><Plus size={16} /> Add Material</button>
          </div>
        )}
      </section>

      <div className="tabs">
        <button type="button" className={`tab ${tab === 'materials' ? 'active' : ''}`} onClick={() => setTab('materials')}>Materials</button>
        <button type="button" className={`tab ${tab === 'categories' ? 'active' : ''}`} onClick={() => setTab('categories')}>Categories</button>
        <button type="button" className={`tab ${tab === 'brands' ? 'active' : ''}`} onClick={() => setTab('brands')}>Brands</button>
      </div>

      {tab === 'materials' && (
        <>
          <section className="mat-toolbar">
            <Search size={16} style={{ color: 'var(--text-muted)' }} />
            <input className="input input-sm" placeholder="Search materials…" value={keyword} onChange={(e) => setKeyword(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && runSearch()} />
            <button type="button" className="btn btn-secondary" onClick={runSearch}>Search</button>
            <div className="spacer" />
            <div className="mat-view-toggle" role="group" aria-label="View mode">
              <button
                type="button"
                className={`mat-view-btn ${viewMode === 'grid' ? 'active' : ''}`}
                title="Grid view"
                onClick={() => setViewMode('grid')}
              >
                <LayoutGrid size={16} />
              </button>
              <button
                type="button"
                className={`mat-view-btn ${viewMode === 'list' ? 'active' : ''}`}
                title="List view"
                onClick={() => setViewMode('list')}
              >
                <List size={16} />
              </button>
            </div>
          </section>

          {brands.length > 0 && (
            <section className="mat-brand-chips">
              <span className="mat-brand-label"><Tag size={13} /> Brands</span>
              {brands.map((b) => (
                <span key={b.id} className="mat-brand-chip">{b.name}</span>
              ))}
            </section>
          )}

          <section className="mat-categories">
            <button type="button" className={`mat-cat-chip ${!category ? 'active' : ''}`} onClick={() => applyCategoryFilter('')}>All</button>
            {categoryOptions.map((c) => (
              <button key={c} type="button" className={`mat-cat-chip ${category === c ? 'active' : ''}`} onClick={() => applyCategoryFilter(c)}>
                <Layers size={12} style={{ marginRight: 4, verticalAlign: -2 }} />{c}
              </button>
            ))}
          </section>

          {loading ? (
            <div className="mat-loading">
              <SkeletonCard />
              <SkeletonCard />
              <SkeletonCard />
            </div>
          ) : filtered.length === 0 ? (
            <MaterialsEmptyState onCreate={openCreate} lowOnly={lowOnly} hasFilters={hasFilters} />
          ) : (
            <section className={`mat-catalog ${viewMode === 'list' ? 'mat-catalog--list' : ''}`}>
              {filtered.map((m) => (
                <MaterialCard
                  key={m.id}
                  material={m}
                  viewMode={viewMode}
                  onView={() => { setDetail(m); setModal('view') }}
                  onEdit={() => openEdit(m)}
                  onStock={() => { setDetail(m); setStockValue(m.stock ?? ''); setModal('stock') }}
                  onPrice={() => { setDetail(m); setPriceValue(m.price ?? ''); setModal('price') }}
                  onDelete={() => setConfirm(m)}
                />
              ))}
            </section>
          )}
        </>
      )}

      {tab === 'categories' && (
        <MetaGrid
          title="Categories"
          rows={categories}
          icon={<Layers size={14} />}
          onAdd={() => { setMetaForm({ name: '', description: '' }); setModal('cat-create') }}
          onEdit={(row) => { setMetaForm({ id: row.id, name: row.name || '', description: row.description || '' }); setModal('cat-edit') }}
          onDelete={async (row) => {
            try {
              await materialApi.deleteCategory(row.id)
              toast.success('Category deleted.')
              await load()
            } catch (error) {
              toast.error(getErrorMessage(error))
            }
          }}
        />
      )}

      {tab === 'brands' && (
        <>
          {brands.length > 0 && (
            <section className="mat-brand-chips">
              {brands.map((b) => (
                <span key={b.id} className="mat-brand-chip"><Tag size={12} style={{ marginRight: 6, verticalAlign: -2 }} />{b.name}</span>
              ))}
            </section>
          )}
          <MetaGrid
            title="Brands"
            rows={brands}
            icon={<Tag size={14} />}
            onAdd={() => { setMetaForm({ name: '', description: '' }); setModal('brand-create') }}
            onEdit={(row) => { setMetaForm({ id: row.id, name: row.name || '', description: row.description || '' }); setModal('brand-edit') }}
            onDelete={async (row) => {
              try {
                await materialApi.deleteBrand(row.id)
                toast.success('Brand deleted.')
                await load()
              } catch (error) {
                toast.error(getErrorMessage(error))
              }
            }}
          />
        </>
      )}

      <Modal
        open={modal === 'create' || modal === 'edit'}
        title={modal === 'edit' ? 'Edit Material' : 'Add Material'}
        onClose={() => setModal(null)}
        footer={(
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="button" className="btn btn-primary" disabled={saving} onClick={saveMaterial}>
              {saving ? 'Saving…' : 'Save'}
            </button>
          </>
        )}
      >
        <MaterialForm form={form} errors={formErrors} onChange={setForm} />
      </Modal>

      <Modal open={modal === 'view'} title="Material Details" onClose={() => setModal(null)}>
        {detail && (
          <dl className="detail-grid">
            <div><dt>Name</dt><dd>{detail.name}</dd></div>
            <div><dt>Category</dt><dd>{detail.category}</dd></div>
            <div><dt>Price</dt><dd>{formatMoney(detail.price)}</dd></div>
            <div><dt>Stock</dt><dd>{detail.stock} {detail.unit}</dd></div>
            <div><dt>Supplier</dt><dd><DisplayId type={DISPLAY_ENTITY.SUPPLIER} id={detail.supplierId} /></dd></div>
            <div><dt>Description</dt><dd>{detail.description}</dd></div>
          </dl>
        )}
      </Modal>

      <Modal
        open={modal === 'stock'}
        title="Update Stock"
        onClose={() => setModal(null)}
        footer={(
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="button" className="btn btn-primary" disabled={saving} onClick={saveStock}>{saving ? 'Saving…' : 'Update'}</button>
          </>
        )}
      >
        <div className="field">
          <label>Stock <span className="req">*</span></label>
          <input className="input" type="number" min="0" value={stockValue} onChange={(e) => setStockValue(e.target.value)} />
        </div>
      </Modal>

      <Modal
        open={modal === 'price'}
        title="Update Price"
        onClose={() => setModal(null)}
        footer={(
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="button" className="btn btn-primary" disabled={saving} onClick={savePrice}>{saving ? 'Saving…' : 'Update'}</button>
          </>
        )}
      >
        <div className="field">
          <label>Price <span className="req">*</span></label>
          <input className="input" type="number" min="0.01" step="0.01" value={priceValue} onChange={(e) => setPriceValue(e.target.value)} />
        </div>
      </Modal>

      <Modal
        open={['cat-create', 'cat-edit', 'brand-create', 'brand-edit'].includes(modal)}
        title={modal?.includes('brand') ? 'Brand' : 'Category'}
        onClose={() => setModal(null)}
        footer={(
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="button" className="btn btn-primary" disabled={saving} onClick={saveMeta}>{saving ? 'Saving…' : 'Save'}</button>
          </>
        )}
      >
        <div className="form-grid">
          <div className="field full">
            <label>Name <span className="req">*</span></label>
            <input className="input" value={metaForm.name} onChange={(e) => setMetaForm({ ...metaForm, name: e.target.value })} />
          </div>
          <div className="field full">
            <label>Description</label>
            <textarea className="textarea" rows={3} value={metaForm.description} onChange={(e) => setMetaForm({ ...metaForm, description: e.target.value })} />
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        open={Boolean(confirm)}
        title="Delete material"
        message={`Delete “${confirm?.name}”? This cannot be undone.`}
        confirmLabel="Delete"
        danger
        loading={saving}
        onCancel={() => setConfirm(null)}
        onConfirm={async () => {
          setSaving(true)
          try {
            await materialApi.remove(confirm.id)
            toast.success('Material deleted successfully.')
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

function MaterialCard({ material: m, viewMode, onView, onEdit, onStock, onPrice, onDelete }) {
  const st = stockStatus(m.stock)
  const initials = String(m.name || '?').slice(0, 2).toUpperCase()
  const isLow = st.tone === 'warning' || st.tone === 'danger'

  return (
    <article className={`mat-product ${viewMode === 'list' ? 'mat-product--list' : ''}`}>
      <div className="mat-product-thumb">
        {initials}
        <span className="mat-price-badge">{formatMoney(m.price)}</span>
      </div>
      <div className="mat-product-body">
        <div className="mat-product-head">
          <h3>{m.name}</h3>
          <DisplayId type={DISPLAY_ENTITY.MATERIAL} id={m.id} className="mat-display-id" />
          {viewMode === 'list' && (
            <span className="mat-price-inline">{formatMoney(m.price)}</span>
          )}
        </div>
        <div className="mat-product-badges">
          <StatusBadge label={st.label} tone={st.tone} />
          <span className="mat-stock-badge">{m.stock} {m.unit}</span>
          {isLow && (
            <span className="mat-low-badge">
              <PackageMinus size={11} /> Low
            </span>
          )}
        </div>
        <div className="mat-product-meta">
          <span className="mat-cat-tag"><Layers size={11} /> {m.category}</span>
          {viewMode === 'list' && m.description && (
            <span className="mat-desc-snippet">{m.description}</span>
          )}
        </div>
      </div>
      <footer className="mat-product-foot">
        <button type="button" className="icon-action" title="View" onClick={onView}><Eye size={15} /></button>
        <button type="button" className="icon-action" title="Edit" onClick={onEdit}><Pencil size={15} /></button>
        <button type="button" className="icon-action" title="Update stock" onClick={onStock}><PackageMinus size={15} /></button>
        <button type="button" className="icon-action" title="Update price" onClick={onPrice}><CircleDollarSign size={15} /></button>
        <button type="button" className="icon-action danger" title="Delete" onClick={onDelete}><Trash2 size={15} /></button>
      </footer>
    </article>
  )
}

function MaterialForm({ form, errors, onChange }) {
  const set = (key, value) => onChange({ ...form, [key]: value })
  const { options: supplierOptions, loading: suppliersLoading, error: suppliersError } = useSupplierOptions()
  return (
    <div className="form-grid">
      <Field label="Name" error={errors.name} required>
        <input className="input" value={form.name} onChange={(e) => set('name', e.target.value)} placeholder="Cement 50kg" />
      </Field>
      <Field label="Category" error={errors.category} required>
        <input className="input" value={form.category} onChange={(e) => set('category', e.target.value)} placeholder="Cement" />
      </Field>
      <Field label="Unit" error={errors.unit} required>
        <input className="input" value={form.unit} onChange={(e) => set('unit', e.target.value)} placeholder="bag" />
      </Field>
      <Field label="Supplier ID" error={errors.supplierId || suppliersError} required>
        <EntitySelect
          value={form.supplierId}
          onChange={(id) => set('supplierId', id)}
          options={supplierOptions}
          loading={suppliersLoading}
          placeholder="Select Supplier ID"
          searchPlaceholder="Search supplier…"
          emptyMessage="No suppliers available"
        />
      </Field>
      <Field label="Price" error={errors.price} required>
        <input className="input" type="number" min="0.01" step="0.01" value={form.price} onChange={(e) => set('price', e.target.value)} />
      </Field>
      <Field label="Stock" error={errors.stock} required>
        <input className="input" type="number" min="0" value={form.stock} onChange={(e) => set('stock', e.target.value)} />
      </Field>
      <Field label="Description" error={errors.description} required className="full">
        <textarea className="textarea" rows={3} value={form.description} onChange={(e) => set('description', e.target.value)} />
      </Field>
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

function MetaGrid({ title, rows, icon, onAdd, onEdit, onDelete }) {
  return (
    <>
      <div className="mat-toolbar">
        <strong>{title}</strong>
        <div className="spacer" />
        <button type="button" className="btn btn-primary" onClick={onAdd}><Plus size={16} /> Add</button>
      </div>
      {rows.length === 0 ? (
        <div className="card"><EmptyState title={`No ${title.toLowerCase()} found.`} /></div>
      ) : (
        <section className="mat-meta-grid">
          {rows.map((row) => (
            <article key={row.id} className="mat-meta-card">
              <header>
                <strong>{icon} {row.name}</strong>
                <div className="actions-cell">
                  <button type="button" className="icon-action" onClick={() => onEdit(row)}><Pencil size={15} /></button>
                  <button type="button" className="icon-action danger" onClick={() => onDelete(row)}><Trash2 size={15} /></button>
                </div>
              </header>
              <p>{row.description || '—'}</p>
            </article>
          ))}
        </section>
      )}
    </>
  )
}
