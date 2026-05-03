import { useState } from 'react'
import { Link } from 'react-router-dom'
import NavMenu from '../NavMenu/NavMenu'
import { useAuth } from '../../context/AuthContext'
import styles from './Header.module.css'

const ASSETS_URL = '/assets/'

export default function Header({ onLoginClick }) {
  const { isAuthenticated, user, logout } = useAuth()
  const [menuOpen, setMenuOpen] = useState(false)
  const [dropdownOpen, setDropdownOpen] = useState(false)

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
          <div className={styles.dropdownContainer}>
            <button 
              className={styles.dropdownToggle} 
              onClick={() => setDropdownOpen(!dropdownOpen)}
            >
              Bienvenido, <span>{user?.username}</span> ▾
            </button>
            
            {dropdownOpen && (
              <div className={styles.dropdownMenu}>
                <Link 
                  to="/dashboard" 
                  className={styles.dropdownItem}
                  onClick={() => setDropdownOpen(false)}
                >
                  👤 Mi Perfil (Dashboard)
                </Link>
                
                {(user?.roles?.includes('admin') || user?.roles?.includes('ROLE_ADMIN')) && (
                  <Link 
                    to="/admin" 
                    className={styles.dropdownItem}
                    onClick={() => setDropdownOpen(false)}
                  >
                    ⚙️ Administración
                  </Link>
                )}
                
                <hr className={styles.dropdownDivider} />
                
                <button
                  className={styles.dropdownItem}
                  onClick={() => {
                    setDropdownOpen(false);
                    handleLogout();
                  }}
                  aria-label="Cerrar sesión"
                >
                  🚪 Cerrar Sesión
                </button>
              </div>
            )}
          </div>
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
