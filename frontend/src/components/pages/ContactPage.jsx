import { useState } from 'react'
import styles from './ContactPage.module.css'

const { baseUrl } = window.APP_DATA

const INITIAL_FORM = { nombre: '', email: '', asunto: '', mensaje: '' }

export default function ContactPage() {
  const [form, setForm] = useState(INITIAL_FORM)
  const [status, setStatus] = useState(null) // null | 'sending' | 'success' | 'error'
  const [errorMsg, setErrorMsg] = useState('')

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setStatus('sending')
    setErrorMsg('')

    try {
      const res = await fetch(`${baseUrl}api/contacto.php`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })
      const data = await res.json()
      if (data.ok) {
        setStatus('success')
        setForm(INITIAL_FORM)
      } else {
        setStatus('error')
        setErrorMsg(data.message ?? 'Error al enviar el mensaje. Inténtalo de nuevo.')
      }
    } catch {
      setStatus('error')
      setErrorMsg('Error de conexión. Comprueba tu red e inténtalo de nuevo.')
    }
  }

  return (
    <div className={styles.contenido}>
      <form className={styles.form} onSubmit={handleSubmit} noValidate>
        <fieldset className={styles.fieldset}>
          <legend className={styles.legend}>
            <h1>Formulario de contacto</h1>
          </legend>

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
        </fieldset>
      </form>
    </div>
  )
}
