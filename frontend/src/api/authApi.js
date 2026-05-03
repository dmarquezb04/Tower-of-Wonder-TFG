/**
 * authApi.js — Capa de comunicación con el backend Spring Boot
 *
 * Centraliza todas las llamadas a /api/auth/* para que ningún componente
 * haga fetch() directamente. Si el endpoint cambia, solo cambia aquí.
 *
 * No usa Axios (la dependencia no está instalada). fetch() nativo es
 * suficiente y evita una dependencia extra para este módulo.
 */

const API_BASE = '/api'

/**
 * Helper interno: ejecuta un fetch POST con JSON y devuelve el body parseado.
 * Lanza un Error con el mensaje del servidor si el status no es 2xx.
 *
 * @param {string} path  — ruta relativa (ej: '/auth/login')
 * @param {object} body  — objeto que se serializa a JSON
 * @param {string} [token] — si se proporciona, añade Authorization: Bearer <token>
 * @returns {Promise<object>} body de la respuesta
 */
async function postJson(path, body, token = null) {
  const headers = { 'Content-Type': 'application/json' }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const res = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers,
    body: JSON.stringify(body),
  })

  const data = await res.json().catch(() => ({}))

  if (!res.ok) {
    // Usar el mensaje del servidor si lo hay, o un genérico
    throw new Error(data.error || data.message || `Error ${res.status}`)
  }

  return data
}

/**
 * Helper interno: ejecuta un fetch GET y devuelve el body parseado.
 * Lanza un Error con el mensaje del servidor si el status no es 2xx.
 */
async function getJson(path, token = null) {
  const headers = {}
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const res = await fetch(`${API_BASE}${path}`, {
    method: 'GET',
    headers,
  })

  const data = await res.json().catch(() => ({}))

  if (!res.ok) {
    throw new Error(data.error || data.message || `Error ${res.status}`)
  }

  return data
}

// ============================================================
// Autenticación
// ============================================================

/**
 * Inicia sesión con email y contraseña.
 *
 * @param {string} email
 * @param {string} password
 * @returns {Promise<{token: string, requiresTwoFactor: boolean, email: string, username: string, message: string}>}
 */
export async function login(email, password) {
  return postJson('/auth/login', { email, password })
}

/**
 * Verifica el código TOTP para completar el login con 2FA.
 *
 * @param {string} tempToken — token temporal recibido en el paso de login
 * @param {string} code      — código de 6 dígitos de Google Authenticator
 * @returns {Promise<{token: string, email: string, username: string}>}
 */
export async function verifyTwoFactor(tempToken, code) {
  return postJson('/auth/verify-2fa', { code }, tempToken)
}

/**
 * Registra un nuevo usuario.
 *
 * @param {string} email
 * @param {string} username
 * @param {string} password
 * @returns {Promise<{message: string}>}
 */
export async function register(email, username, password) {
  return postJson('/auth/register', { email, username, password })
}

/**
 * Cierra la sesión revocando el token en el servidor.
 *
 * @param {string} token — JWT actual
 * @returns {Promise<{message: string}>}
 */
export async function logout(token) {
  return postJson('/auth/logout', {}, token)
}

// ============================================================
// Usuario y 2FA
// ============================================================

export async function getProfile(token) {
  return getJson('/user/profile', token)
}

export async function setup2FA(token) {
  return getJson('/user/2fa/setup', token)
}

export async function enable2FA(token, secret, code) {
  return postJson('/user/2fa/enable', { secret, code }, token)
}

export async function disable2FA(token, code) {
  return postJson('/user/2fa/disable', { code }, token)
}

export async function deleteAccount(token) {
  const res = await fetch(`${API_BASE}/user/me`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
  
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.error || data.message || `Error ${res.status}`)
  }
  
  return res.json().catch(() => ({}))
}

// ============================================================
// Administrador
// ============================================================

export async function getAdminUsers(token) {
  return getJson('/admin/users', token)
}

export async function getAdminMetrics(token) {
  return getJson('/admin/metrics', token)
}

