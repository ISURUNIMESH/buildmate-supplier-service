import { displayIdOrFallback, ensureDisplayId } from '../../utils/displayId'
import './displayId.css'

/**
 * Renders PREFIX_001 for UI; real Mongo id stays in title for hover.
 * Never send this text to the API — use the `id` prop value for requests.
 */
export default function DisplayId({ type, id, className = '', fallback = '—' }) {
  if (id == null || id === '') {
    return <span className={`display-id is-empty ${className}`.trim()}>{fallback}</span>
  }
  const code = ensureDisplayId(type, id) || displayIdOrFallback(type, id)
  return (
    <span className={`display-id ${className}`.trim()} title={String(id)} data-real-id={String(id)}>
      {code || fallback}
    </span>
  )
}
