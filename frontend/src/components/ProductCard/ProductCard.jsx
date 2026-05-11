import { useState } from 'react';
import { useCart } from '../../context/CartContext';
import styles from './ProductCard.module.css';

export default function ProductCard({ product }) {
  const { addToCart } = useCart();
  const [added, setAdded] = useState(false);

  const handleAdd = () => {
    addToCart(product);
    setAdded(true);
    // Volver al estado normal tras 2 segundos
    setTimeout(() => setAdded(false), 2000);
  };

  const hasBadge = product.description && product.description.includes('¡Doble!');

  return (
    <div className={styles.card}>
      {hasBadge && (
        <div className={styles.badge}>
          {product.description}
        </div>
      )}
      
      <div className={styles.imageContainer}>
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} className={styles.image} />
        ) : (
          <div className={styles.imagePlaceholder}>💎</div>
        )}
      </div>

      <div className={styles.info}>
        <h3 className={styles.name}>
          {product.name} 
          <span className={styles.infoIcon} data-tooltip={product.description || 'Sin descripción disponible'}>ⓘ</span>
        </h3>
      </div>

      <button 
        className={`${styles.btnPrice} ${added ? styles.added : ''}`} 
        onClick={handleAdd}
        disabled={added}
      >
        {added ? '¡Añadido! ✓' : `${product.price.toFixed(2)} €`}
      </button>
    </div>
  );
}
