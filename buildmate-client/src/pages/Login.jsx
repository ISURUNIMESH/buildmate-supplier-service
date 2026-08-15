import { useEffect, useState } from 'react'
import { HardHat, ShieldCheck, Building2, Workflow } from 'lucide-react'
import { Navigate, useLocation, useSearchParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const googleLoginUrl = import.meta.env.VITE_GOOGLE_LOGIN_URL
  || 'http://localhost:9000/oauth2/authorization/google'

export default function Login() {
  const { isAuthenticated, booting } = useAuth()
  const location = useLocation()
  const [params] = useSearchParams()
  const [loading, setLoading] = useState(false)
  const error = params.get('error')

  useEffect(() => {
    document.title = 'Sign in · BuildHub'
  }, [])

  if (!booting && isAuthenticated) {
    const dest = location.state?.from || '/'
    return <Navigate to={dest} replace />
  }

  const startGoogleLogin = () => {
    setLoading(true)
    window.location.assign(googleLoginUrl)
  }

  return (
    <div className="login-shell">
      <section className="login-hero" aria-label="BuildHub brand">
        <div className="login-hero-glow" aria-hidden="true" />
        <div className="login-hero-inner">
          <div className="login-hero-brand">
            <div className="brand-mark login-hero-mark"><HardHat size={28} /></div>
            <div>
              <p className="login-kicker">Construction Cloud</p>
              <h1 className="login-hero-title">BuildHub</h1>
            </div>
          </div>
          <p className="login-hero-lead">
            One workspace for materials, suppliers, inventory, and payments —
            secured through your enterprise gateway.
          </p>
          <ul className="login-hero-points">
            <li><ShieldCheck size={16} /> OAuth2 + BuildHub JWT</li>
            <li><Workflow size={16} /> Microservice orchestration</li>
            <li><Building2 size={16} /> Field-to-finance visibility</li>
          </ul>
        </div>
      </section>

      <section className="login-panel">
        <div className="login-panel-card">
          <p className="login-eyebrow">Secure sign-in</p>
          <h2>Continue to your workspace</h2>
          <p className="login-copy">
            Authenticate with Google. BuildHub then issues its own RSA-signed access token
            for the API Gateway — Google tokens never reach your microservices.
          </p>

          {error ? (
            <div className="login-error" role="alert">
              {decodeURIComponent(error)}
            </div>
          ) : null}

          <button
            type="button"
            className="btn-google"
            onClick={startGoogleLogin}
            disabled={loading || booting}
          >
            <GoogleIcon />
            {loading ? 'Redirecting to Google…' : 'Continue with Google'}
          </button>

          <p className="login-footnote">
            By continuing you access the protected BuildHub console at the organization level.
          </p>
        </div>
      </section>
    </div>
  )
}

function GoogleIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 48 48" aria-hidden="true">
      <path fill="#FFC107" d="M43.6 20.5H42V20H24v8h11.3C33.7 32.7 29.3 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3 0 5.8 1.1 7.9 3l5.7-5.7C34.2 6.1 29.4 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20 20-8.9 20-20c0-1.2-.1-2.3-.4-3.5z"/>
      <path fill="#FF3D00" d="M6.3 14.7l6.6 4.8C14.6 16 18.9 12 24 12c3 0 5.8 1.1 7.9 3l5.7-5.7C34.2 6.1 29.4 4 24 4 16.1 4 9.3 8.5 6.3 14.7z"/>
      <path fill="#4CAF50" d="M24 44c5.2 0 10-2 13.5-5.2l-6.2-5.2C29.2 35.4 26.7 36 24 36c-5.3 0-9.7-3.3-11.3-8l-6.5 5C9.2 39.5 16 44 24 44z"/>
      <path fill="#1976D2" d="M43.6 20.5H42V20H24v8h11.3c-1.1 3.1-3.5 5.5-6.5 6.9l.1.1 6.2 5.2C39.1 37.4 44 31.9 44 24c0-1.2-.1-2.3-.4-3.5z"/>
    </svg>
  )
}
