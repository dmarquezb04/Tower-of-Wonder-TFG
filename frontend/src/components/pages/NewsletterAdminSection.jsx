import { useState } from 'react';
import { broadcastNewsletter } from '../../api/newsletterApi';
import styles from './AdminDashboard.module.css';

/**
 * NewsletterAdminSection — Panel para enviar boletines informativos.
 */
export default function NewsletterAdminSection({ token }) {
  const [subject, setSubject] = useState('');
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!subject || !content) {
      setError('El asunto y el contenido son obligatorios.');
      return;
    }

    if (!window.confirm('¿Estás seguro de que deseas enviar esta newsletter a TODOS los suscriptores confirmados? Esta acción no se puede deshacer.')) {
      return;
    }

    try {
      setLoading(true);
      setError(null);
      setMessage(null);
      
      const response = await broadcastNewsletter(token, { subject, content });
      setMessage(response.message);
      setSubject('');
      setContent('');
    } catch (err) {
      setError(err.message || 'Error al enviar la newsletter.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className={styles.section}>
      <h2 className={styles.sectionTitle}>Difusión de Newsletter</h2>
      <p style={{ color: '#aaa', marginBottom: '20px' }}>
        Desde aquí puedes enviar un correo masivo a todos los usuarios que se han suscrito a la newsletter y han confirmado su dirección.
      </p>

      {message && <p style={{ color: '#4ade80', marginBottom: '16px', fontWeight: 'bold' }}>✅ {message}</p>}
      {error && <p className={styles.error}>❌ {error}</p>}

      <form onSubmit={handleSend} className={styles.tableWrapper} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
        <div className={styles.formGroup}>
          <label>Asunto del Correo</label>
          <input 
            type="text" 
            value={subject} 
            onChange={(e) => setSubject(e.target.value)}
            className={styles.searchInput}
            placeholder="Ej: ¡Novedades de la semana en Tower of Wonder!"
            required
          />
        </div>

        <div className={styles.formGroup}>
          <label>Contenido (Soporta HTML básico)</label>
          <textarea 
            value={content} 
            onChange={(e) => setContent(e.target.value)}
            className={styles.searchInput}
            placeholder="Escribe aquí el cuerpo del mensaje..."
            rows="15"
            style={{ width: '100%', minHeight: '300px', resize: 'vertical' }}
            required
          />
          <small style={{ color: '#666', marginTop: '8px', display: 'block' }}>
            Puedes usar etiquetas HTML como &lt;h1&gt;, &lt;b&gt;, &lt;p&gt;, &lt;a href="..."&gt;, etc.
          </small>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <button 
            type="submit" 
            className={styles.btnPrimary} 
            style={{ padding: '12px 40px', fontSize: '16px' }}
            disabled={loading}
          >
            {loading ? 'Enviando...' : '🚀 Enviar Newsletter'}
          </button>
        </div>
      </form>
    </section>
  );
}
