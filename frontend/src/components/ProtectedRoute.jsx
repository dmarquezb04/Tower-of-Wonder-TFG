import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children, requireAdmin = false }) {
  const { isAuthenticated, user } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    // Redirigir a inicio si no está logueado
    return <Navigate to="/" state={{ from: location }} replace />
  }

  // Comprobar si requiere ser admin y si el usuario tiene el rol
  if (requireAdmin) {
    const isAdmin = user?.roles?.includes('admin') || user?.roles?.includes('ROLE_ADMIN')
    if (!isAdmin) {
      return <Navigate to="/dashboard" replace />
    }
  }

  return children
}
