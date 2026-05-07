import { useState } from 'react'
import { subscribe } from '../../api/newsletterApi'
import styles from './Footer.module.css'

const assetsUrl = '/assets/'
const baseUrl = '/'

const SOCIAL_LINKS = [
  { href: 'https://www.x.com', src: `${assetsUrl}img/social_media/twitter.png`, alt: 'Twitter' },
  { href: 'https://www.instagram.com', src: `${assetsUrl}img/social_media/instagram.png`, alt: 'Instagram' },
  { href: 'https://www.youtube.com', src: `${assetsUrl}img/social_media/youtube.png`, alt: 'YouTube' },
  { href: 'https://www.tiktok.com', src: `${assetsUrl}img/social_media/tiktok.png`, alt: 'TikTok' },
  { href: 'https://www.discord.com', src: `${assetsUrl}img/social_media/discord.png`, alt: 'Discord' },
]

export default function Footer() {
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  const handleNewsletterSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setMessage(null)
    setError(null)
    
    const email = e.target[0].value

    try {
      const res = await subscribe(email)
      setMessage(res.message)
      e.target.reset()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <footer className={styles.footer}>
      <div id="redes_sociales" className={styles.socialLinks}>
        {SOCIAL_LINKS.map(({ href, src, alt }) => (
          <a key={alt} href={href} target="_blank" rel="noopener noreferrer">
            <img src={src} alt={alt} className={styles.socialIcon} />
          </a>
        ))}
      </div>

      <div className={styles.license}>
        <p className={styles.licenseText}>
          © {new Date().getFullYear()} Darío Márquez Bautista —{' '}
          Contenido bajo licencia{' '}
          <a
            href="https://creativecommons.org/licenses/by-nc-nd/4.0/"
            target="_blank"
            rel="noopener noreferrer"
          >
            Creative Commons BY-NC-ND 4.0
          </a>
        </p>
      </div>

      <div id="susc_newsletter" className={styles.newsletter}>
        <form onSubmit={handleNewsletterSubmit} className={styles.newsletterForm}>
          <input
            type="email"
            placeholder="¡Suscríbete a nuestra newsletter!"
            className={styles.newsletterInput}
            required
            disabled={loading}
          />
          <button type="submit" className={styles.newsletterBtn} disabled={loading}>
            {loading ? '...' : 'SUSCRIBIRSE'}
          </button>
        </form>
        {message && <p className={styles.successMessage}>{message}</p>}
        {error && <p className={styles.errorMessage}>{error}</p>}
      </div>
    </footer>
  )
}
