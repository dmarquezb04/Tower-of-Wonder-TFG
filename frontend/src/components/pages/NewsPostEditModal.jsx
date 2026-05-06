import { useState, useEffect } from 'react'
import styles from './AdminDashboard.module.css'

/**
 * NewsPostEditModal — Modal CRUD para posts del blog en el panel de admin.
 * Reutiliza los estilos de AdminDashboard.module.css (igual que ProductEditModal).
 */
export default function NewsPostEditModal({ isOpen, post, onClose, onSave }) {
  const EMPTY = {
    title: '', slug: '', summary: '', content: '', imageUrl: '', active: true
  }
  const [formData, setFormData] = useState(EMPTY)
  const [loading, setLoading]   = useState(false)
  const [error, setError]       = useState(null)

  useEffect(() => {
    if (post) {
      setFormData({
        title:    post.title    || '',
        slug:     post.slug     || '',
        summary:  post.summary  || '',
        content:  post.content  || '',
        imageUrl: post.imageUrl || '',
        active:   post.active   ?? true,
      })
    } else {
      setFormData(EMPTY)
    }
    setError(null)
  }, [post, isOpen])

  if (!isOpen) return null

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target
    setFormData(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await onSave(post?.id, formData)
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent} style={{ maxWidth: '640px' }}>
        <h2>{post ? 'Editar Noticia' : 'Nueva Noticia'}</h2>

        {error && <div className={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} className={styles.modalForm}>
          <div className={styles.formGroup}>
            <label>Título</label>
            <input
              type="text" name="title" value={formData.title}
              onChange={handleChange} required className={styles.searchInput}
            />
          </div>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label>Slug (URL)</label>
              <input
                type="text" name="slug" value={formData.slug}
                onChange={handleChange} placeholder="actualizacion-v1-2"
                className={styles.searchInput}
              />
            </div>
            <div className={styles.formGroup}>
              <label>Imagen (URL)</label>
              <input
                type="text" name="imageUrl" value={formData.imageUrl}
                onChange={handleChange} placeholder="https://..."
                className={styles.searchInput}
              />
            </div>
          </div>

          <div className={styles.formGroup}>
            <label>Resumen (se muestra en la lista de noticias)</label>
            <textarea
              name="summary" value={formData.summary}
              onChange={handleChange} className={styles.searchInput} rows="2"
              maxLength={500}
            />
          </div>

          <div className={styles.formGroup}>
            <label>Contenido completo</label>
            <textarea
              name="content" value={formData.content}
              onChange={handleChange} required
              className={styles.searchInput} rows="10"
            />
          </div>

          <div className={styles.formGroup}>
            <label className={styles.checkboxLabel}>
              <input
                type="checkbox" name="active"
                checked={formData.active} onChange={handleChange}
              />
              Post Visible / Publicado
            </label>
          </div>

          <div className={styles.modalActions}>
            <button type="button" className={styles.btnCancel} onClick={onClose} disabled={loading}>
              Cancelar
            </button>
            <button type="submit" className={styles.btnSuccess} disabled={loading}>
              {loading ? 'Guardando...' : (post ? 'Actualizar' : 'Crear Post')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
