import apiClient, { getAuthHeader } from './apiClient';

/**
 * shopApi.js — Catálogo de productos, categorías y checkout.
 */

// --- Categorías ---

/**
 * Obtiene todas las categorías de la base de datos.
 */
export const getCategories = async () => {
  const response = await apiClient.get('/categories');
  return response.data;
};

/**
 * Crea una nueva categoría (Admin).
 */
export const createCategory = async (token, categoryData) => {
  const response = await apiClient.post('/categories', categoryData, getAuthHeader(token));
  return response.data;
};

/**
 * Actualiza una categoría existente (Admin).
 */
export const updateCategory = async (token, id, categoryData) => {
  const response = await apiClient.put(`/categories/${id}`, categoryData, getAuthHeader(token));
  return response.data;
};

/**
 * Elimina una categoría (Admin).
 */
export const deleteCategory = async (token, id) => {
  const response = await apiClient.delete(`/categories/${id}`, getAuthHeader(token));
  return response.data;
};

// --- Productos ---

/**
 * Obtiene todos los productos, opcionalmente filtrados por categoría.
 */
export const getProducts = async (category = '') => {
  const url = category 
    ? `/shop/products?category=${encodeURIComponent(category)}` 
    : '/shop/products';
    
  const response = await apiClient.get(url);
  return response.data;
};

/**
 * Simula la compra de un carrito. Requiere autenticación.
 */
export const checkoutCart = async (token, cartItems) => {
  const response = await apiClient.post('/shop/checkout', cartItems, getAuthHeader(token));
  return response.data;
};
