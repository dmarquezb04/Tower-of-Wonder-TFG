import { useState, useEffect } from 'react';
import { getProducts } from '../../api/shopApi';
import { useCart } from '../../context/CartContext';
import ProductCard from '../ProductCard/ProductCard';
import styles from './ShopPage.module.css';

export default function ShopPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [category, setCategory] = useState(''); 
  const [searchTerm, setSearchTerm] = useState('');
  
  const { cartCount, setIsCartOpen } = useCart();

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        const data = await getProducts(category);
        setProducts(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    fetchProducts();
  }, [category]);

  const categories = [
    { id: '', label: 'Todo' },
    { id: 'Bendición Lunar', label: 'Bendición Lunar' },
    { id: 'Pase de Batalla', label: 'Pase de Batalla' },
    { id: 'Cristal génesis', label: 'Cristal génesis' }
  ];

  // Filtrado por nombre (cliente)
  const filteredProducts = products.filter(p => 
    p.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className={styles.container}>
      <div className={styles.shopHeader}>
        {/* Píldoras de Filtro */}
        <div className={styles.filters}>
          {categories.map(cat => (
            <button
              key={cat.id}
              className={`${styles.filterBtn} ${category === cat.id ? styles.active : ''}`}
              onClick={() => setCategory(cat.id)}
            >
              {cat.label}
            </button>
          ))}
        </div>

        {/* Buscador y Carrito */}
        <div className={styles.controls}>
          <div className={styles.searchWrapper}>
            <span className={styles.searchIcon}>🔍</span>
            <input 
              type="text" 
              placeholder="Buscar por nombre..." 
              className={styles.searchInput}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          
          <button 
            className={styles.cartBtn} 
            onClick={() => setIsCartOpen(true)}
            aria-label="Abrir carrito"
          >
            🛒 Carrito {cartCount > 0 && <span className={styles.cartCount}>({cartCount})</span>}
          </button>
        </div>
      </div>

      {loading && <p className={styles.loading}>Cargando productos...</p>}
      {error && <p className={styles.error}>{error}</p>}
      
      {!loading && !error && filteredProducts.length === 0 && (
        <p className={styles.empty}>No se han encontrado productos.</p>
      )}

      {/* Grid de Productos */}
      <div className={styles.grid}>
        {filteredProducts.map(product => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </div>
  );
}
