import { useCallback, useEffect, useMemo, useState } from 'react'
import { supplierApi } from '../services/supplierApi'
import { materialApi } from '../services/materialApi'
import { orderApi } from '../services/orderApi'
import { paymentApi } from '../services/paymentApi'
import { authApi } from '../services/authApi'
import { useAuth } from '../auth/AuthContext'
import { isAdminUser } from '../auth/roles'
import { getErrorMessage } from '../services/api'
import { DISPLAY_ENTITY, ensureDisplayId, ensureDisplayIds } from '../utils/displayId'

function asArray(data) {
  if (Array.isArray(data)) return data
  if (data && Array.isArray(data.content)) return data.content
  return []
}

function withDisplay(entityKey, rows, sortField) {
  return ensureDisplayIds(entityKey, rows, { sortField }).map((row) => {
    const displayId = row.displayId || ensureDisplayId(entityKey, row.id)
    return { ...row, displayId }
  })
}

export function useSupplierOptions() {
  const [options, setOptions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const reload = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const rows = withDisplay(DISPLAY_ENTITY.SUPPLIER, asArray(await supplierApi.list()), 'createdAt')
      setOptions(
        rows.map((s) => ({
          value: s.id,
          displayId: s.displayId,
          label: `${s.displayId} — ${s.companyName || s.supplierCode || 'Supplier'}`,
          description: [s.supplierCode, s.email, s.status].filter(Boolean).join(' · '),
          searchText: [s.displayId, s.id, s.companyName, s.supplierCode, s.email].filter(Boolean).join(' '),
          raw: s,
        })),
      )
    } catch (err) {
      setOptions([])
      setError(getErrorMessage(err) || 'Failed to load suppliers')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { reload() }, [reload])
  return { options, loading, error, reload }
}

export function useMaterialOptions({ supplierId = '' } = {}) {
  const [all, setAll] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const reload = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const rows = withDisplay(DISPLAY_ENTITY.MATERIAL, asArray(await materialApi.list()), 'createdAt')
      setAll(rows)
    } catch (err) {
      setAll([])
      setError(getErrorMessage(err) || 'Failed to load materials')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { reload() }, [reload])

  const options = useMemo(() => {
    const filtered = supplierId
      ? all.filter((m) => String(m.supplierId || '') === String(supplierId))
      : all
    return filtered.map((m) => {
      const displayId = m.displayId || ensureDisplayId(DISPLAY_ENTITY.MATERIAL, m.id)
      const supplierDisplay = m.supplierId
        ? ensureDisplayId(DISPLAY_ENTITY.SUPPLIER, m.supplierId)
        : null
      return {
        value: m.id,
        displayId,
        label: `${displayId} — ${m.name || 'Material'}`,
        description: [m.category, m.unit, m.price != null ? `price ${m.price}` : null, supplierDisplay]
          .filter(Boolean)
          .join(' · '),
        searchText: [displayId, m.id, m.name, m.category, m.supplierId, supplierDisplay].filter(Boolean).join(' '),
        raw: m,
      }
    })
  }, [all, supplierId])

  return { options, loading, error, reload, all }
}

export function useOrderOptions() {
  const [options, setOptions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const reload = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const rows = withDisplay(DISPLAY_ENTITY.ORDER, asArray(await orderApi.list()), 'createdDate')
      setOptions(
        rows.map((o) => {
          const userDisplay = o.userId ? ensureDisplayId(DISPLAY_ENTITY.USER, o.userId) : null
          return {
            value: o.id,
            displayId: o.displayId,
            label: `${o.displayId} — ${[userDisplay, o.status].filter(Boolean).join(' · ') || 'Order'}`,
            description: o.totalPrice != null ? `total ${o.totalPrice}` : undefined,
            searchText: [o.displayId, o.id, o.userId, userDisplay, o.status].filter(Boolean).join(' '),
            raw: o,
          }
        }),
      )
    } catch (err) {
      setOptions([])
      setError(getErrorMessage(err) || 'Failed to load orders')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { reload() }, [reload])
  return { options, loading, error, reload }
}

export function usePaymentOptions() {
  const [options, setOptions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const reload = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const rows = withDisplay(DISPLAY_ENTITY.PAYMENT, asArray(await paymentApi.list()), 'createdAt')
      setOptions(
        rows.map((p) => {
          const orderDisplay = p.orderId ? ensureDisplayId(DISPLAY_ENTITY.ORDER, p.orderId) : null
          const userDisplay = p.userId ? ensureDisplayId(DISPLAY_ENTITY.USER, p.userId) : null
          return {
            value: p.id,
            displayId: p.displayId,
            label: `${p.displayId} — ${[
              p.amount != null ? `${p.amount} ${p.currency || ''}`.trim() : null,
              p.status,
            ].filter(Boolean).join(' · ') || 'Payment'}`,
            description: [orderDisplay, userDisplay].filter(Boolean).join(' · ') || undefined,
            searchText: [p.displayId, p.id, p.orderId, orderDisplay, p.userId, userDisplay, p.status]
              .filter(Boolean)
              .join(' '),
            raw: p,
          }
        }),
      )
    } catch (err) {
      setOptions([])
      setError(getErrorMessage(err) || 'Failed to load payments')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { reload() }, [reload])
  return { options, loading, error, reload }
}

/**
 * User options for admins from GET /api/auth/users.
 * Non-admins get only the signed-in user (UI should lock via UserIdField).
 */
export function useUserOptions({ adminOnlyDirectory = false } = {}) {
  const { user } = useAuth()
  const admin = isAdminUser(user)
  const [options, setOptions] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const reload = useCallback(async () => {
    if (adminOnlyDirectory && !admin) {
      setOptions([])
      setLoading(false)
      setError('')
      return
    }

    if (!admin) {
      const authId = user?.id || user?.sub
      if (!authId) {
        setOptions([])
      } else {
        const displayId = ensureDisplayId(DISPLAY_ENTITY.USER, authId)
        setOptions([{
          value: String(authId),
          displayId,
          label: `${displayId} — ${user?.name || user?.email || 'Signed-in user'}`,
          description: [user?.email, 'Signed-in user'].filter(Boolean).join(' · '),
          searchText: [displayId, authId, user?.name, user?.email].filter(Boolean).join(' '),
          raw: user,
        }])
      }
      setLoading(false)
      setError('')
      return
    }

    setLoading(true)
    setError('')
    try {
      const rows = withDisplay(DISPLAY_ENTITY.USER, asArray(await authApi.listUsers()))
      setOptions(
        rows.map((u) => ({
          value: String(u.id),
          displayId: u.displayId,
          label: `${u.displayId} — ${u.name || u.email || 'User'}`,
          description: [u.email, ...(Array.isArray(u.roles) ? u.roles : [])].filter(Boolean).join(' · '),
          searchText: [u.displayId, u.id, u.name, u.email].filter(Boolean).join(' '),
          raw: u,
        })),
      )
    } catch (err) {
      setOptions([])
      setError(getErrorMessage(err) || 'Failed to load users')
    } finally {
      setLoading(false)
    }
  }, [admin, adminOnlyDirectory, user])

  useEffect(() => { reload() }, [reload])

  return { options, loading, error, reload }
}
