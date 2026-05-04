import axios from 'axios';

const API_URL = '/api/admin/products';

// Configurar axios para usar el token (esto se podría centralizar en un axios instance)
const getHeaders = (token) => ({
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

export const getAdminProducts = async (token) => {
  const response = await axios.get(API_URL, getHeaders(token));
  return response.data;
};

export const createProduct = async (token, productData) => {
  const response = await axios.post(API_URL, productData, getHeaders(token));
  return response.data;
};

export const updateProduct = async (token, id, productData) => {
  const response = await axios.put(`${API_URL}/${id}`, productData, getHeaders(token));
  return response.data;
};

export const deleteProduct = async (token, id) => {
  const response = await axios.delete(`${API_URL}/${id}`, getHeaders(token));
  return response.data;
};
