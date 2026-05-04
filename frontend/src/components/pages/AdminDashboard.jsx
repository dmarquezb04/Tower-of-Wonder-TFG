import { useState, useEffect } from 'react'
import { getAdminUsers, updateUser, deleteUser } from '../../api/authApi'
import { getAdminMetricsStats } from '../../api/metricsApi'
import { getAdminProducts, createProduct, updateProduct, deleteProduct } from '../../api/adminShopApi'
import { useAuth } from '../../context/AuthContext'
import styles from './AdminDashboard.module.css'

// Modales
import UserEditModal from './UserEditModal'
import ProductEditModal from './ProductEditModal'
import DialogModal from '../DialogModal/DialogModal'

export default function AdminDashboard() {
  const { token } = useAuth()
  
  // Estados para datos
  const [metrics, setMetrics] = useState(null)
  const [users, setUsers] = useState([])
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  
  // Estados para UI
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768)
  const [activeTab, setActiveTab] = useState('metrics') // 'metrics' | 'users' | 'products'
  const [searchTerm, setSearchTerm] = useState('')

  // Estados para Modales
  const [isUserModalOpen, setIsUserModalOpen] = useState(false)
  const [selectedUser, setSelectedUser] = useState(null)
  
  const [isProductModalOpen, setIsProductModalOpen] = useState(false)
  const [selectedProduct, setSelectedProduct] = useState(null)

  const [confirmDelete, setConfirmDelete] = useState({ open: false, type: '', id: null, title: '' })

  // 1. Detectar redimensión para bloqueo móvil
  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth < 768)
    window.addEventListener('resize', handleResize)
    return () => window.removeEventListener('resize', handleResize)
  }, [])

  // 2. Cargar métricas al entrar
  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const stats = await getAdminMetricsStats(token)
        setMetrics(stats)
      } catch (err) {
        console.error('Error cargando métricas:', err)
      }
    }
    if (token && !isMobile && activeTab === 'metrics') fetchMetrics()
  }, [token, isMobile, activeTab])

  // 3. Gestión de Usuarios
  const handleLoadUsers = async () => {
    try {
      setLoading(true)
      const usersData = await getAdminUsers(token)
      setUsers(usersData)
    } catch (err) {
      setError('Error al cargar usuarios: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleSaveUser = async (userId, userData) => {
    await updateUser(token, userId, userData)
    handleLoadUsers() // Recargar lista
  }

  const openUserModal = (user) => {
    setSelectedUser(user)
    setIsUserModalOpen(true)
  }

  // 4. Gestión de Productos
  const handleLoadProducts = async () => {
    try {
      setLoading(true)
      const productsData = await getAdminProducts(token)
      setProducts(productsData)
    } catch (err) {
      setError('Error al cargar productos: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleSaveProduct = async (id, productData) => {
    if (id) {
      await updateProduct(token, id, productData)
    } else {
      await createProduct(token, productData)
    }
    handleLoadProducts() // Recargar lista
  }

  const openProductModal = (product = null) => {
    setSelectedProduct(product)
    setIsProductModalOpen(true)
  }

  // 5. Eliminación genérica
  const askDelete = (type, id, name) => {
    setConfirmDelete({ 
      open: true, 
      type, 
      id, 
      title: `¿Eliminar ${type === 'user' ? 'usuario' : 'producto'}?`,
      message: `¿Estás seguro de que deseas eliminar permanentemente a "${name}"? Esta acción no se puede deshacer.`
    })
  }

  const onConfirmDelete = async () => {
    try {
      if (confirmDelete.type === 'user') {
        await deleteUser(token, confirmDelete.id)
        handleLoadUsers()
      } else {
        await deleteProduct(token, confirmDelete.id)
        handleLoadProducts()
      }
    } catch (err) {
      alert('Error al eliminar: ' + err.message)
    } finally {
      setConfirmDelete({ ...confirmDelete, open: false })
    }
  }

  // Filtrado en el cliente para búsqueda rápida
  const filteredUsers = users.filter(u => 
    u.username?.toLowerCase().includes(searchTerm.toLowerCase()) || 
    u.email.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const filteredProducts = products.filter(p => 
    p.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.category?.toLowerCase().includes(searchTerm.toLowerCase())
  )

  // Si es móvil, bloqueamos el acceso
  if (isMobile) {
    return (
      <div className={styles.mobileBlock}>
        <div className={styles.mobileBlockContent}>
          <h1>⚠️ Acceso Restringido</h1>
          <p>El panel de administración solo está disponible en escritorio.</p>
          <button onClick={() => window.location.href = '/'}>Volver al Inicio</button>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Panel de Control Administrativo</h1>

      {/* PESTAÑAS */}
      <div className={styles.tabs}>
        <button 
          className={activeTab === 'metrics' ? styles.tabActive : styles.tab} 
          onClick={() => { setActiveTab('metrics'); setError(null); }}
        >
          Métricas
        </button>
        <button 
          className={activeTab === 'users' ? styles.tabActive : styles.tab} 
          onClick={() => { setActiveTab('users'); setError(null); setSearchTerm(''); }}
        >
          Usuarios
        </button>
        <button 
          className={activeTab === 'products' ? styles.tabActive : styles.tab} 
          onClick={() => { setActiveTab('products'); setError(null); setSearchTerm(''); }}
        >
          Productos
        </button>
      </div>

      {error && <p className={styles.error}>{error}</p>}

      {/* CONTENIDO MÉTRICAS */}
      {activeTab === 'metrics' && (
        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>Métricas de Visitas</h2>
          <div className={styles.metricsGrid}>
            <div className={styles.metricCard}>
              <h3>Visitas Totales</h3>
              <p className={styles.metricValue}>{metrics?.total || 0}</p>
            </div>
            <div className={styles.metricCard}>
               <h3>Páginas Únicas</h3>
               <p className={styles.metricValue}>{metrics?.porUrl?.length || 0}</p>
            </div>
          </div>
          <div className={styles.tablesGrid}>
            <div className={styles.miniTableWrapper}>
              <h3>Top Páginas</h3>
              <table className={styles.miniTable}>
                <thead><tr><th>Ruta</th><th>Visitas</th></tr></thead>
                <tbody>
                  {metrics?.porUrl?.map((m, i) => (
                    <tr key={i}><td>{m.url}</td><td>{m.visitas}</td></tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className={styles.miniTableWrapper}>
              <h3>Zonas / IPs</h3>
              <table className={styles.miniTable}>
                <thead><tr><th>Zona</th><th>Visitas</th></tr></thead>
                <tbody>
                  {metrics?.porZona?.map((m, i) => (
                    <tr key={i}><td>{m.zona}</td><td>{m.visitas}</td></tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </section>
      )}

      {/* CONTENIDO USUARIOS */}
      {activeTab === 'users' && (
        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>Gestión de Usuarios</h2>
          <div className={styles.controls}>
            <input 
              type="text" placeholder="Buscar usuario..." 
              value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)}
              className={styles.searchInput}
            />
            <button onClick={handleLoadUsers} className={styles.btnPrimary} disabled={loading}>
              {loading ? 'Cargando...' : 'Cargar / Refrescar'}
            </button>
          </div>
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead>
                <tr><th>Username</th><th>Email</th><th>Rol</th><th>Activo</th><th>Acciones</th></tr>
              </thead>
              <tbody>
                {filteredUsers.map(u => (
                  <tr key={u.idUsuario}>
                    <td>{u.username}</td><td>{u.email}</td>
                    <td>
                      <span className={u.role === 'admin' ? styles.badgeAdmin : (u.role === 'moderator' ? styles.badgeModerator : styles.badgeUser)}>
                        {u.role || 'SIN ROL'}
                      </span>
                    </td>
                    <td>{u.activo ? '✅' : '❌'}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button className={styles.btnAction} onClick={() => openUserModal(u)}>Gestionar</button>
                        <button className={`${styles.btnAction} ${styles.btnDanger}`} onClick={() => askDelete('user', u.idUsuario, u.username)}>Eliminar</button>
                      </div>
                    </td>
                  </tr>
                ))}
                {users.length === 0 && !loading && (
                  <tr><td colSpan="5" className={styles.textCenter}>Pulsa en cargar para ver los usuarios.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {/* CONTENIDO PRODUCTOS */}
      {activeTab === 'products' && (
        <section className={styles.section}>
          <div className={styles.headerWithAction}>
            <h2 className={styles.sectionTitle}>Catálogo de Productos</h2>
            <button className={styles.btnSuccess} onClick={() => openProductModal()}>+ Añadir Producto</button>
          </div>
          <div className={styles.controls}>
            <input 
              type="text" placeholder="Buscar producto o categoría..." 
              value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)}
              className={styles.searchInput}
            />
            <button onClick={handleLoadProducts} className={styles.btnPrimary} disabled={loading}>
              Cargar / Refrescar
            </button>
          </div>
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead>
                <tr><th>Imagen</th><th>Nombre</th><th>Precio</th><th>Stock</th><th>Estado</th><th>Acciones</th></tr>
              </thead>
              <tbody>
                {filteredProducts.map(p => (
                  <tr key={p.id}>
                    <td><img src={p.imageUrl} alt={p.name} width="40" style={{ borderRadius: '4px' }} /></td>
                    <td>{p.name} <br/><small style={{color: '#888'}}>{p.category}</small></td>
                    <td>{p.price}€</td><td>{p.stock}</td>
                    <td>{p.active ? '✅' : '🚫'}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button className={styles.btnAction} onClick={() => openProductModal(p)}>Editar</button>
                        <button className={`${styles.btnAction} ${styles.btnDanger}`} onClick={() => askDelete('product', p.id, p.name)}>Eliminar</button>
                      </div>
                    </td>
                  </tr>
                ))}
                {products.length === 0 && !loading && (
                  <tr><td colSpan="6" className={styles.textCenter}>Pulsa en cargar para ver los productos.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {/* MODALES */}
      <UserEditModal 
        isOpen={isUserModalOpen} 
        user={selectedUser} 
        onClose={() => setIsUserModalOpen(false)} 
        onSave={handleSaveUser} 
      />

      <ProductEditModal 
        isOpen={isProductModalOpen} 
        product={selectedProduct} 
        onClose={() => setIsProductModalOpen(false)} 
        onSave={handleSaveProduct} 
      />

      <DialogModal 
        isOpen={confirmDelete.open}
        title={confirmDelete.title}
        message={confirmDelete.message}
        onConfirm={onConfirmDelete}
        onCancel={() => setConfirmDelete({ ...confirmDelete, open: false })}
        confirmText="Eliminar"
        isDanger={true}
      />
    </div>
  )
}
