import { X } from 'lucide-react'

export default function SideDrawer({
  open,
  title,
  subtitle,
  onClose,
  children,
  footer,
  wide = false,
}) {
  if (!open) return null

  return (
    <div className="drawer-root" role="dialog" aria-modal="true" aria-label={title}>
      <button type="button" className="drawer-backdrop" aria-label="Close drawer" onClick={onClose} />
      <aside className={`drawer-panel ${wide ? 'wide' : ''}`}>
        <header className="drawer-header">
          <div>
            <h3>{title}</h3>
            {subtitle ? <p>{subtitle}</p> : null}
          </div>
          <button type="button" className="icon-action" onClick={onClose} aria-label="Close">
            <X size={16} />
          </button>
        </header>
        <div className="drawer-body">{children}</div>
        {footer ? <footer className="drawer-footer">{footer}</footer> : null}
      </aside>
    </div>
  )
}
