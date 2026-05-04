import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
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
        const response = await fetch('/api/newsletter/confirm', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ token })
        });
        
        const data = await response.json();
        
        if (response.ok) {
          setStatus('success');
          setMessage(data.message || 'Suscripción confirmada correctamente.');
        } else {
          setStatus('error');
          setMessage(data.error || 'Error al confirmar la suscripción.');
        }
      } catch (err) {
        setStatus('error');
        setMessage('Error de conexión con el servidor.');
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
