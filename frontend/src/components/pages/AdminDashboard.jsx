import { useState, useEffect } from 'react'
import { getAdminUsers, updateUser, deleteUser } from '../../api/authApi'
import { getAdminMetricsStats } from '../../api/metricsApi'
import { getAdminProducts, createProduct, updateProduct, deleteProduct } from '../../api/adminShopApi'
import { getAdminCharacters, createCharacter, updateCharacter, deleteCharacter } from '../../api/characterApi'
import { getAdminNewsPosts, createNewsPost, updateNewsPost, deleteNewsPost } from '../../api/newsApi'
import { useAuth } from '../../context/AuthContext'
import styles from './AdminDashboard.module.css'

// Modales
import UserEditModal from './UserEditModal'
import ProductEditModal from './ProductEditModal'
import CategoryManagementModal from './CategoryManagementModal'
import CharacterEditModal from './CharacterEditModal'
import NewsPostEditModal from './NewsPostEditModal'
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
  const [activeTab, setActiveTab] = useState('metrics') // 'metrics' | 'users' | 'products' | 'characters' | 'news'
  const [searchTerm, setSearchTerm] = useState('')

  // Estados para Modales
  const [isUserModalOpen, setIsUserModalOpen] = useState(false)
  const [selectedUser, setSelectedUser] = useState(null)
  
  const [isProductModalOpen, setIsProductModalOpen] = useState(false)
  const [selectedProduct, setSelectedProduct] = useState(null)
  
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false)

  const [isCharacterModalOpen, setIsCharacterModalOpen] = useState(false)
  const [selectedCharacter, setSelectedCharacter] = useState(null)

  const [isNewsModalOpen, setIsNewsModalOpen] = useState(false)
  const [selectedNews, setSelectedNews] = useState(null)

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

  // 5. Gestión de Personajes
  const [characters, setCharacters] = useState([])
  const handleLoadCharacters = async () => {
    try {
      setLoading(true)
      const data = await getAdminCharacters(token)
      setCharacters(data)
    } catch (err) {
      setError('Error al cargar personajes: ' + err.message)
    } finally {
      setLoading(false)
    }
  }
  const handleSaveCharacter = async (id, data) => {
    if (id) { await updateCharacter(token, id, data) }
    else     { await createCharacter(token, data) }
    handleLoadCharacters()
  }
  const openCharacterModal = (char = null) => {
    setSelectedCharacter(char)
    setIsCharacterModalOpen(true)
  }

  // 6. Gestión de Noticias
  const [newsPosts, setNewsPosts] = useState([])
  const handleLoadNews = async () => {
    try {
      setLoading(true)
      const data = await getAdminNewsPosts(token)
      setNewsPosts(data)
    } catch (err) {
      setError('Error al cargar noticias: ' + err.message)
    } finally {
      setLoading(false)
    }
  }
  const handleSaveNews = async (id, data) => {
    if (id) { await updateNewsPost(token, id, data) }
    else     { await createNewsPost(token, data) }
    handleLoadNews()
  }
  const openNewsModal = (post = null) => {
    setSelectedNews(post)
    setIsNewsModalOpen(true)
  }

  // 7. Eliminación genérica
  const askDelete = (type, id, name) => {
    const labels = { user: 'usuario', product: 'producto', character: 'personaje', news: 'noticia' }
    setConfirmDelete({ 
      open: true, type, id, 
      title: `¿Eliminar ${labels[type] || type}?`,
      message: `¿Estás seguro de que deseas eliminar permanentemente "${name}"? Esta acción no se puede deshacer.`
    })
  }

  const onConfirmDelete = async () => {
    try {
      if (confirmDelete.type === 'user') {
        await deleteUser(token, confirmDelete.id)
        handleLoadUsers()
      } else if (confirmDelete.type === 'product') {
        await deleteProduct(token, confirmDelete.id)
        handleLoadProducts()
      } else if (confirmDelete.type === 'character') {
        await deleteCharacter(token, confirmDelete.id)
        handleLoadCharacters()
      } else if (confirmDelete.type === 'news') {
        await deleteNewsPost(token, confirmDelete.id)
        handleLoadNews()
      }
    } catch (err) {
      alert('Error al eliminar: ' + err.message)
    } finally {
      setConfirmDelete({ ...confirmDelete, open: false })
    }
  }

  // Filtrado en el cliente para búsqueda rápida
  const filteredUsers = (users || []).filter(u => {
    const usernameMatch = (u.username?.toLowerCase() || "").includes(searchTerm.toLowerCase());
    const emailMatch = (u.email?.toLowerCase() || "").includes(searchTerm.toLowerCase());
    return usernameMatch || emailMatch;
  });

  const filteredProducts = (products || []).filter(p => {
    const nameMatch = (p.name?.toLowerCase() || "").includes(searchTerm.toLowerCase());
    const categoryMatch = (p.category?.name?.toLowerCase() || "").includes(searchTerm.toLowerCase());
    return nameMatch || categoryMatch;
  });

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
        <button 
          className={activeTab === 'characters' ? styles.tabActive : styles.tab} 
          onClick={() => { setActiveTab('characters'); setError(null); setSearchTerm(''); }}
        >
          Personajes
        </button>
        <button 
          className={activeTab === 'news' ? styles.tabActive : styles.tab} 
          onClick={() => { setActiveTab('news'); setError(null); setSearchTerm(''); }}
        >
          Noticias
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
            <div style={{ display: 'flex', gap: '10px' }}>
              <button className={styles.btnAction} onClick={() => setIsCategoryModalOpen(true)}>📁 Categorías</button>
              <button className={styles.btnSuccess} onClick={() => openProductModal()}>+ Añadir Producto</button>
            </div>
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
                    <td>{p.name} <br/><small style={{color: '#888'}}>{p.category?.name || 'Sin categoría'}</small></td>
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

      {/* CONTENIDO PERSONAJES */}
      {activeTab === 'characters' && (
        <section className={styles.section}>
          <div className={styles.headerWithAction}>
            <h2 className={styles.sectionTitle}>Gestión de Personajes</h2>
            <div style={{ display: 'flex', gap: '10px' }}>
              <button className={styles.btnSuccess} onClick={() => openCharacterModal()}>+ Añadir Personaje</button>
            </div>
          </div>
          <div className={styles.controls}>
            <input
              type="text" placeholder="Buscar personaje..."
              value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)}
              className={styles.searchInput}
            />
            <button onClick={handleLoadCharacters} className={styles.btnPrimary} disabled={loading}>
              Cargar / Refrescar
            </button>
          </div>
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead>
                <tr><th>Nombre</th><th>Slug</th><th>Activo</th><th>Acciones</th></tr>
              </thead>
              <tbody>
                {(characters || []).filter(c => 
                  c && (c.name?.toLowerCase() || '').includes(searchTerm.toLowerCase())
                ).map(c => (
                  <tr key={c.id}>
                    <td>{c.name}</td>
                    <td style={{ color: '#aaa', fontSize: '13px' }}>{c.slug}</td>
                    <td>{c.active ? '✅' : '🚫'}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button className={styles.btnAction} onClick={() => openCharacterModal(c)}>Editar</button>
                        <button className={`${styles.btnAction} ${styles.btnDanger}`} onClick={() => askDelete('character', c.id, c.name)}>Eliminar</button>
                      </div>
                    </td>
                  </tr>
                ))}
                {characters.length === 0 && !loading && (
                  <tr><td colSpan="4" className={styles.textCenter}>Pulsa en cargar para ver los personajes.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {/* CONTENIDO NOTICIAS */}
      {activeTab === 'news' && (
        <section className={styles.section}>
          <div className={styles.headerWithAction}>
            <h2 className={styles.sectionTitle}>Gestión de Noticias</h2>
            <button className={styles.btnSuccess} onClick={() => openNewsModal()}>+ Nueva Noticia</button>
          </div>
          <div className={styles.controls}>
            <input
              type="text" placeholder="Buscar noticia..."
              value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)}
              className={styles.searchInput}
            />
            <button onClick={handleLoadNews} className={styles.btnPrimary} disabled={loading}>
              Cargar / Refrescar
            </button>
          </div>
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead>
                <tr><th>Título</th><th>Slug</th><th>Publicado</th><th>Acciones</th></tr>
              </thead>
              <tbody>
                {(newsPosts || []).filter(p =>
                  p && (p.title?.toLowerCase() || '').includes(searchTerm.toLowerCase())
                ).map(p => (
                  <tr key={p.id}>
                    <td>{p.title}</td>
                    <td style={{ color: '#aaa', fontSize: '13px' }}>{p.slug}</td>
                    <td>{p.active ? '✅' : '🚫'}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button className={styles.btnAction} onClick={() => openNewsModal(p)}>Editar</button>
                        <button className={`${styles.btnAction} ${styles.btnDanger}`} onClick={() => askDelete('news', p.id, p.title)}>Eliminar</button>
                      </div>
                    </td>
                  </tr>
                ))}
                {newsPosts.length === 0 && !loading && (
                  <tr><td colSpan="4" className={styles.textCenter}>Pulsa en cargar para ver las noticias.</td></tr>
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

      <CategoryManagementModal 
        isOpen={isCategoryModalOpen} 
        onClose={() => setIsCategoryModalOpen(false)} 
      />

      <CharacterEditModal
        isOpen={isCharacterModalOpen}
        character={selectedCharacter}
        onClose={() => setIsCharacterModalOpen(false)}
        onSave={handleSaveCharacter}
      />

      <NewsPostEditModal
        isOpen={isNewsModalOpen}
        post={selectedNews}
        onClose={() => setIsNewsModalOpen(false)}
        onSave={handleSaveNews}
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
