import { useEffect } from 'react'
import EntitySelect from './EntitySelect'
import DisplayId from './DisplayId'
import { useAuth } from '../../auth/AuthContext'
import { currentUserId, isAdminUser } from '../../auth/roles'
import { useUserOptions } from '../../hooks/useEntityOptions'
import { DISPLAY_ENTITY } from '../../utils/displayId'
import './userIdField.css'

/**
 * Role-aware User ID control:
 * - Normal users: locked to signed-in user ID (auto-applied); shows U_### .
 * - Admins: searchable dropdown of all users from GET /api/auth/users.
 * API values remain the real backend user id.
 */
export default function UserIdField({
  value = '',
  onChange,
  error = '',
  disabled = false,
  placeholder = 'Select User ID',
  emptyMessage = 'No users available',
  autoAssign = true,
}) {
  const { user } = useAuth()
  const admin = isAdminUser(user)
  const selfId = currentUserId(user)
  const { options, loading, error: loadError } = useUserOptions({ adminOnlyDirectory: true })

  useEffect(() => {
    if (!autoAssign || admin || !selfId) return
    if (value !== selfId) onChange?.(selfId)
  }, [admin, selfId, value, onChange, autoAssign])

  if (!admin) {
    const label = user?.name || user?.email || 'Signed-in user'
    return (
      <div className="user-id-field">
        <div className={`user-id-locked ${disabled ? 'is-disabled' : ''}`} title={selfId || undefined}>
          <span className="user-id-locked-label">{label}</span>
          <DisplayId type={DISPLAY_ENTITY.USER} id={selfId} className="user-id-locked-id" />
        </div>
        <p className="user-id-hint">Using your signed-in user ID</p>
        {error ? <div className="field-error">{error}</div> : null}
      </div>
    )
  }

  return (
    <div className="user-id-field">
      <EntitySelect
        value={value}
        onChange={onChange}
        options={options}
        loading={loading}
        error={loadError || error}
        disabled={disabled || loading}
        placeholder={placeholder}
        searchPlaceholder="Search user…"
        emptyMessage={emptyMessage}
      />
      <p className="user-id-hint">Admin: select any registered user</p>
    </div>
  )
}
