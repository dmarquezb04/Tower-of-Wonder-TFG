import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { confirmSubscription } from '../../api/newsletterApi';
import styles from './MessagePage.module.css';

export default function NewsletterConfirmPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const [status, setStatus] = useState('loading');
  const [message, setMessage] = useState('Confirmando suscripción...');
  const navigate = useNavigate();

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('Token no válido o ausente.');
      return;
    }

    const confirm = async () => {
      try {
        const response = await confirmSubscription(token);
        setStatus('success');
        setMessage(response.message || 'Suscripción confirmada correctamente.');
      } catch (err) {
        setStatus('error');
        setMessage(err.message);
      }
    };

    confirm();
  }, [token]);

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        {status === 'loading' && <h2 className={styles.loading}>Procesando...</h2>}
        {status === 'success' && <h2 className={styles.success}>¡Suscripción Confirmada!</h2>}
        {status === 'error' && <h2 className={styles.error}>Ocurrió un error</h2>}
        
        <p className={styles.message}>{message}</p>
        
        {status !== 'loading' && (
          <button onClick={() => navigate('/')} className={styles.btn}>
            Volver al Inicio
          </button>
        )}
      </div>
    </div>
  );
}
