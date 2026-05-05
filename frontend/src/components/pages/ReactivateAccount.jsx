import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
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
        const response = await axios.post('/api/auth/reactivate', { token });
        setStatus('success');
        setMessage(response.data.message || 'Cuenta reactivada correctamente.');
      } catch (err) {
        setStatus('error');
        const errorMsg = err.response?.data?.error || 'Error al reactivar la cuenta.';
        setMessage(errorMsg);
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
