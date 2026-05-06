import { useState, useEffect } from 'react'
import { getCharacters } from '../../api/characterApi'
import styles from './CharactersPage.module.css'

/**
 * CharactersPage — Página de personajes con layout Maestro-Detalle.
 *
 * Izquierda: lista de personajes cargada dinámicamente desde /api/characters.
 * Derecha: información detallada del personaje seleccionado, con carrusel
 *          de hasta 3 imágenes navegable con flechas.
 */
export default function CharactersPage() {
  const [characters, setCharacters] = useState([])
  const [selected, setSelected]     = useState(null)
  const [loading, setLoading]        = useState(true)
  const [error, setError]            = useState(null)
  const [imgIndex, setImgIndex]      = useState(0)

  // Carga inicial de personajes
  useEffect(() => {
    const fetchCharacters = async () => {
      try {
        setLoading(true)
        const data = await getCharacters()
        setCharacters(data)
        if (data.length > 0) setSelected(data[0])
      } catch (err) {
        setError('No se pudieron cargar los personajes.')
        console.error(err)
      } finally {
        setLoading(false)
      }
    }
    fetchCharacters()
  }, [])

  // Resetear carrusel al cambiar de personaje
  const handleSelect = (character) => {
    setSelected(character)
    setImgIndex(0)
  }

  // La lista de imágenes viene como character.images[] con {id, imageUrl, sortOrder}
  const images = selected?.images?.map(img => img.imageUrl).filter(Boolean) ?? []

  const prevImage = () => setImgIndex((i) => (i - 1 + images.length) % images.length)
  const nextImage = () => setImgIndex((i) => (i + 1) % images.length)

  if (loading) {
    return (
      <div className={styles.contenido}>
        <div className={styles.loadingState}>Cargando personajes...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className={styles.contenido}>
        <div className={styles.errorState}>{error}</div>
      </div>
    )
  }

  if (characters.length === 0) {
    return (
      <div className={styles.contenido}>
        <div className={styles.emptyState}>
          <p>No hay personajes disponibles todavía.</p>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.contenido}>
      <div className={styles.layout}>

        {/* ── LISTA LATERAL (Maestro) ────────────────────── */}
        <aside className={styles.sidebar}>
          <h1 className={styles.sidebarTitle}>Personajes</h1>
          <ul className={styles.characterList}>
            {characters.map((char) => (
              <li key={char.id}>
                <button
                  className={`${styles.characterBtn} ${selected?.id === char.id ? styles.active : ''}`}
                  onClick={() => handleSelect(char)}
                >
                  {/* Avatar: primera imagen o placeholder */}
                  <div className={styles.avatar}>
                    {char.images?.[0]?.imageUrl
                      ? <img src={char.images[0].imageUrl} alt={char.name} />
                      : <span className={styles.avatarPlaceholder}>?</span>
                    }
                  </div>
                  <span className={styles.charName}>{char.name}</span>
                </button>
              </li>
            ))}
          </ul>
        </aside>

        {/* ── DETALLE (Detalle) ─────────────────────────── */}
        {selected && (
          <section className={styles.detail}>

            {/* Carrusel de imágenes */}
            <div className={styles.carousel}>
              {images.length > 0 ? (
                <>
                  <img
                    key={imgIndex}
                    src={images[imgIndex]}
                    alt={`${selected.name} — imagen ${imgIndex + 1}`}
                    className={styles.carouselImg}
                  />

                  {/* Overlay con el nombre del personaje */}
                  <div className={styles.nameOverlay}>
                    <h2>{selected.name}</h2>
                  </div>

                  {images.length > 1 && (
                    <>
                      <button
                        className={`${styles.carouselArrow} ${styles.carouselPrev}`}
                        onClick={prevImage}
                        aria-label="Imagen anterior"
                      >
                        ‹
                      </button>
                      <button
                        className={`${styles.carouselArrow} ${styles.carouselNext}`}
                        onClick={nextImage}
                        aria-label="Imagen siguiente"
                      >
                        ›
                      </button>
                      {/* Indicadores de punto */}
                      <div className={styles.dots}>
                        {images.map((_, i) => (
                          <button
                            key={i}
                            className={`${styles.dot} ${i === imgIndex ? styles.dotActive : ''}`}
                            onClick={() => setImgIndex(i)}
                            aria-label={`Ir a imagen ${i + 1}`}
                          />
                        ))}
                      </div>
                    </>
                  )}
                </>
              ) : (
                <div className={styles.noImage}>
                  <div className={styles.nameOverlay}>
                    <h2>{selected.name}</h2>
                  </div>
                  <span>Sin imágenes disponibles</span>
                </div>
              )}
            </div>

            {/* Información (Descripción a la derecha en desktop) */}
            <div className={styles.info}>
              <p className={styles.charDescription}>
                {selected.description || 'Descripción no disponible.'}
              </p>
            </div>
          </section>
        )}
      </div>
    </div>
  )
}
