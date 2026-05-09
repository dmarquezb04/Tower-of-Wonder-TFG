import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { unsubscribeNewsletter } from '../../api/newsletterApi';
import styles from './MessagePage.module.css';

/**
 * NewsletterUnsubscribePage — Gestiona la baja de la newsletter mediante un token.
 */
export default function NewsletterUnsubscribePage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const [status, setStatus] = useState('loading');
  const [message, setMessage] = useState('Procesando tu solicitud de baja...');
  const navigate = useNavigate();

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('El enlace de baja no es válido o ha expirado.');
      return;
    }

    const performUnsubscribe = async () => {
      try {
        const response = await unsubscribeNewsletter(token);
        setStatus('success');
        setMessage(response.message || 'Has sido dado de baja correctamente.');
      } catch (err) {
        setStatus('error');
        setMessage(err.message || 'No se pudo procesar la baja.');
      }
    };

    // Pequeño retardo para que el usuario vea que algo ocurre
    const timer = setTimeout(performUnsubscribe, 1000);
    return () => clearTimeout(timer);
  }, [token]);

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.iconBox}>
          {status === 'loading' && <span style={{ fontSize: '48px' }}>⏳</span>}
          {status === 'success' && <span style={{ fontSize: '48px' }}>👋</span>}
          {status === 'error' && <span style={{ fontSize: '48px' }}>⚠️</span>}
        </div>

        {status === 'loading' && <h2 className={styles.loading}>Cancelando suscripción</h2>}
        {status === 'success' && <h2 className={styles.success}>Baja Confirmada</h2>}
        {status === 'error' && <h2 className={styles.error}>Enlace no válido</h2>}
        
        <p className={styles.message}>{message}</p>
        
        <div style={{ marginTop: '30px' }}>
          <button onClick={() => navigate('/')} className={styles.btn}>
            Volver a la Academia
          </button>
        </div>
      </div>
    </div>
  );
}
