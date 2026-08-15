/**
 * Stable short display IDs for UI only (localStorage registry).
 * Real Mongo / backend IDs are never changed — always use `id` for API calls.
 *
 * Format: PREFIX_001 (leading zeros, never renumbered on delete).
 */

export const DISPLAY_ENTITY = {
  SUPPLIER: 'supplier',
  MATERIAL: 'material',
  ORDER: 'order',
  PAYMENT: 'payment',
  INVOICE: 'invoice',
  USER: 'user',
  CART: 'cart',
  INVENTORY: 'inventory',
}

const PREFIX = {
  [DISPLAY_ENTITY.SUPPLIER]: 'S',
  [DISPLAY_ENTITY.MATERIAL]: 'M',
  [DISPLAY_ENTITY.ORDER]: 'O',
  [DISPLAY_ENTITY.PAYMENT]: 'P',
  [DISPLAY_ENTITY.INVOICE]: 'I',
  [DISPLAY_ENTITY.USER]: 'U',
  [DISPLAY_ENTITY.CART]: 'C',
  [DISPLAY_ENTITY.INVENTORY]: 'K',
}

const STORAGE_KEY = 'buildmate_display_ids_v1'

function emptyStore() {
  return { maps: {}, next: {} }
}

function loadStore() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return emptyStore()
    const parsed = JSON.parse(raw)
    if (!parsed || typeof parsed !== 'object') return emptyStore()
    return {
      maps: parsed.maps && typeof parsed.maps === 'object' ? parsed.maps : {},
      next: parsed.next && typeof parsed.next === 'object' ? parsed.next : {},
    }
  } catch {
    return emptyStore()
  }
}

function saveStore(store) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(store))
  } catch {
    // quota / private mode — still return in-memory assignment for this session
  }
}

export function formatDisplayCode(prefix, n) {
  const num = Number(n)
  if (!prefix || !Number.isFinite(num) || num < 1) return ''
  return `${prefix}_${String(Math.floor(num)).padStart(3, '0')}`
}

/**
 * Ensure a stable display id for a real backend id.
 * First time an id is seen it gets the next unused number for that entity type.
 */
export function ensureDisplayId(entityKey, realId) {
  if (realId == null || realId === '') return ''
  const id = String(realId)
  const prefix = PREFIX[entityKey]
  if (!prefix) return id

  const store = loadStore()
  if (!store.maps[entityKey]) store.maps[entityKey] = {}

  const existing = store.maps[entityKey][id]
  if (existing != null) {
    return formatDisplayCode(prefix, existing)
  }

  const next = Number(store.next[entityKey]) > 0 ? Number(store.next[entityKey]) : 1
  store.maps[entityKey][id] = next
  store.next[entityKey] = next + 1
  saveStore(store)
  return formatDisplayCode(prefix, next)
}

/** Read without allocating (returns '' if not yet registered). */
export function peekDisplayId(entityKey, realId) {
  if (realId == null || realId === '') return ''
  const id = String(realId)
  const prefix = PREFIX[entityKey]
  if (!prefix) return ''
  const store = loadStore()
  const n = store.maps?.[entityKey]?.[id]
  if (n == null) return ''
  return formatDisplayCode(prefix, n)
}

/**
 * Register many records stably. Prefer ObjectId / string sort so first discovery
 * order is deterministic across a fresh browser (not array index from API).
 */
export function ensureDisplayIds(entityKey, records, { idField = 'id', sortField } = {}) {
  const list = (Array.isArray(records) ? records : []).filter((r) => r?.[idField] != null && r[idField] !== '')
  list.sort((a, b) => {
    if (sortField) {
      const ta = new Date(a[sortField] || 0).getTime()
      const tb = new Date(b[sortField] || 0).getTime()
      if (ta !== tb) return ta - tb
    }
    return String(a[idField]).localeCompare(String(b[idField]))
  })
  return list.map((r) => {
    const displayId = ensureDisplayId(entityKey, r[idField])
    return { ...r, displayId }
  })
}

export function displayIdOrFallback(entityKey, realId) {
  return ensureDisplayId(entityKey, realId) || '—'
}

/**
 * Resolve a UI display code (e.g. I_001) or pass through a real backend id.
 * Returns '' when a known PREFIX_### form cannot be found in this browser's registry.
 */
export function resolveRealId(entityKey, value) {
  if (value == null || value === '') return ''
  const raw = String(value).trim()
  const prefix = PREFIX[entityKey]
  if (!prefix) return raw

  const match = raw.match(new RegExp(`^${prefix}[_-]?(\\d+)$`, 'i'))
  if (!match) return raw

  const n = Number(match[1])
  if (!Number.isFinite(n) || n < 1) return ''

  const store = loadStore()
  const map = store.maps?.[entityKey] || {}
  for (const [realId, assigned] of Object.entries(map)) {
    if (Number(assigned) === n) return realId
  }
  return ''
}
