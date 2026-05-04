import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';
import { checkoutCart } from '../../api/shopApi';
import styles from './CartModal.module.css';

export default function CartModal({ openLoginModal }) {
  const { isCartOpen, setIsCartOpen, cart, updateQuantity, removeFromCart, clearCart, cartTotal } = useCart();
  const { token, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  if (!isCartOpen) return null;

  const handleClose = () => {
    setIsCartOpen(false);
    setError(null);
    setSuccess(false);
  };

  const handleCheckout = async () => {
    if (!isAuthenticated) {
      handleClose();
      openLoginModal();
      return;
    }

    if (cart.length === 0) return;

    try {
      setLoading(true);
      setError(null);
      // Formatear el carrito para la API: [{id: 1, quantity: 2}]
      const cartItems = cart.map(item => ({ id: item.id, quantity: item.quantity }));
      
      // Simulamos un retraso de pasarela de pago para que sea visual
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      await checkoutCart(token, cartItems);
      setSuccess(true);
      clearCart();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.overlay} onClick={handleClose}>
      <div className={styles.modal} onClick={e => e.stopPropagation()}>
        <div className={styles.header}>
          <h2>Tu Carrito</h2>
          <button className={styles.closeBtn} onClick={handleClose}>&times;</button>
        </div>

        <div className={styles.content}>
          {success ? (
            <div className={styles.successBox}>
              <div className={styles.successIcon}>✓</div>
              <h3>¡Pago Completado!</h3>
              <p>Tu simulación de compra ha sido exitosa.</p>
              <button className={styles.btnPrimary} onClick={handleClose}>Continuar</button>
            </div>
          ) : loading ? (
            <div className={styles.loadingBox}>
              <div className={styles.spinner}></div>
              <p>Procesando pago seguro...</p>
            </div>
          ) : cart.length === 0 ? (
            <div className={styles.emptyBox}>
              <p>Tu carrito está vacío.</p>
              {window.location.pathname !== '/shop' && (
                <button className={styles.btnPrimary} onClick={() => { handleClose(); navigate('/shop'); }}>
                  Ir a la tienda
                </button>
              )}
            </div>
          ) : (
            <>
              {error && <div className={styles.error}>{error}</div>}
              
              <ul className={styles.cartList}>
                {cart.map(item => (
                  <li key={item.id} className={styles.cartItem}>
                    <div className={styles.itemInfo}>
                      <h4>{item.name}</h4>
                      <p>{item.price.toFixed(2)} € x {item.quantity}</p>
                    </div>
                    <div className={styles.itemControls}>
                      <button onClick={() => updateQuantity(item.id, item.quantity - 1)}>-</button>
                      <span>{item.quantity}</span>
                      <button onClick={() => updateQuantity(item.id, item.quantity + 1)}>+</button>
                      <button className={styles.btnRemove} onClick={() => removeFromCart(item.id)}>🗑️</button>
                    </div>
                  </li>
                ))}
              </ul>

              <div className={styles.footer}>
                <div className={styles.totalRow}>
                  <span>Total:</span>
                  <span>{cartTotal.toFixed(2)} €</span>
                </div>
                <div className={styles.actions}>
                  <button className={styles.btnClear} onClick={clearCart}>Vaciar Carrito</button>
                  <button className={styles.btnPrimary} onClick={handleCheckout}>Finalizar Compra</button>
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
