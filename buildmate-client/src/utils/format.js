export function formatMoney(value, currency = 'LKR') {
  const num = Number(value ?? 0)
  if (Number.isNaN(num)) return `${currency} 0.00`
  return `${currency} ${num.toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`
}

export function formatDate(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function formatDateOnly(value = new Date()) {
  return new Date(value).toLocaleDateString(undefined, {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

export function stockStatus(stock, threshold = 150) {
  const n = Number(stock ?? 0)
  if (n <= 0) return { label: 'OUT OF STOCK', tone: 'danger' }
  if (n <= threshold) return { label: 'LOW STOCK', tone: 'warning' }
  return { label: 'IN STOCK', tone: 'success' }
}

export function inventoryStatus(item) {
  const available = Number(item?.availableQuantity ?? 0)
  const minimum = Number(item?.minimumStock ?? 0)
  if (available <= 0) return { label: 'OUT OF STOCK', tone: 'danger' }
  if (minimum > 0 && available <= minimum) return { label: 'LOW STOCK', tone: 'warning' }
  return { label: 'IN STOCK', tone: 'success' }
}

export const ORDER_STATUSES = ['PENDING', 'CONFIRMED', 'PAID', 'CANCELLED', 'DELIVERED']
export const SUPPLIER_STATUSES = ['PENDING', 'APPROVED', 'REJECTED']
export const PAYMENT_STATUS_OPTIONS = ['PENDING', 'SUCCESS', 'REFUNDED', 'FAILED']
