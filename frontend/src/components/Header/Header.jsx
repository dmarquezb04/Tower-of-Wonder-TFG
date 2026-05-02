import { useState } from 'react'
import NavMenu from '../NavMenu/NavMenu'
import { useAuth } from '../../context/AuthContext'
import styles from './Header.module.css'

const ASSETS_URL = '/assets/'

export default function Header({ onLoginClick }) {
  const { isAuthenticated, user, logout } = useAuth()
  const [menuOpen, setMenuOpen] = useState(false)

  const handleLogout = async () => {
    await logout()
    // La página se actualiza sola porque AuthContext cambia isAuthenticated
  }

  return (
    <header className={styles.header}>
      <a href="/" className={styles.logoLink}>
        <img
          src={`${ASSETS_URL}img/logo.png`}
          alt="Logo Tower of Wonder"
          className={styles.logo}
        />
      </a>

      {isAuthenticated ? (
        <div className={styles.userWelcome}>
          Bienvenido, {user?.username} |{' '}
          <button
            className={styles.logoutLink}
            onClick={handleLogout}
            aria-label="Cerrar sesión"
          >
            Salir
          </button>
        </div>
      ) : (
        <button
          id="login-button"
          className={styles.loginBtn}
          onClick={onLoginClick}
        >
          LOGIN
        </button>
      )}

      <button
        id="menu-toggle"
        className={styles.menuToggle}
        onClick={() => setMenuOpen(!menuOpen)}
        aria-label="Abrir menú"
        aria-expanded={menuOpen}
      >
        {menuOpen ? '✕' : '☰'}
      </button>

      <NavMenu isOpen={menuOpen} onClose={() => setMenuOpen(false)} />
    </header>
  )
}
