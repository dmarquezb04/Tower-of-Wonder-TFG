import { useState, useEffect } from 'react'
import styles from './AdminDashboard.module.css'

export default function ProductEditModal({ isOpen, product, onClose, onSave }) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    stock: '',
    category: '',
    imageUrl: '',
    active: true
  })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (product) {
      setFormData({
        name: product.name || '',
        description: product.description || '',
        price: product.price || '',
        stock: product.stock || '',
        category: product.category || '',
        imageUrl: product.imageUrl || '',
        active: product.active ?? true
      })
    } else {
      setFormData({
        name: '',
        description: '',
        price: '',
        stock: '',
        category: '',
        imageUrl: '',
        active: true
      })
    }
  }, [product, isOpen])

  if (!isOpen) return null

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await onSave(product?.id, formData)
      onClose()
    } catch (err) {
      alert('Error al guardar el producto: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent} style={{ maxWidth: '600px' }}>
        <h2>{product ? 'Editar Producto' : 'Nuevo Producto'}</h2>
        
        <form onSubmit={handleSubmit} className={styles.modalForm}>
          <div className={styles.formGroup}>
            <label>Nombre del Producto</label>
            <input 
              type="text" name="name" value={formData.name} 
              onChange={handleChange} required className={styles.searchInput}
            />
          </div>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label>Precio (€)</label>
              <input 
                type="number" step="0.01" name="price" value={formData.price} 
                onChange={handleChange} required className={styles.searchInput}
              />
            </div>
            <div className={styles.formGroup}>
              <label>Stock</label>
              <input 
                type="number" name="stock" value={formData.stock} 
                onChange={handleChange} required className={styles.searchInput}
              />
            </div>
          </div>

          <div className={styles.formGroup}>
            <label>Categoría</label>
            <input 
              type="text" name="category" value={formData.category} 
              onChange={handleChange} className={styles.searchInput}
            />
          </div>

          <div className={styles.formGroup}>
            <label>URL de la Imagen</label>
            <input 
              type="text" name="imageUrl" value={formData.imageUrl} 
              onChange={handleChange} className={styles.searchInput}
            />
          </div>

          <div className={styles.formGroup}>
            <label>Descripción</label>
            <textarea 
              name="description" value={formData.description} 
              onChange={handleChange} className={styles.searchInput}
              rows="3"
            />
          </div>

          <div className={styles.formGroup}>
            <label className={styles.checkboxLabel}>
              <input 
                type="checkbox" name="active" checked={formData.active} 
                onChange={handleChange}
              />
              Producto Visible / Activo
            </label>
          </div>

          <div className={styles.modalActions}>
            <button type="button" className={styles.btnCancel} onClick={onClose} disabled={loading}>
              Cancelar
            </button>
            <button type="submit" className={styles.btnSuccess} disabled={loading}>
              {loading ? 'Guardando...' : (product ? 'Actualizar' : 'Crear Producto')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
