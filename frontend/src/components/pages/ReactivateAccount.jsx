import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import styles from './MessagePage.module.css';

export default function ReactivateAccount() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const [status, setStatus] = useState('loading'); // loading, success, error
  const [message, setMessage] = useState('Verificando token de reactivación...');
  const navigate = useNavigate();

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('Token no válido o ausente.');
      return;
    }

    const reactivate = async () => {
      try {
        const response = await fetch('/api/auth/reactivate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ token })
        });
        
        const data = await response.json();
        
        if (response.ok) {
          setStatus('success');
          setMessage(data.message || 'Cuenta reactivada correctamente.');
        } else {
          setStatus('error');
          setMessage(data.error || 'Error al reactivar la cuenta.');
        }
      } catch (err) {
        setStatus('error');
        setMessage('Error de conexión con el servidor.');
      }
    };

    reactivate();
  }, [token]);

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        {status === 'loading' && <h2 className={styles.loading}>Procesando...</h2>}
        {status === 'success' && <h2 className={styles.success}>¡Bienvenido de nuevo!</h2>}
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
