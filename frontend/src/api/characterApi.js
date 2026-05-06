import axios from 'axios';

const API_URL = '/api/characters';

/**
 * Obtiene todos los personajes activos (público).
 */
export const getCharacters = async () => {
  const response = await axios.get(API_URL);
  return response.data;
};

/**
 * Obtiene un personaje por slug (público).
 */
export const getCharacterBySlug = async (slug) => {
  const response = await axios.get(`${API_URL}/${slug}`);
  return response.data;
};

// ─── Admin ───────────────────────────────────────────────────────────────────

const authHeader = (token) => ({ headers: { Authorization: `Bearer ${token}` } });

export const getAdminCharacters = async (token) => {
  const response = await axios.get(`${API_URL}/admin/all`, authHeader(token));
  return response.data;
};

export const createCharacter = async (token, data) => {
  const response = await axios.post(API_URL, data, authHeader(token));
  return response.data;
};

export const updateCharacter = async (token, id, data) => {
  const response = await axios.put(`${API_URL}/${id}`, data, authHeader(token));
  return response.data;
};

export const deleteCharacter = async (token, id) => {
  const response = await axios.delete(`${API_URL}/${id}`, authHeader(token));
  return response.data;
};
