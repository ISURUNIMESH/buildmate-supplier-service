import { useEffect, useMemo, useRef, useState } from 'react'
import { ChevronsUpDown, Loader2, Search, X } from 'lucide-react'
import './entitySelect.css'

/**
 * Searchable ID picker. Options: { value, label, description? }
 * value/onChange use the raw entity ID string the backend already expects.
 */
export default function EntitySelect({
  value = '',
  onChange,
  options = [],
  loading = false,
  error = '',
  disabled = false,
  placeholder = 'Select…',
  searchPlaceholder = 'Search…',
  emptyMessage = 'No IDs available',
  loadingMessage = 'Loading…',
  clearable = true,
}) {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')
  const rootRef = useRef(null)
  const inputRef = useRef(null)

  const selected = useMemo(
    () => options.find((o) => String(o.value) === String(value)) || null,
    [options, value],
  )

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    if (!q) return options
    return options.filter((o) => {
      const hay = [o.value, o.label, o.displayId, o.description, o.searchText]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
      return hay.includes(q)
    })
  }, [options, query])

  useEffect(() => {
    if (!open) return undefined
    const onDoc = (e) => {
      if (rootRef.current && !rootRef.current.contains(e.target)) {
        setOpen(false)
        setQuery('')
      }
    }
    document.addEventListener('mousedown', onDoc)
    return () => document.removeEventListener('mousedown', onDoc)
  }, [open])

  useEffect(() => {
    if (open) {
      setTimeout(() => inputRef.current?.focus(), 0)
    }
  }, [open])

  const toggle = () => {
    if (disabled || loading) return
    setOpen((v) => !v)
  }

  const pick = (opt) => {
    onChange?.(opt.value)
    setOpen(false)
    setQuery('')
  }

  const clear = (e) => {
    e.stopPropagation()
    onChange?.('')
    setQuery('')
  }

  let triggerLabel = placeholder
  if (loading && !selected) triggerLabel = loadingMessage
  else if (selected) triggerLabel = selected.label || selected.displayId || selected.value

  return (
    <div className={`entity-select ${disabled ? 'is-disabled' : ''} ${open ? 'is-open' : ''}`} ref={rootRef}>
      <button
        type="button"
        className={`entity-select-trigger ${!selected ? 'is-placeholder' : ''}`}
        onClick={toggle}
        disabled={disabled || loading}
        aria-haspopup="listbox"
        aria-expanded={open}
        title={selected ? String(selected.value) : undefined}
      >
        <span className="entity-select-value">
          {loading && !selected ? <Loader2 size={14} className="entity-select-spin" /> : null}
          <span className="entity-select-primary">{triggerLabel}</span>
          {selected?.description ? (
            <span className="entity-select-secondary">{selected.description}</span>
          ) : null}
        </span>
        <span className="entity-select-actions">
          {clearable && value && !disabled ? (
            <span
              role="button"
              tabIndex={-1}
              className="entity-select-clear"
              onClick={clear}
              onKeyDown={(e) => e.key === 'Enter' && clear(e)}
              aria-label="Clear selection"
            >
              <X size={14} />
            </span>
          ) : null}
          <ChevronsUpDown size={16} />
        </span>
      </button>

      {error ? <div className="entity-select-error">{error}</div> : null}

      {open ? (
        <div className="entity-select-panel" role="listbox">
          <div className="entity-select-search">
            <Search size={14} />
            <input
              ref={inputRef}
              className="entity-select-search-input"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder={searchPlaceholder}
              aria-label={searchPlaceholder}
            />
          </div>
          <div className="entity-select-list">
            {loading ? (
              <div className="entity-select-empty">{loadingMessage}</div>
            ) : filtered.length === 0 ? (
              <div className="entity-select-empty">{options.length === 0 ? emptyMessage : 'No matches'}</div>
            ) : (
              filtered.map((opt) => (
                <button
                  type="button"
                  key={String(opt.value)}
                  className={`entity-select-option ${String(opt.value) === String(value) ? 'is-active' : ''}`}
                  onClick={() => pick(opt)}
                  role="option"
                  aria-selected={String(opt.value) === String(value)}
                >
                  <span className="entity-select-option-label">{opt.label || opt.displayId || opt.value}</span>
                  {opt.description ? (
                    <span className="entity-select-option-desc">{opt.description}</span>
                  ) : null}
                  {opt.displayId ? (
                    <span className="entity-select-option-id" title={String(opt.value)}>
                      {opt.displayId}
                    </span>
                  ) : null}
                </button>
              ))
            )}
          </div>
        </div>
      ) : null}
    </div>
  )
}
