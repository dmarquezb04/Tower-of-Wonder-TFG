import { useState, useEffect, useRef } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import styles from './NavMenu.module.css'

/**
 * Elementos de navegación.
 * - href externo: string con URL completa o '#'
 * - to: ruta interna de React Router (ej: '/', '/contacto')
 * - children: submenú desplegable
 */
const NAV_ITEMS = [
  { label: 'INICIO',      to: '/' },
  { label: '🏪 TIENDA',   to: '/shop' },
  { label: 'PERSONAJES',  to: '/personajes' },
  { label: 'NOTICIAS',    to: '/noticias' },
  { label: 'CONTACTO',    to: '/contacto' },
]

export default function NavMenu({ isOpen, onClose }) {
  const menuRef = useRef(null)

  // Cerrar al hacer clic fuera en móvil
  useEffect(() => {
    if (!isOpen) return
    const handleOutsideClick = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        const toggle = document.getElementById('menu-toggle')
        if (toggle && toggle.contains(e.target)) return
        onClose()
      }
    }
    document.addEventListener('mousedown', handleOutsideClick)
    return () => document.removeEventListener('mousedown', handleOutsideClick)
  }, [isOpen, onClose])

  return (
    <div
      ref={menuRef}
      id="menudiv"
      className={`${styles.menuDiv} ${isOpen ? styles.active : ''}`}
    >
      {/* Botón ✕ explícito — solo visible en el drawer móvil */}
      <button
        className={styles.closeDrawerBtn}
        onClick={onClose}
        aria-label="Cerrar menú"
      >
        ✕
      </button>

      <nav>
        <ul className={styles.menu} id="menu">
          {NAV_ITEMS.map((item) => (
            <NavItem key={item.label} item={item} onClose={onClose} />
          ))}
        </ul>
      </nav>

      {/* Licencia — solo visible en el drawer móvil */}
      <div className={styles.licenseMobile}>
        <p className={styles.licenseText}>
          © {new Date().getFullYear()} Darío Márquez Bautista —{' '}
          Contenido bajo licencia{' '}
          <a
            href="https://creativecommons.org/licenses/by-nc-nd/4.0/"
            target="_blank"
            rel="noopener noreferrer"
          >
            CC BY-NC-ND 4.0
          </a>
        </p>
      </div>
    </div>
  )
}

function NavItem({ item, onClose }) {
  const [open, setOpen] = useState(false)
  const navigate = useNavigate()

  // FIX Bug 5: usar matchMedia en vez de window.innerWidth snapshot
  // Esto es reactivo a cambios de orientación del dispositivo
  const isMobile = () => window.matchMedia('(max-width: 768px)').matches

  const handleMainClick = (e) => {
    if (item.children) {
      if (isMobile()) {
        // En móvil, el tap despliega/colapsa el submenú
        e.preventDefault()
        setOpen((prev) => !prev)
      }
      // En desktop, el hover CSS controla el submenú (no se interfiere)
    } else if (item.to) {
      // Ruta React Router — navegar y cerrar drawer
      if (isMobile()) onClose()
      e.preventDefault()
      navigate(item.to)
    } else {
      if (isMobile()) onClose()
    }
  }

  return (
    // FIX Bug 4: la clase submenuOpen controla la visibilidad en móvil vía JS,
    // mientras que el CSS deshabilita :hover en móvil (ver NavMenu.module.css)
    <li className={`${styles.navItem} ${open ? styles.submenuOpen : ''}`}>
      {/* Usar <Link> para rutas internas, <a> para externas */}
      {item.to ? (
        <Link
          to={item.to}
          className={styles.navLink}
          onClick={handleMainClick}
        >
          {item.label}
        </Link>
      ) : (
        <a href={item.href || '#'} className={styles.navLink} onClick={handleMainClick}>
          {item.label}
          {item.children && <span className={styles.arrow}>▾</span>}
        </a>
      )}

      {item.children && (
        <ul className={styles.submenu}>
          {item.children.map((child) => (
            <li key={child.label}>
              <a
                href={child.href}
                className={styles.submenuLink}
                onClick={() => isMobile() && onClose()}
              >
                {child.label}
              </a>
            </li>
          ))}
        </ul>
      )}
    </li>
  )
}
