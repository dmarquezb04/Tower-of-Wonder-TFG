import axios from 'axios';

const API_URL = '/api/news';

/**
 * Obtiene todos los posts activos (público).
 */
export const getNewsPosts = async () => {
  const response = await axios.get(API_URL);
  return response.data;
};

/**
 * Obtiene un post por slug (público).
 */
export const getNewsPostBySlug = async (slug) => {
  const response = await axios.get(`${API_URL}/${slug}`);
  return response.data;
};

// ─── Admin ───────────────────────────────────────────────────────────────────

const authHeader = (token) => ({ headers: { Authorization: `Bearer ${token}` } });

export const getAdminNewsPosts = async (token) => {
  const response = await axios.get(`${API_URL}/admin/all`, authHeader(token));
  return response.data;
};

export const createNewsPost = async (token, data) => {
  const response = await axios.post(API_URL, data, authHeader(token));
  return response.data;
};

export const updateNewsPost = async (token, id, data) => {
  const response = await axios.put(`${API_URL}/${id}`, data, authHeader(token));
  return response.data;
};

export const deleteNewsPost = async (token, id) => {
  const response = await axios.delete(`${API_URL}/${id}`, authHeader(token));
  return response.data;
};
