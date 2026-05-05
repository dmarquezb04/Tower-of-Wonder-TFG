import { useState, useEffect } from 'react'
import axios from 'axios'
import { useAuth } from '../../context/AuthContext'
import styles from './AdminDashboard.module.css'

export default function ProductEditModal({ isOpen, product, onClose, onSave }) {
  const { token } = useAuth()
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    stock: '',
    category: null,
    imageUrl: '',
    active: true
  })
  const [categories, setCategories] = useState([])
  const [newCategoryName, setNewCategoryName] = useState('')
  const [showNewCategoryInput, setShowNewCategoryInput] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (product) {
      setFormData({
        name: product.name || '',
        description: product.description || '',
        price: product.price || '',
        stock: product.stock || '',
        category: product.category || null,
        imageUrl: product.imageUrl || '',
        active: product.active ?? true
      })
    } else {
      setFormData({
        name: '',
        description: '',
        price: '',
        stock: '',
        category: null,
        imageUrl: '',
        active: true
      })
    }
    setError(null)
    setShowNewCategoryInput(false)
  }, [product, isOpen])

  // Cargar categorías al abrir el modal
  useEffect(() => {
    if (isOpen && token) {
      axios.get('/api/categories', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
        .then(res => {
          setCategories(res.data)
        })
        .catch(err => {
          console.error('Error cargando categorías:', err)
          setError('No se pudieron cargar las categorías')
        })
    }
  }, [isOpen])

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
    setError(null)
    try {
      // Si hay una categoría seleccionada por ID, nos aseguramos de enviar el objeto esperado
      const dataToSave = { ...formData }
      await onSave(product?.id, dataToSave)
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleAddCategory = async () => {
    if (!newCategoryName.trim()) return
    try {
      const res = await axios.post('/api/categories', 
        { name: newCategoryName },
        { headers: { 'Authorization': `Bearer ${token}` } }
      )
      
      const newCat = res.data
      setCategories(prev => [...prev, newCat])
      setFormData(prev => ({ ...prev, category: newCat }))
      setNewCategoryName('')
      setShowNewCategoryInput(false)
    } catch (err) {
      console.error(err)
      alert('Error al crear categoría: Verifique sus permisos de admin')
    }
  }

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent} style={{ maxWidth: '600px' }}>
        <h2>{product ? 'Editar Producto' : 'Nuevo Producto'}</h2>
        
        {error && <div className={styles.error}>{error}</div>}
        
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
            <div style={{ display: 'flex', gap: '8px' }}>
              {!showNewCategoryInput ? (
                <>
                  <select 
                    name="category" 
                    value={formData.category?.id || ''} 
                    onChange={(e) => {
                      const cat = categories.find(c => c.id === parseInt(e.target.value))
                      setFormData({ ...formData, category: cat })
                    }}
                    className={styles.searchInput}
                    required
                  >
                    <option value="">Selecciona una categoría...</option>
                    {categories.map(cat => (
                      <option key={cat.id} value={cat.id}>{cat.name}</option>
                    ))}
                  </select>
                  <button 
                    type="button" className={styles.btnAction} 
                    onClick={() => setShowNewCategoryInput(true)}
                  >
                    + Nueva
                  </button>
                </>
              ) : (
                <>
                  <input 
                    type="text" 
                    placeholder="Nombre de la categoría..."
                    value={newCategoryName}
                    onChange={(e) => setNewCategoryName(e.target.value)}
                    className={styles.searchInput}
                  />
                  <button type="button" className={styles.btnSuccess} onClick={handleAddCategory}>Añadir</button>
                  <button type="button" className={styles.btnCancel} onClick={() => setShowNewCategoryInput(false)}>X</button>
                </>
              )}
            </div>
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
