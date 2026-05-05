import axios from 'axios';

const API_URL = '/api/shop';

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
