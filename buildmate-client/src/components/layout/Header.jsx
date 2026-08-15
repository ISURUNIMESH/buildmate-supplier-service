import { useEffect, useRef, useState } from 'react'
import {
  Bell,
  LogOut,
  Menu,
  Moon,
  Search,
  Sun,
  UserRound,
} from 'lucide-react'
import { useAuth } from '../../auth/AuthContext'
import { useTheme } from '../../theme/ThemeContext'
import { formatDateOnly } from '../../utils/format'

const SAMPLE_NOTES = [
  { id: 1, title: 'Payment received', body: 'Order payment marked SUCCESS', time: '2m' },
  { id: 2, title: 'Low stock alert', body: 'Cement bags below minimum', time: '1h' },
  { id: 3, title: 'Supplier verified', body: 'New supplier documents uploaded', time: 'Yesterday' },
]

export default function Header({ title, subtitle, onMenu }) {
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const [query, setQuery] = useState('')
  const [notesOpen, setNotesOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const notesRef = useRef(null)
  const profileRef = useRef(null)

  useEffect(() => {
    const onDoc = (e) => {
      if (notesRef.current && !notesRef.current.contains(e.target)) setNotesOpen(false)
      if (profileRef.current && !profileRef.current.contains(e.target)) setProfileOpen(false)
    }
    document.addEventListener('mousedown', onDoc)
    return () => document.removeEventListener('mousedown', onDoc)
  }, [])

  return (
    <header className="top-header">
      <div className="header-left">
        <button type="button" className="menu-toggle" onClick={onMenu} aria-label="Open menu">
          <Menu size={18} />
        </button>
        <div className="page-heading">
          <h1>{title}</h1>
          {subtitle ? <p>{subtitle}</p> : null}
        </div>
      </div>

      <div className="header-right">
        <label className="header-search" aria-label="Search">
          <Search size={16} />
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search workspace…"
          />
        </label>

        <div className="header-chip date-chip">{formatDateOnly()}</div>

        <button
          type="button"
          className="icon-btn"
          onClick={toggleTheme}
          aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
          title="Toggle theme"
        >
          {theme === 'dark' ? <Sun size={17} /> : <Moon size={17} />}
        </button>

        <div className="header-pop" ref={notesRef}>
          <button
            type="button"
            className="icon-btn"
            aria-label="Notifications"
            aria-expanded={notesOpen}
            onClick={() => {
              setNotesOpen((v) => !v)
              setProfileOpen(false)
            }}
          >
            <Bell size={17} />
            <span className="pulse-dot" />
          </button>
          {notesOpen ? (
            <div className="header-menu" role="menu">
              <div className="header-menu-note">
                <strong style={{ color: 'var(--text)', display: 'block', marginBottom: 4 }}>Notifications</strong>
                Workspace activity from BuildHub services
              </div>
              {SAMPLE_NOTES.map((n) => (
                <button key={n.id} type="button" className="header-menu-item" role="menuitem">
                  <span style={{ flex: 1 }}>
                    <strong style={{ display: 'block' }}>{n.title}</strong>
                    <span className="muted" style={{ fontWeight: 500 }}>{n.body}</span>
                  </span>
                  <span className="muted" style={{ fontSize: '0.72rem' }}>{n.time}</span>
                </button>
              ))}
            </div>
          ) : null}
        </div>

        <div className="header-pop" ref={profileRef}>
          <button
            type="button"
            className="profile-trigger"
            aria-expanded={profileOpen}
            onClick={() => {
              setProfileOpen((v) => !v)
              setNotesOpen(false)
            }}
          >
            {user?.profileImageUrl ? (
              <img className="avatar-img" src={user.profileImageUrl} alt={user.name || 'User'} />
            ) : (
              <div className="avatar">{(user?.name || 'BM').slice(0, 2).toUpperCase()}</div>
            )}
            <div className="profile-meta">
              <strong>{user?.name || 'BuildHub User'}</strong>
              <span>{user?.email || 'Signed in'}</span>
            </div>
          </button>
          {profileOpen ? (
            <div className="header-menu" role="menu">
              <div className="header-menu-note">
                Signed in as <strong style={{ color: 'var(--text)' }}>{user?.email || 'user'}</strong>
              </div>
              <button type="button" className="header-menu-item" role="menuitem">
                <UserRound size={16} /> Profile
              </button>
              <button type="button" className="header-menu-item danger" role="menuitem" onClick={logout}>
                <LogOut size={16} /> Sign out
              </button>
            </div>
          ) : null}
        </div>
      </div>
    </header>
  )
}
