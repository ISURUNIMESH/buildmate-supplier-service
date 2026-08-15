import { useEffect } from 'react'
import { BrowserRouter, Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import { ToastProvider } from './components/common/Toast'
import MainLayout from './components/layout/MainLayout'
import { AuthProvider, useAuth } from './auth/AuthContext'
import ProtectedRoute from './auth/ProtectedRoute'
import { ThemeProvider } from './theme/ThemeContext'
import { configureApiAuth } from './services/api'
import Login from './pages/Login'
import OAuthCallback from './pages/OAuthCallback'
import Dashboard from './pages/Dashboard'
import Materials from './pages/Materials'
import Suppliers from './pages/Suppliers'
import Orders from './pages/Orders'
import Cart from './pages/Cart'
import Inventory from './pages/Inventory'
import Payments from './pages/Payments'
import Invoices from './pages/Invoices'
import Reports from './pages/Reports'
import './styles/global.css'
import './styles/layout.css'
import './styles/components.css'
import './styles/login.css'

function ApiAuthBridge({ children }) {
  const { getAccessToken, logout } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    configureApiAuth({
      getToken: getAccessToken,
      onUnauthorized: () => {
        logout()
        navigate('/login', { replace: true })
      },
    })
  }, [getAccessToken, logout, navigate])

  return children
}

export default function App() {
  return (
    <ThemeProvider>
      <ToastProvider>
        <AuthProvider>
          <BrowserRouter>
            <ApiAuthBridge>
              <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/oauth/callback" element={<OAuthCallback />} />
                <Route
                  element={(
                    <ProtectedRoute>
                      <MainLayout />
                    </ProtectedRoute>
                  )}
                >
                  <Route index element={<Dashboard />} />
                  <Route path="materials" element={<Materials />} />
                  <Route path="suppliers" element={<Suppliers />} />
                  <Route path="orders" element={<Orders />} />
                  <Route path="cart" element={<Cart />} />
                  <Route path="inventory" element={<Inventory />} />
                  <Route path="payments" element={<Payments />} />
                  <Route path="invoices" element={<Invoices />} />
                  <Route path="reports" element={<Reports />} />
                </Route>
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </ApiAuthBridge>
          </BrowserRouter>
        </AuthProvider>
      </ToastProvider>
    </ThemeProvider>
  )
}
