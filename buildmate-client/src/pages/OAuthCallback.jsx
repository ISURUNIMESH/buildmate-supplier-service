import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import Loader from '../components/common/Loader'
import { useAuth } from '../auth/AuthContext'

export default function OAuthCallback() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const { loginWithToken } = useAuth()
  const [error, setError] = useState('')

  useEffect(() => {
    const token = params.get('token')
    const expiresIn = params.get('expires_in') || '3600'
    if (!token) {
      setError('No access token returned from Auth Server.')
      return
    }
    try {
      loginWithToken(token, Number(expiresIn))
      navigate('/', { replace: true })
    } catch (err) {
      setError(err.message || 'Unable to complete sign-in.')
    }
  }, [params, loginWithToken, navigate])

  if (error) {
    return (
      <div className="login-page">
        <div className="login-card">
          <h2>Sign-in failed</h2>
          <p className="login-error">{error}</p>
          <button type="button" className="btn btn-primary" onClick={() => navigate('/login')}>
            Back to login
          </button>
        </div>
      </div>
    )
  }

  return <Loader label="Completing Google sign-in…" />
}
