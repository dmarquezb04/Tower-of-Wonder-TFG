import styles from './Footer.module.css'

const { assetsUrl, baseUrl } = window.APP_DATA

const SOCIAL_LINKS = [
  { href: 'https://www.x.com', src: `${assetsUrl}img/social_media/twitter.png`, alt: 'Twitter' },
  { href: 'https://www.instagram.com', src: `${assetsUrl}img/social_media/instagram.png`, alt: 'Instagram' },
  { href: 'https://www.youtube.com', src: `${assetsUrl}img/social_media/youtube.png`, alt: 'YouTube' },
  { href: 'https://www.tiktok.com', src: `${assetsUrl}img/social_media/tiktok.png`, alt: 'TikTok' },
  { href: 'https://www.discord.com', src: `${assetsUrl}img/social_media/discord.png`, alt: 'Discord' },
]

export default function Footer() {
  const handleNewsletterSubmit = (e) => {
    e.preventDefault()
    // TODO: conectar a endpoint newsletter
    alert('¡Gracias por suscribirte!')
    e.target.reset()
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
          />
          <button type="submit" className={styles.newsletterBtn}>
            SUSCRIBIRSE
          </button>
        </form>
      </div>
    </footer>
  )
}
