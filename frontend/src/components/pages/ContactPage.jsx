import { useState, useEffect } from 'react'
import { sendContactMessage } from '../../api/contactApi'
import { useAuth } from '../../context/AuthContext'
import styles from './ContactPage.module.css'

const INITIAL_FORM = { nombre: '', email: '', asunto: '', mensaje: '' }

/**
 * ContactPage — Formulario de contacto.
 */
export default function ContactPage() {
  const { user, token } = useAuth()
  const [form, setForm]       = useState(INITIAL_FORM)
  const [status, setStatus]   = useState(null) // null | 'sending' | 'success' | 'error'
  const [errorMsg, setErrorMsg] = useState('')

  // Pre-rellenar datos si el usuario está autenticado
  useEffect(() => {
    if (user) {
      setForm(prev => ({
        ...prev,
        nombre: prev.nombre || user.username || '',
        email: prev.email || user.email || ''
      }))
    }
  }, [user])

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setStatus('sending')
    setErrorMsg('')

    try {
      await sendContactMessage(form, token)
      setStatus('success')
      setForm(INITIAL_FORM)
    } catch (err) {
      setStatus('error')
      setErrorMsg(err.message)
    }
  }

  return (
    <div className={styles.contenido}>
      {/* FIX Bug 3: <div> + <h1> independiente en vez de <fieldset>/<legend> */}
      <div className={styles.formCard}>
        <h1 className={styles.formTitle}>Formulario de contacto</h1>

        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          {status === 'success' && (
            <div className={styles.alertSuccess}>
              ✓ ¡Mensaje enviado correctamente! Te responderemos pronto.
            </div>
          )}
          {status === 'error' && (
            <div className={styles.alertError}>{errorMsg}</div>
          )}

          <div className={styles.formRow}>
            <label htmlFor="contact-nombre">Nombre:</label>
            <div className={styles.formField}>
              <input
                type="text"
                id="contact-nombre"
                name="nombre"
                value={form.nombre}
                onChange={handleChange}
                required
                maxLength={100}
              />
            </div>
          </div>

          <div className={styles.formRow}>
            <label htmlFor="contact-email">Email:</label>
            <div className={styles.formField}>
              <input
                type="email"
                id="contact-email"
                name="email"
                value={form.email}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className={styles.formRow}>
            <label htmlFor="contact-asunto">Asunto:</label>
            <div className={styles.formField}>
              <input
                type="text"
                id="contact-asunto"
                name="asunto"
                value={form.asunto}
                onChange={handleChange}
                maxLength={150}
              />
            </div>
          </div>

          <div className={styles.formRow}>
            <label htmlFor="contact-mensaje">Mensaje:</label>
            <div className={styles.formField}>
              <textarea
                id="contact-mensaje"
                name="mensaje"
                value={form.mensaje}
                onChange={handleChange}
                rows={6}
                required
                maxLength={2000}
              />
            </div>
          </div>

          <div className={styles.formRowCenter}>
            <button type="submit" className={styles.submitBtn} disabled={status === 'sending'}>
              {status === 'sending' ? 'Enviando…' : 'Enviar mensaje'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
