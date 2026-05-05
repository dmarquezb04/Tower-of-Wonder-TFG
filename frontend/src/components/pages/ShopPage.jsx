import { useState, useEffect, useRef } from 'react';
import { getProducts, getCategories } from '../../api/shopApi';
import { useCart } from '../../context/CartContext';
import ProductCard from '../ProductCard/ProductCard';
import styles from './ShopPage.module.css';

const ShopPage = () => {
  const [products, setProducts] = useState([]);
  const [dbCategories, setDbCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [category, setCategory] = useState(''); 
  const [searchTerm, setSearchTerm] = useState('');
  const scrollRef = useRef(null);
  
  const { cartCount, setIsCartOpen } = useCart();

  // Cargar categorías una sola vez al montar
  useEffect(() => {
    const fetchCats = async () => {
      try {
        const data = await getCategories();
        setDbCategories(data);
      } catch (err) {
        console.error("Error cargando categorías:", err);
      }
    };
    fetchCats();
  }, []);

  // Cargar productos cuando cambie la categoría
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

  const allCategories = [
    { name: 'Todo', filter: '' },
    ...dbCategories.map(c => ({ name: c.name, filter: c.name }))
  ];

  const scroll = (offset) => {
    scrollRef.current.scrollBy({ left: offset, behavior: 'smooth' });
  };

  const filteredProducts = products.filter(p => 
    p.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className={styles.container}>
      <div className={styles.shopHeader}>
        <div className={styles.categoriesWrapper}>
          <button className={styles.scrollBtn} onClick={() => scroll(-150)}>‹</button>
          <div className={styles.filters} ref={scrollRef}>
            {allCategories.map((cat) => (
              <button
                key={cat.name}
                className={`${styles.filterBtn} ${category === cat.filter ? styles.active : ''}`}
                onClick={() => setCategory(cat.filter)}
              >
                {cat.name}
              </button>
            ))}
          </div>
          <button className={styles.scrollBtn} onClick={() => scroll(150)}>›</button>
        </div>

        <div className={styles.controls}>
          <div className={styles.searchWrapper}>
            <span className={styles.searchIcon}>🔍</span>
            <input 
              type="text" 
              placeholder="Buscar productos..." 
              className={styles.searchInput}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          
          <button className={styles.cartBtn} onClick={() => setIsCartOpen(true)}>
            <span>🛒 Carrito</span>
            <span className={styles.cartCount}>({cartCount})</span>
          </button>
        </div>
      </div>

      <div className={styles.grid}>
        {loading ? (
          <div className={styles.loading}>Cargando productos...</div>
        ) : error ? (
          <div className={styles.error}>{error}</div>
        ) : filteredProducts.length === 0 ? (
          <div className={styles.empty}>No se encontraron productos.</div>
        ) : (
          filteredProducts.map(product => (
            <ProductCard key={product.id} product={product} />
          ))
        )}
      </div>
    </div>
  );
};

export default ShopPage;
