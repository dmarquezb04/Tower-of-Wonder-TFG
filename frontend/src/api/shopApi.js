const API_URL = '/api/shop';

/**
 * Obtiene todos los productos, opcionalmente filtrados por categoría.
 */
export const getProducts = async (category = '') => {
  const url = category ? `${API_URL}/products?category=${encodeURIComponent(category)}` : `${API_URL}/products`;
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error('Error al cargar los productos');
  }
  return response.json();
};

/**
 * Simula la compra de un carrito.
 */
export const checkoutCart = async (token, cartItems) => {
  const response = await fetch(`${API_URL}/checkout`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(cartItems)
  });
  
  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.error || 'Error procesando la compra');
  }
  return data;
};
