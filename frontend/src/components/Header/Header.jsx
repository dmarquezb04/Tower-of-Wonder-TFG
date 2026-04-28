import { useState } from 'react'
import NavMenu from '../NavMenu/NavMenu'
import styles from './Header.module.css'

const { isAuthenticated, username, baseUrl, assetsUrl } = window.APP_DATA

export default function Header({ onLoginClick }) {
  const [menuOpen, setMenuOpen] = useState(false)

  return (
    <header className={styles.header}>
      <a href={`${baseUrl}index.php`} className={styles.logoLink}>
        <img
          src={`${assetsUrl}img/logo.png`}
          alt="Logo Tower of Wonder"
          className={styles.logo}
        />
      </a>

      {isAuthenticated ? (
        <div className={styles.userWelcome}>
          Bienvenido, {username} |{' '}
          <a href={`${baseUrl}auth/logout.php`} className={styles.logoutLink}>
            Salir
          </a>
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
