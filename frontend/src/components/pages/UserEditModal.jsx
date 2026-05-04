import { useState, useEffect } from 'react'
import styles from './AdminDashboard.module.css'

export default function UserEditModal({ isOpen, user, onClose, onSave }) {
  const [formData, setFormData] = useState({
    username: '',
    role: 'user',
    activo: true
  })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (user) {
      setFormData({
        username: user.username || '',
        role: user.role || 'user',
        activo: user.activo ?? true
      })
    }
  }, [user, isOpen])

  if (!isOpen || !user) return null

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
      await onSave(user.idUsuario, formData)
      onClose()
    } catch (err) {
      alert('Error al guardar: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent}>
        <h2>Gestionar Usuario</h2>
        <p style={{ color: '#888', marginBottom: '20px' }}>ID: {user.idUsuario} | Email: {user.email}</p>

        <form onSubmit={handleSubmit} className={styles.modalForm}>
          <div className={styles.formGroup}>
            <label>Nombre de Usuario</label>
            <input 
              type="text" name="username" value={formData.username} 
              onChange={handleChange} required className={styles.searchInput}
            />
          </div>

          <div className={styles.formGroup}>
            <label>Rol del Sistema</label>
            <select 
              name="role" value={formData.role} 
              onChange={handleChange} className={styles.searchInput}
              style={{ width: '100%', background: 'rgba(255,255,255,0.05)' }}
            >
              <option value="user" style={{ background: '#1a1525' }}>Usuario Estándar</option>
              <option value="moderator" style={{ background: '#1a1525' }}>Moderador</option>
              <option value="admin" style={{ background: '#1a1525' }}>Administrador</option>
            </select>
          </div>

          <div className={styles.formGroup}>
            <label className={styles.checkboxLabel}>
              <input 
                type="checkbox" name="activo" checked={formData.activo} 
                onChange={handleChange}
              />
              Cuenta Activa / Habilitada
            </label>
          </div>

          <div className={styles.modalActions}>
            <button type="button" className={styles.btnCancel} onClick={onClose} disabled={loading}>
              Cancelar
            </button>
            <button type="submit" className={styles.btnSuccess} disabled={loading}>
              {loading ? 'Guardando...' : 'Guardar Cambios'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
