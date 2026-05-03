import { useState, useEffect } from 'react'
import { getAdminMetrics, getAdminUsers } from '../../api/authApi'
import { useAuth } from '../../context/AuthContext'
import styles from './AdminDashboard.module.css'

export default function AdminDashboard() {
  const { token } = useAuth()
  
  const [metrics, setMetrics] = useState(null)
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true)
        const [metricsData, usersData] = await Promise.all([
          getAdminMetrics(token),
          getAdminUsers(token)
        ])
        setMetrics(metricsData)
        setUsers(usersData)
      } catch (err) {
        setError('Acceso denegado o error de conexión. ' + err.message)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [token])

  if (loading) return <div className={styles.container}>Cargando panel de administración...</div>
  if (error) return <div className={styles.container}><p className={styles.error}>{error}</p></div>

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Panel de Administración</h1>

      {metrics && (
        <div className={styles.metricsGrid}>
          <div className={styles.metricCard}>
            <h3>Total Usuarios</h3>
            <p className={styles.metricValue}>{metrics.totalUsers}</p>
          </div>
          <div className={styles.metricCard}>
            <h3>Usuarios Activos</h3>
            <p className={styles.metricValue}>{metrics.activeUsers}</p>
          </div>
          <div className={styles.metricCard}>
            <h3>Protegidos (2FA)</h3>
            <p className={styles.metricValue}>{metrics.usersWith2FA}</p>
          </div>
        </div>
      )}

      <div className={styles.tableWrapper}>
        <h2>Listado de Usuarios</h2>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>Email</th>
              <th>Roles</th>
              <th>2FA</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {users.map(user => (
              <tr key={user.idUsuario}>
                <td>{user.idUsuario}</td>
                <td>{user.username || '-'}</td>
                <td>{user.email}</td>
                <td>
                  {user.roles.map(r => {
                    const roleName = r.charAt(0).toUpperCase() + r.slice(1).toLowerCase();
                    const badgeClass = styles[`badge${roleName}`] || styles.badgeUser;
                    
                    return (
                      <span key={r} className={badgeClass}>
                        {r}
                      </span>
                    );
                  })}
                </td>
                <td>{user.twoFaEnabled ? '✅ Sí' : '❌ No'}</td>
                <td>
                  <span className={user.activo ? styles.textSuccess : styles.textDanger}>
                    {user.activo ? 'Activo' : 'Inactivo'}
                  </span>
                </td>
              </tr>
            ))}
            {users.length === 0 && (
              <tr>
                <td colSpan="6" className={styles.textCenter}>No hay usuarios registrados.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
