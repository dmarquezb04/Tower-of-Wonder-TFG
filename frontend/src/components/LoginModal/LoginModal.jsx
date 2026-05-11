import { useState, useEffect, useRef } from 'react'
import { useAuth } from '../../context/AuthContext'
import styles from './LoginModal.module.css'

/**
 * LoginModal — Modal de login, registro y verificación 2FA.
 *
 * Flujo completo:
 *   1. Modo 'login' o 'register' (prop initialMode)
 *   2. Si login y el servidor responde requiresTwoFactor: true → modo '2fa'
 *   3. Al completar → onClose() y el AuthContext actualiza el header
 *
 * Ya no hace submit de formulario HTML a PHP. Llama directamente
 * a AuthContext.login() / register() / verifyTwoFactor() que a su vez
 * llaman a la API Spring Boot (/api/auth/*).
 */
export default function LoginModal({ isOpen, onClose, initialMode }) {
  const { login, verifyTwoFactor, register } = useAuth()

  const [mode, setMode]           = useState(initialMode || 'login')
  const [error, setError]         = useState(null)
  const [success, setSuccess]     = useState(null)
  const [loading, setLoading]     = useState(false)
  // Estado 2FA
  const [tempToken, setTempToken] = useState(null)
  const [twoFaCode, setTwoFaCode] = useState('')
  const [email, setEmail]         = useState('')
  const [password, setPassword]   = useState('')
  const [username, setUsername]   = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  const overlayRef = useRef(null)

  // Sincronizar modo cuando cambia desde fuera
  useEffect(() => {
    setMode(initialMode || 'login')
    setError(null)
    setSuccess(null)
    setTempToken(null)
    setTwoFaCode('')
    setPassword('')
    setConfirmPassword('')
  }, [initialMode, isOpen])

  // Cerrar con ESC
  useEffect(() => {
    if (!isOpen) return
    const handleKey = (e) => { if (e.key === 'Escape') handleClose() }
    document.addEventListener('keydown', handleKey)
    return () => document.removeEventListener('keydown', handleKey)
  }, [isOpen]) // eslint-disable-line react-hooks/exhaustive-deps

  // Bloquear scroll del body cuando modal abierto
  useEffect(() => {
    document.body.style.overflow = isOpen ? 'hidden' : ''
    return () => { document.body.style.overflow = '' }
  }, [isOpen])

  const handleClose = () => {
    setError(null)
    setSuccess(null)
    setTempToken(null)
    setTwoFaCode('')
    onClose()
  }

  const handleOverlayClick = (e) => {
    if (e.target === overlayRef.current) handleClose()
  }

  const toggleMode = (e) => {
    e.preventDefault()
    setError(null)
    setSuccess(null)
    setMode((m) => (m === 'login' ? 'register' : 'login'))
  }

  // ============================================================
  // Submit — Login
  // ============================================================
  const handleLoginSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    // Los valores se obtienen del estado (email, password)

    try {
      const res = await login(email, password)
      if (res.requiresTwoFactor) {
        // Guardar token temporal y mostrar paso de 2FA
        setTempToken(res.tempToken)
        setMode('2fa')
      } else {
        // Login completo — cerrar modal
        handleClose()
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  // ============================================================
  // Submit — Verificación 2FA
  // ============================================================
  const handleTwoFaSubmit = async (e) => {
    e.preventDefault()
    if (twoFaCode.length !== 6) {
      setError('El código debe tener 6 dígitos')
      return
    }
    setError(null)
    setLoading(true)

    try {
      await verifyTwoFactor(tempToken, twoFaCode)
      handleClose()
    } catch (err) {
      setError(err.message)
      setTwoFaCode('')
    } finally {
      setLoading(false)
    }
  }

  // ============================================================
  // Submit — Registro
  // ============================================================
  const handleRegisterSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    // Los valores se obtienen del estado (email, username, password, confirmPassword)

    if (password !== confirmPassword) {
      setError('Las contraseñas no coinciden')
      setLoading(false)
      return
    }
    if (password.length < 8) {
      setError('La contraseña debe tener al menos 8 caracteres')
      setLoading(false)
      return
    }

    try {
      await register(email, username, password)
      setSuccess('¡Cuenta creada! Ahora puedes iniciar sesión')
      // Limpiar campos para que no aparezcan en el login
      setEmail('')
      setUsername('')
      setPassword('')
      setConfirmPassword('')
      setMode('login')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  // ============================================================
  // Render
  // ============================================================
  const titles = {
    login:    'Iniciar Sesión',
    register: 'Crear Cuenta',
    '2fa':    'Verificación en dos pasos',
  }

  return (
    <div
      ref={overlayRef}
      className={`${styles.overlay} ${isOpen ? styles.active : ''}`}
      onClick={handleOverlayClick}
      aria-modal="true"
      role="dialog"
      aria-label={titles[mode]}
    >
      <div className={styles.container}>
        <button className={styles.closeBtn} onClick={handleClose} aria-label="Cerrar">
          &times;
        </button>

        <div className={styles.content}>
          <h2 className={styles.title}>{titles[mode]}</h2>

          {/* Mensajes de estado */}
          {success && (
            <div className={`${styles.alert} ${styles.alertSuccess}`}>{success}</div>
          )}
          {error && (
            <div className={`${styles.alert} ${styles.alertError}`}>{error}</div>
          )}

          {/* ---- FORMULARIO LOGIN ---- */}
          {mode === 'login' && (
            <form id="form-login" onSubmit={handleLoginSubmit}>
              <div className={styles.formRow}>
                <label htmlFor="modal-email">Correo electrónico:</label>
                <div className={styles.formField}>
                  <input
                    type="email"
                    id="modal-email"
                    name="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    autoComplete="email"
                    disabled={loading}
                  />
                </div>
              </div>

              <div className={styles.formRow}>
                <label htmlFor="modal-password">Contraseña:</label>
                <div className={styles.formField}>
                  <input
                    type="password"
                    id="modal-password"
                    name="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete="current-password"
                    disabled={loading}
                  />
                </div>
              </div>

              <div className={styles.formRowCenter}>
                <button type="submit" className={styles.submitBtn} disabled={loading}>
                  {loading ? 'Comprobando...' : 'Continuar'}
                </button>
              </div>
            </form>
          )}

          {/* ---- FORMULARIO 2FA ---- */}
          {mode === '2fa' && (
            <form id="form-2fa" onSubmit={handleTwoFaSubmit}>
              <p className={styles.twoFaInfo}>
                Introduce el código de 6 dígitos de tu aplicación Google Authenticator.
              </p>
              <div className={styles.formRow}>
                <label htmlFor="modal-2fa-code">Código:</label>
                <div className={styles.formField}>
                  <input
                    type="text"
                    id="modal-2fa-code"
                    name="code"
                    inputMode="numeric"
                    pattern="\d{6}"
                    maxLength={6}
                    required
                    autoComplete="one-time-code"
                    placeholder="000000"
                    value={twoFaCode}
                    onChange={(e) => setTwoFaCode(e.target.value.replace(/\D/g, ''))}
                    disabled={loading}
                    className={styles.codeInput}
                  />
                </div>
              </div>

              <div className={styles.formRowCenter}>
                <button type="submit" className={styles.submitBtn} disabled={loading || twoFaCode.length !== 6}>
                  {loading ? 'Verificando...' : 'Verificar'}
                </button>
              </div>

              <div className={styles.footer}>
                <p>
                  <a href="#" onClick={(e) => { e.preventDefault(); setMode('login'); setTempToken(null) }}>
                    ← Volver al login
                  </a>
                </p>
              </div>
            </form>
          )}

          {/* ---- FORMULARIO REGISTRO ---- */}
          {mode === 'register' && (
            <form id="form-register" onSubmit={handleRegisterSubmit}>
              <div className={styles.formRow}>
                <label htmlFor="modal-reg-email">Correo electrónico:</label>
                <div className={styles.formField}>
                  <input
                    type="email"
                    id="modal-reg-email"
                    name="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    autoComplete="email"
                    disabled={loading}
                  />
                </div>
              </div>

              <div className={styles.formRow}>
                <label htmlFor="modal-reg-username">Nombre de usuario:</label>
                <div className={styles.formField}>
                  <input
                    type="text"
                    id="modal-reg-username"
                    name="username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                    minLength={3}
                    maxLength={30}
                    autoComplete="username"
                    disabled={loading}
                  />
                </div>
              </div>

              <div className={styles.formRow}>
                <label htmlFor="modal-reg-password">Contraseña:</label>
                <div className={styles.formField}>
                  <input
                    type="password"
                    id="modal-reg-password"
                    name="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    minLength={8}
                    autoComplete="new-password"
                    disabled={loading}
                  />
                </div>
              </div>

              <div className={styles.formRow}>
                <label htmlFor="modal-reg-confirm">Confirmar contraseña:</label>
                <div className={styles.formField}>
                  <input
                    type="password"
                    id="modal-reg-confirm"
                    name="confirmPassword"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                    autoComplete="new-password"
                    disabled={loading}
                  />
                </div>
              </div>

              <div className={styles.formRowCenter}>
                <button type="submit" className={styles.submitBtn} disabled={loading}>
                  {loading ? 'Registrando...' : 'Registrarse'}
                </button>
              </div>
            </form>
          )}

          {/* Toggle login ↔ registro (no en modo 2FA) */}
          {mode !== '2fa' && (
            <div className={styles.footer}>
              <p>
                {mode === 'login' ? '¿No tienes cuenta?' : '¿Ya tienes cuenta?'}{' '}
                <a href="#" onClick={toggleMode}>
                  {mode === 'login' ? 'Regístrate aquí' : 'Inicia sesión aquí'}
                </a>
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
