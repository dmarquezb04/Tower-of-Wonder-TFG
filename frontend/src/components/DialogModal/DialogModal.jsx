import { useEffect, useRef } from 'react'
import styles from './DialogModal.module.css'

export default function DialogModal({ 
  isOpen, 
  title, 
  message, 
  onConfirm, 
  onCancel, 
  confirmText = 'Aceptar', 
  cancelText = 'Cancelar',
  isAlert = false,
  isDanger = false
}) {
  const overlayRef = useRef(null)

  // Cerrar con ESC (solo si no es alerta obligatoria, o si queremos que ESC equivalga a cancelar/aceptar)
  useEffect(() => {
    if (!isOpen) return
    const handleKey = (e) => {
      if (e.key === 'Escape') {
        if (isAlert) onConfirm()
        else onCancel()
      }
    }
    document.addEventListener('keydown', handleKey)
    return () => document.removeEventListener('keydown', handleKey)
  }, [isOpen, isAlert, onConfirm, onCancel])

  // Bloquear scroll del body
  useEffect(() => {
    document.body.style.overflow = isOpen ? 'hidden' : ''
    return () => { document.body.style.overflow = '' }
  }, [isOpen])

  const handleOverlayClick = (e) => {
    if (e.target === overlayRef.current) {
      if (isAlert) onConfirm()
      else onCancel()
    }
  }

  if (!isOpen) return null

  return (
    <div
      ref={overlayRef}
      className={`${styles.overlay} ${isOpen ? styles.active : ''}`}
      onClick={handleOverlayClick}
      aria-modal="true"
      role="dialog"
      aria-label={title}
    >
      <div className={styles.container}>
        <h2 className={styles.title}>{title}</h2>
        <p className={styles.message}>{message}</p>
        
        <div className={styles.actions}>
          {!isAlert && (
            <button className={styles.btnCancel} onClick={onCancel}>
              {cancelText}
            </button>
          )}
          <button 
            className={`${styles.btnConfirm} ${isDanger ? styles.btnDanger : ''}`} 
            onClick={onConfirm}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  )
}
