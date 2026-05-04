import { useCart } from '../../context/CartContext';
import styles from './ProductCard.module.css';

export default function ProductCard({ product }) {
  const { addToCart } = useCart();

  // Si tiene un tag promocional en la descripción (ej: "¡Doble! +60") lo extraemos para simular la imagen.
  // Como no tenemos campo específico, usaremos la descripción como tag si empieza por "!" o similar, o simplemente lo mockeamos.
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
          <span className={styles.infoIcon}>ⓘ</span>
        </h3>
      </div>

      <button className={styles.btnPrice} onClick={() => addToCart(product)}>
        {product.price.toFixed(2)} €
      </button>
    </div>
  );
}
