import styles from './HomePage.module.css'

const assetsUrl = '/assets/'
const baseUrl = '/'

const PLATFORMS = [
  { name: 'Steam', img: `${assetsUrl}img/plataformas_descarga/steam_logo.png`, href: 'https://store.steampowered.com/' },
  { name: 'Xbox Series S/X', img: `${assetsUrl}img/plataformas_descarga/xbox_logo.png`, href: 'https://www.xbox.com/es-ES/microsoft-store' },
  { name: 'PlayStation 5', img: `${assetsUrl}img/plataformas_descarga/ps5_logo.png`, href: 'https://store.playstation.com/es-es/pages/latest' },
  { name: 'Nintendo Switch', img: `${assetsUrl}img/plataformas_descarga/switch_logo.png`, href: 'https://www.nintendo.com/es-es/Nintendo-eShop/Nintendo-eShop-1806894.html' },
]

export default function HomePage() {
  return (
    <div className={styles.contenido}>
      <div className={styles.hero}>
        <h1 className={styles.heroTitle}>¡Bienvenido a Tower of Wonder!</h1>
        <p className={styles.heroText}>
          Sumérgete en un mundo de fantasía y aventura con nuestro nuevo videojuego. Explora
          paisajes impresionantes, enfrenta desafíos épicos y descubre secretos ocultos en cada
          rincón. ¡Únete a la comunidad de jugadores y vive la experiencia de Tower of Wonder hoy
          mismo!
        </p>
      </div>

      <div className={styles.platformsSection}>
        <h2 className={styles.platformsTitle}>⇓ Descarga en todas las plataformas ⇓</h2>
        <div className={styles.platformsGrid} id="plataformas">
          {PLATFORMS.map(({ name, img, href }) => (
            <a key={name} href={href} className={styles.platformCard}>
              <img src={img} alt={name} className={styles.platformImg} />
            </a>
          ))}
        </div>
      </div>
    </div>
  )
}
