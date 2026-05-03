import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import * as authApi from '../api/authApi'

/**
 * AuthContext — Estado global de autenticación
 *
 * Sustituye a window.APP_DATA.isAuthenticated / username / baseUrl.
 * El JWT se persiste en localStorage para sobrevivir recargas de página.
 *
 * Expone:
 *   - isAuthenticated {boolean}
 *   - user {null | {email, username}}
 *   - token {null | string}
 *   - login(email, password) → {requiresTwoFactor, tempToken?}
 *   - verifyTwoFactor(tempToken, code) → void
 *   - register(email, username, password) → void
 *   - logout() → void
 *   - authError {null | string}
 *   - clearAuthError() → void
 */

const AuthContext = createContext(null)

const TOKEN_KEY = 'tow_auth_token'
const USER_KEY  = 'tow_auth_user'

/**
 * Decodifica el payload del JWT sin verificar la firma.
 * Solo para leer datos del usuario en el cliente. La verificación real
 * la hace el servidor en cada petición.
 *
 * @param {string} token
 * @returns {object|null}
 */
function decodeJwtPayload(token) {
  try {
    const payload = token.split('.')[1]
    return JSON.parse(atob(payload))
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  // Restaurar estado desde localStorage al cargar la página
  const [token, setToken]     = useState(() => localStorage.getItem(TOKEN_KEY))
  const [user, setUser]       = useState(() => {
    const saved = localStorage.getItem(USER_KEY)
    return saved ? JSON.parse(saved) : null
  })
  const [authError, setAuthError] = useState(null)

  // Si hay un token guardado, verificar que no ha expirado
  useEffect(() => {
    if (token) {
      const payload = decodeJwtPayload(token)
      if (!payload || (payload.exp && payload.exp * 1000 < Date.now())) {
        // Token expirado — limpiar
        _clearSession()
      }
    }
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const _saveSession = useCallback((newToken, userData) => {
    // Decodificar el token para extraer los roles
    const payload = decodeJwtPayload(newToken)
    const roles = payload?.roles || []
    
    const enhancedUserData = { ...userData, roles }
    
    setToken(newToken)
    setUser(enhancedUserData)
    localStorage.setItem(TOKEN_KEY, newToken)
    localStorage.setItem(USER_KEY, JSON.stringify(enhancedUserData))
    setAuthError(null)
  }, [])

  const _clearSession = useCallback(() => {
    setToken(null)
    setUser(null)
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }, [])

  // ============================================================
  // login() — primer paso, puede requerir 2FA
  // Devuelve {requiresTwoFactor: false} si completó directamente,
  // o {requiresTwoFactor: true, tempToken} si hay que verificar 2FA.
  // ============================================================
  const login = useCallback(async (email, password) => {
    setAuthError(null)
    const res = await authApi.login(email, password)

    if (res.requiresTwoFactor) {
      // Devolver el token temporal para que LoginModal muestre el paso 2FA
      return { requiresTwoFactor: true, tempToken: res.token }
    }

    // Login completo
    _saveSession(res.token, { email: res.email, username: res.username })
    return { requiresTwoFactor: false }
  }, [_saveSession])

  // ============================================================
  // verifyTwoFactor() — segundo paso tras login con 2FA
  // ============================================================
  const verifyTwoFactor = useCallback(async (tempToken, code) => {
    setAuthError(null)
    const res = await authApi.verifyTwoFactor(tempToken, code)
    _saveSession(res.token, { email: res.email, username: res.username })
  }, [_saveSession])

  // ============================================================
  // register()
  // ============================================================
  const register = useCallback(async (email, username, password) => {
    setAuthError(null)
    await authApi.register(email, username, password)
    // El registro no hace login automático — el modal mostrará el paso de login
  }, [])

  // ============================================================
  // logout()
  // ============================================================
  const logout = useCallback(async () => {
    if (token) {
      try {
        await authApi.logout(token)
      } catch {
        // Si el servidor falla, hacemos logout local de todas formas
      }
    }
    _clearSession()
  }, [token, _clearSession])

  const clearAuthError = useCallback(() => setAuthError(null), [])

  const value = {
    isAuthenticated: !!token,
    user,
    token,
    login,
    verifyTwoFactor,
    register,
    logout,
    authError,
    clearAuthError,
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth() debe usarse dentro de <AuthProvider>')
  }
  return ctx
}
