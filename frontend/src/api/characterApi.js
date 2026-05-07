import apiClient, { getAuthHeader } from './apiClient';

/**
 * characterApi.js — Gestión de personajes del juego.
 */

const BASE_URL = '/characters';

/**
 * Obtiene todos los personajes activos (público).
 */
export const getCharacters = async () => {
  const response = await apiClient.get(BASE_URL);
  return response.data;
};

/**
 * Obtiene un personaje por slug (público).
 */
export const getCharacterBySlug = async (slug) => {
  const response = await apiClient.get(`${BASE_URL}/${slug}`);
  return response.data;
};

// --- Admin ---

export const getAdminCharacters = async (token) => {
  const response = await apiClient.get(`${BASE_URL}/admin/all`, getAuthHeader(token));
  return response.data;
};

export const createCharacter = async (token, data) => {
  const response = await apiClient.post(BASE_URL, data, getAuthHeader(token));
  return response.data;
};

export const updateCharacter = async (token, id, data) => {
  const response = await apiClient.put(`${BASE_URL}/${id}`, data, getAuthHeader(token));
  return response.data;
};

export const deleteCharacter = async (token, id) => {
  const response = await apiClient.delete(`${BASE_URL}/${id}`, getAuthHeader(token));
  return response.data;
};
