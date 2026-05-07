import { useState, useEffect } from 'react'
import { getCategories, updateCategory, deleteCategory } from '../../api/shopApi'
import { useAuth } from '../../context/AuthContext'
import styles from './AdminDashboard.module.css'

export default function CategoryManagementModal({ isOpen, onClose }) {
  const { token } = useAuth()
  const [categories, setCategories] = useState([])
  const [editId, setEditId] = useState(null)
  const [editName, setEditName] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const fetchCategories = async () => {
    if (!token) return
    try {
      const data = await getCategories()
      setCategories(data)
    } catch (err) {
      console.error('Error al cargar categorías:', err)
      setError(err.message)
    }
  }

  useEffect(() => {
    if (isOpen) fetchCategories()
  }, [isOpen])

  const handleUpdate = async (id) => {
    if (!editName.trim()) return
    try {
      await updateCategory(token, id, { name: editName })
      setEditId(null)
      fetchCategories()
    } catch (err) {
      setError(err.message)
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('¿Estás seguro? Los productos en esta categoría se quedarán sin categoría vinculada.')) return
    try {
      await deleteCategory(token, id)
      fetchCategories()
    } catch (err) {
      setError(err.message)
    }
  }

  if (!isOpen) return null

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent} style={{ maxWidth: '500px' }}>
        <div className={styles.headerWithAction}>
           <h2>Gestionar Categorías</h2>
           <button onClick={onClose} className={styles.btnCancel}>Cerrar</button>
        </div>

        {error && <div className={styles.error} style={{ marginTop: '10px' }}>{error}</div>}

        <div className={styles.tableWrapper} style={{ marginTop: '20px' }}>
          <table className={styles.table}>
            <thead>
              <tr><th>Nombre</th><th>Acciones</th></tr>
            </thead>
            <tbody>
              {categories.map(cat => (
                <tr key={cat.id}>
                  <td>
                    {editId === cat.id ? (
                      <input 
                        type="text" value={editName} 
                        onChange={(e) => setEditName(e.target.value)}
                        className={styles.searchInput}
                      />
                    ) : (
                      cat.name
                    )}
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      {editId === cat.id ? (
                        <>
                          <button className={styles.btnSuccess} onClick={() => handleUpdate(cat.id)}>Guardar</button>
                          <button className={styles.btnCancel} onClick={() => setEditId(null)}>X</button>
                        </>
                      ) : (
                        <>
                          <button className={styles.btnAction} onClick={() => { setEditId(cat.id); setEditName(cat.name); }}>Editar</button>
                          <button className={`${styles.btnAction} ${styles.btnDanger}`} onClick={() => handleDelete(cat.id)}>Borrar</button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
