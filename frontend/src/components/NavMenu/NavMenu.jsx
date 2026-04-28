import { useState, useEffect, useRef } from 'react'
import styles from './NavMenu.module.css'

const { baseUrl } = window.APP_DATA

const NAV_ITEMS = [
  { label: 'INICIO', href: `${baseUrl}index.php` },
  {
    label: 'PERSONAJES', href: '#',
    children: [
      { label: 'Personaje 1', href: '#' },
      { label: 'Personaje 2', href: '#' },
      { label: 'Personaje 3', href: '#' },
      { label: 'Personaje 4', href: '#' },
    ]
  },
  {
    label: 'NOTICIAS', href: '#',
    children: [
      { label: 'Updates', href: '#' },
      { label: 'Blog de desarrollo', href: '#' },
    ]
  },
  { label: 'FAQ', href: '#' },
  { label: 'CONTACTO', href: `${baseUrl}contacto.php` },
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
  const isMobile = () => window.innerWidth <= 768

  const handleMainClick = (e) => {
    if (item.children) {
      if (isMobile()) {
        e.preventDefault()
        setOpen((prev) => !prev)
      }
    } else {
      if (isMobile()) onClose()
    }
  }

  return (
    <li className={`${styles.navItem} ${open ? styles.submenuOpen : ''}`}>
      <a href={item.href} className={styles.navLink} onClick={handleMainClick}>
        {item.label}
        {item.children && <span className={styles.arrow}>▾</span>}
      </a>
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
