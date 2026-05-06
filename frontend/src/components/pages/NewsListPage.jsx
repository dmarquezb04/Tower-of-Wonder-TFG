import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getNewsPosts } from '../../api/newsApi'
import styles from './NewsListPage.module.css'

/**
 * NewsListPage — Listado de noticias tipo blog.
 * Muestra tarjetas con imagen, título, resumen y fecha.
 * Al hacer clic, navega a /noticias/:slug para el post completo.
 */
export default function NewsListPage() {
  const [posts, setPosts]   = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]   = useState(null)
  const navigate             = useNavigate()

  useEffect(() => {
    const fetchPosts = async () => {
      try {
        setLoading(true)
        const data = await getNewsPosts()
        setPosts(data)
      } catch (err) {
        setError('No se pudieron cargar las noticias.')
        console.error(err)
      } finally {
        setLoading(false)
      }
    }
    fetchPosts()
  }, [])

  const formatDate = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    return date.toLocaleDateString('es-ES', { day: 'numeric', month: 'long', year: 'numeric' })
  }

  return (
    <div className={styles.contenido}>
      <div className={styles.container}>
        <h1 className={styles.pageTitle}>Noticias</h1>

        {loading ? (
          <div className={styles.loading}>Cargando noticias...</div>
        ) : error ? (
          <div className={styles.error}>{error}</div>
        ) : posts.length === 0 ? (
          <div className={styles.empty}>No hay noticias disponibles todavía.</div>
        ) : (
          <div className={styles.grid}>
            {posts.map((post) => (
              <article
                key={post.id}
                className={styles.card}
                onClick={() => navigate(`/noticias/${post.slug}`)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => e.key === 'Enter' && navigate(`/noticias/${post.slug}`)}
              >
                {post.imageUrl && (
                  <div className={styles.cardImage}>
                    <img src={post.imageUrl} alt={post.title} />
                  </div>
                )}
                <div className={styles.cardBody}>
                  <time className={styles.cardDate}>{formatDate(post.publishedAt)}</time>
                  <h2 className={styles.cardTitle}>{post.title}</h2>
                  {post.summary && (
                    <p className={styles.cardSummary}>{post.summary}</p>
                  )}
                  <span className={styles.readMore}>Leer más →</span>
                </div>
              </article>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
