import axios from 'axios';

const API_URL = '/api/shop';
const CAT_URL = '/api/categories';

/**
 * Obtiene todas las categorías de la base de datos.
 */
export const getCategories = async () => {
  const response = await axios.get(CAT_URL);
  return response.data;
};

/**
 * Obtiene todos los productos, opcionalmente filtrados por categoría.
 */
export const getProducts = async (category = '') => {
  const url = category 
    ? `${API_URL}/products?category=${encodeURIComponent(category)}` 
    : `${API_URL}/products`;
    
  const response = await axios.get(url);
  return response.data;
};

/**
 * Simula la compra de un carrito.
 */
export const checkoutCart = async (token, cartItems) => {
  const response = await axios.post(`${API_URL}/checkout`, cartItems, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return response.data;
};
