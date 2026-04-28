import { useState, useEffect, useRef } from 'react'
import styles from './LoginModal.module.css'

const { baseUrl } = window.APP_DATA

export default function LoginModal({ isOpen, onClose, initialMode, errorMessage, successMessage }) {
  const [mode, setMode] = useState(initialMode || 'login')
  const overlayRef = useRef(null)

  // Sincronizar modo cuando cambia desde fuera (URL param)
  useEffect(() => {
    setMode(initialMode || 'login')
  }, [initialMode])

  // Cerrar con ESC
  useEffect(() => {
    if (!isOpen) return
    const handleKey = (e) => { if (e.key === 'Escape') onClose() }
    document.addEventListener('keydown', handleKey)
    return () => document.removeEventListener('keydown', handleKey)
  }, [isOpen, onClose])

  // Bloquear scroll del body cuando modal abierto
  useEffect(() => {
    document.body.style.overflow = isOpen ? 'hidden' : ''
    return () => { document.body.style.overflow = '' }
  }, [isOpen])

  const toggleMode = (e) => {
    e.preventDefault()
    setMode((m) => (m === 'login' ? 'register' : 'login'))
  }

  const handleOverlayClick = (e) => {
    if (e.target === overlayRef.current) onClose()
  }

  // Limpiar URL params al enviar (para no re-mostrar el error al volver)
  const handleSubmit = () => {
    const url = new URL(window.location)
    url.searchParams.delete('error')
    url.searchParams.delete('modal')
    url.searchParams.delete('success')
    url.searchParams.delete('message')
    const refererInput = document.getElementById('modal-referer')
    if (refererInput) refererInput.value = url.pathname + url.search
    window.history.replaceState({}, '', url)
  }

  const isLogin = mode === 'login'

  return (
    <div
      ref={overlayRef}
      className={`${styles.overlay} ${isOpen ? styles.active : ''}`}
      onClick={handleOverlayClick}
      aria-modal="true"
      role="dialog"
      aria-label={isLogin ? 'Iniciar sesión' : 'Crear cuenta'}
    >
      <div className={styles.container}>
        <button className={styles.closeBtn} onClick={onClose} aria-label="Cerrar">
          &times;
        </button>

        <div className={styles.content}>
          <h2 className={styles.title}>{isLogin ? 'Iniciar Sesión' : 'Crear Cuenta'}</h2>

          {/* Mensajes de estado */}
          {successMessage && (
            <div className={`${styles.alert} ${styles.alertSuccess}`}>{successMessage}</div>
          )}
          {errorMessage && (
            <div className={`${styles.alert} ${styles.alertError}`}>{errorMessage}</div>
          )}

          <form
            id="form-login"
            action={isLogin ? `${baseUrl}auth/login.php` : `${baseUrl}auth/register.php`}
            method="POST"
            onSubmit={handleSubmit}
          >
            <input type="hidden" id="modal-referer" name="referer" value={window.location.pathname} />

            <div className={styles.formRow}>
              <label htmlFor="modal-email">Correo electrónico:</label>
              <div className={styles.formField}>
                <input type="email" id="modal-email" name="email" required autoComplete="email" />
              </div>
            </div>

            <div className={styles.formRow}>
              <label htmlFor="modal-password">Contraseña:</label>
              <div className={styles.formField}>
                <input
                  type="password"
                  id="modal-password"
                  name="password"
                  required
                  autoComplete={isLogin ? 'current-password' : 'new-password'}
                />
              </div>
            </div>

            <div className={styles.formRowCenter}>
              <button type="submit" className={styles.submitBtn}>
                {isLogin ? 'Continuar' : 'Registrarse'}
              </button>
            </div>
          </form>

          <div className={styles.footer}>
            <p>
              {isLogin ? '¿No tienes cuenta?' : '¿Ya tienes cuenta?'}{' '}
              <a href="#" onClick={toggleMode}>
                {isLogin ? 'Regístrate aquí' : 'Inicia sesión aquí'}
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
