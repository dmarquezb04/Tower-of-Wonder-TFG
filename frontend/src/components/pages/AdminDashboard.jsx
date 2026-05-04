import { useState, useEffect } from 'react'
import { getAdminUsers } from '../../api/authApi'
import { getAdminMetricsStats } from '../../api/metricsApi'
import { getAdminProducts } from '../../api/adminShopApi'
import { useAuth } from '../../context/AuthContext'
import styles from './AdminDashboard.module.css'

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

  // 1. Detectar redimensión para bloqueo móvil
  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth < 768)
    window.addEventListener('resize', handleResize)
    return () => window.removeEventListener('resize', handleResize)
  }, [])

  // 2. Cargar métricas al entrar (son ligeras)
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

  // 3. Cargar usuarios
  const handleLoadUsers = async () => {
    try {
      setLoading(true)
      const usersData = await getAdminUsers(token)
      if (searchTerm) {
        setUsers(usersData.filter(u => 
          u.username?.toLowerCase().includes(searchTerm.toLowerCase()) || 
          u.email.toLowerCase().includes(searchTerm.toLowerCase())
        ))
      } else {
        setUsers(usersData)
      }
    } catch (err) {
      setError('Error al cargar usuarios: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  // 4. Cargar productos
  const handleLoadProducts = async () => {
    try {
      setLoading(true)
      // Aquí usaremos el endpoint real cuando lo creemos
      const productsData = await getAdminProducts(token)
      setProducts(productsData)
    } catch (err) {
      setError('Error al cargar productos: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

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

      {/* CONTENIDO MÉTICAS */}
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
              {loading ? 'Buscando...' : 'Cargar Usuarios'}
            </button>
          </div>
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead>
                <tr><th>Username</th><th>Email</th><th>Roles</th><th>Estado</th><th>Acciones</th></tr>
              </thead>
              <tbody>
                {users.map(u => (
                  <tr key={u.idUsuario}>
                    <td>{u.username}</td><td>{u.email}</td>
                    <td>{u.roles.join(', ')}</td>
                    <td>{u.activo ? '✅' : '❌'}</td>
                    <td><button className={styles.btnAction}>Gestionar</button></td>
                  </tr>
                ))}
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
            <button className={styles.btnSuccess}>+ Añadir Producto</button>
          </div>
          <div className={styles.controls}>
            <input 
              type="text" placeholder="Buscar producto..." 
              className={styles.searchInput}
            />
            <button onClick={handleLoadProducts} className={styles.btnPrimary} disabled={loading}>
              Cargar Productos
            </button>
          </div>
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead>
                <tr><th>Imagen</th><th>Nombre</th><th>Precio</th><th>Stock</th><th>Estado</th><th>Acciones</th></tr>
              </thead>
              <tbody>
                {products.map(p => (
                  <tr key={p.id}>
                    <td><img src={p.imageUrl} alt={p.name} width="40" /></td>
                    <td>{p.name}</td><td>{p.price}€</td><td>{p.stock}</td>
                    <td>{p.active ? 'Activo' : 'Oculto'}</td>
                    <td><button className={styles.btnAction}>Editar</button></td>
                  </tr>
                ))}
                {products.length === 0 && (
                  <tr><td colSpan="6" className={styles.textCenter}>Pulsa en cargar para ver los productos.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </div>
  )
}
