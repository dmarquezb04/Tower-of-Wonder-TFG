import { useState, useEffect } from 'react'
import styles from './AdminDashboard.module.css'
import modalStyles from './CharacterEditModal.module.css'

/**
 * CharacterEditModal — Modal CRUD para personajes.
 *
 * La sección de imágenes es completamente dinámica:
 * - Un input + botón "Añadir" para agregar nuevas URLs
 * - Cada imagen en la lista muestra una miniatura y permite:
 *   ✎ Editar (inline, sin tocar la BD hasta que se guarda el formulario)
 *   🗑 Eliminar de la lista
 * Al guardar, se envía toda la lista al backend que reemplaza la anterior.
 */
export default function CharacterEditModal({ isOpen, character, onClose, onSave }) {
  const EMPTY_FORM = { name: '', slug: '', description: '', active: true }

  const [formData, setFormData]   = useState(EMPTY_FORM)
  // Lista de URLs de imágenes (strings)
  const [imageUrls, setImageUrls] = useState([])
  // Estado del input de nueva imagen
  const [newUrl, setNewUrl]       = useState('')
  // Índice de la imagen que se está editando inline (-1 = ninguna)
  const [editingIdx, setEditingIdx] = useState(-1)
  const [editingUrl, setEditingUrl] = useState('')

  const [loading, setLoading] = useState(false)
  const [error, setError]     = useState(null)

  // Sincronizar con el personaje recibido (modo edición) o limpiar (modo creación)
  useEffect(() => {
    if (character) {
      setFormData({
        name:        character.name        || '',
        slug:        character.slug        || '',
        description: character.description || '',
        active:      character.active      ?? true,
      })
      // Extraer las URLs de la lista de imágenes del personaje
      setImageUrls((character.images || []).map(img => img?.imageUrl).filter(Boolean))
    } else {
      setFormData(EMPTY_FORM)
      setImageUrls([])
    }
    setNewUrl('')
    setEditingIdx(-1)
    setError(null)
  }, [character, isOpen])

  if (!isOpen) return null

  // ─── Handlers del formulario ──────────────────────────────────────────────

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target
    setFormData(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      // Construir el payload con la lista de imágenes en el formato esperado por el backend
      const payload = {
        ...formData,
        images: imageUrls.map((url, i) => ({ imageUrl: url, sortOrder: i }))
      }
      await onSave(character?.id, payload)
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  // ─── Handlers de imágenes ─────────────────────────────────────────────────

  const handleAddImage = () => {
    const url = newUrl.trim()
    if (!url) return
    setImageUrls(prev => [...prev, url])
    setNewUrl('')
  }

  const handleDeleteImage = (idx) => {
    setImageUrls(prev => prev.filter((_, i) => i !== idx))
    if (editingIdx === idx) setEditingIdx(-1)
  }

  const startEdit = (idx) => {
    setEditingIdx(idx)
    setEditingUrl(imageUrls[idx])
  }

  const confirmEdit = (idx) => {
    const url = editingUrl.trim()
    if (!url) return
    setImageUrls(prev => prev.map((u, i) => (i === idx ? url : u)))
    setEditingIdx(-1)
  }

  const cancelEdit = () => setEditingIdx(-1)

  // ─── Render ───────────────────────────────────────────────────────────────

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent} style={{ maxWidth: '580px' }}>
        <h2>{character ? 'Editar Personaje' : 'Nuevo Personaje'}</h2>

        {error && <div className={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} className={styles.modalForm}>

          {/* Nombre + Slug */}
          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label>Nombre</label>
              <input
                type="text" name="name" value={formData.name}
                onChange={handleChange} required className={styles.searchInput}
              />
            </div>
            <div className={styles.formGroup}>
              <label>Slug (URL)</label>
              <input
                type="text" name="slug" value={formData.slug}
                onChange={handleChange} placeholder="personaje-ejemplo"
                className={styles.searchInput}
              />
            </div>
          </div>

          {/* Descripción */}
          <div className={styles.formGroup}>
            <label>Descripción</label>
            <textarea
              name="description" value={formData.description}
              onChange={handleChange} className={styles.searchInput} rows="4"
            />
          </div>

          {/* ── Gestor dinámico de imágenes ─────────────────────────── */}
          <div className={styles.formGroup}>
            <label>Imágenes (carrusel)</label>

            {/* Lista de imágenes actuales */}
            {imageUrls.length > 0 && (
              <ul className={modalStyles.imageList}>
                {imageUrls.map((url, idx) => (
                  <li key={idx} className={modalStyles.imageItem}>
                    {/* Miniatura */}
                    <div className={modalStyles.thumb}>
                      <img src={url} alt={`Imagen ${idx + 1}`} onError={(e) => { e.target.style.display = 'none' }} />
                      <span className={modalStyles.thumbNum}>{idx + 1}</span>
                    </div>

                    {/* URL (modo visualización o edición) */}
                    {editingIdx === idx ? (
                      <div className={modalStyles.editRow}>
                        <input
                          type="text"
                          value={editingUrl}
                          onChange={(e) => setEditingUrl(e.target.value)}
                          className={`${styles.searchInput} ${modalStyles.editInput}`}
                          autoFocus
                        />
                        <button type="button" className={modalStyles.btnSave} onClick={() => confirmEdit(idx)}>✓</button>
                        <button type="button" className={modalStyles.btnCancelEdit} onClick={cancelEdit}>✕</button>
                      </div>
                    ) : (
                      <span className={modalStyles.imageUrl} title={url}>{url}</span>
                    )}

                    {/* Botones de acción */}
                    {editingIdx !== idx && (
                      <div className={modalStyles.imageActions}>
                        <button type="button" className={modalStyles.btnEdit} onClick={() => startEdit(idx)} title="Editar URL">✎</button>
                        <button type="button" className={modalStyles.btnDelete} onClick={() => handleDeleteImage(idx)} title="Eliminar imagen">🗑</button>
                      </div>
                    )}
                  </li>
                ))}
              </ul>
            )}

            {/* Input para añadir nueva imagen */}
            <div className={modalStyles.addImageRow}>
              <input
                type="text"
                value={newUrl}
                onChange={(e) => setNewUrl(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), handleAddImage())}
                placeholder="https://ejemplo.com/imagen.jpg"
                className={`${styles.searchInput} ${modalStyles.addInput}`}
              />
              <button
                type="button"
                className={modalStyles.btnAdd}
                onClick={handleAddImage}
                disabled={!newUrl.trim()}
              >
                + Añadir
              </button>
            </div>
          </div>
          {/* ──────────────────────────────────────────────────────── */}

          {/* Activo */}
          <div className={styles.formGroup}>
            <label className={styles.checkboxLabel}>
              <input
                type="checkbox" name="active"
                checked={formData.active} onChange={handleChange}
              />
              Personaje Visible / Activo
            </label>
          </div>

          <div className={styles.modalActions}>
            <button type="button" className={styles.btnCancel} onClick={onClose} disabled={loading}>
              Cancelar
            </button>
            <button type="submit" className={styles.btnSuccess} disabled={loading}>
              {loading ? 'Guardando...' : (character ? 'Actualizar' : 'Crear Personaje')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
