import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getNewsPostBySlug } from '../../api/newsApi'
import styles from './NewsPostPage.module.css'

/**
 * NewsPostPage — Vista completa de una noticia individual.
 * La URL es /noticias/:slug.
 */
export default function NewsPostPage() {
  const { slug }             = useParams()
  const navigate             = useNavigate()
  const [post, setPost]      = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]    = useState(null)

  useEffect(() => {
    const fetchPost = async () => {
      try {
        setLoading(true)
        const data = await getNewsPostBySlug(slug)
        setPost(data)
      } catch (err) {
        setError('No se encontró la noticia.')
        console.error(err)
      } finally {
        setLoading(false)
      }
    }
    fetchPost()
  }, [slug])

  const formatDate = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    return date.toLocaleDateString('es-ES', { day: 'numeric', month: 'long', year: 'numeric' })
  }

  if (loading) {
    return (
      <div className={styles.contenido}>
        <div className={styles.loadingState}>Cargando...</div>
      </div>
    )
  }

  if (error || !post) {
    return (
      <div className={styles.contenido}>
        <div className={styles.errorState}>
          <p>{error || 'Noticia no encontrada.'}</p>
          <button className={styles.backBtn} onClick={() => navigate('/noticias')}>
            ← Volver a Noticias
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.contenido}>
      <div className={styles.container}>
        <button className={styles.backBtn} onClick={() => navigate('/noticias')}>
          ← Volver a Noticias
        </button>

        {post.imageUrl && (
          <div className={styles.hero}>
            <img src={post.imageUrl} alt={post.title} className={styles.heroImg} />
          </div>
        )}

        <article className={styles.article}>
          <time className={styles.date}>{formatDate(post.publishedAt)}</time>
          <h1 className={styles.title}>{post.title}</h1>
          {post.summary && (
            <p className={styles.summary}>{post.summary}</p>
          )}
          <div className={styles.divider} />
          <div className={styles.content}>
            {/* Renderizamos el contenido preservando saltos de línea */}
            {post.content.split('\n').map((paragraph, i) =>
              paragraph.trim()
                ? <p key={i}>{paragraph}</p>
                : <br key={i} />
            )}
          </div>
        </article>
      </div>
    </div>
  )
}
