import apiClient, { getAuthHeader } from './apiClient';

/**
 * newsApi.js — Gestión del blog de noticias.
 */

const BASE_URL = '/news';

/**
 * Obtiene todos los posts activos (público).
 */
export const getNewsPosts = async () => {
  const response = await apiClient.get(BASE_URL);
  return response.data;
};

/**
 * Obtiene un post por slug (público).
 */
export const getNewsPostBySlug = async (slug) => {
  const response = await apiClient.get(`${BASE_URL}/${slug}`);
  return response.data;
};

// --- Admin ---

export const getAdminNewsPosts = async (token) => {
  const response = await apiClient.get(`${BASE_URL}/admin/all`, getAuthHeader(token));
  return response.data;
};

export const createNewsPost = async (token, data) => {
  const response = await apiClient.post(BASE_URL, data, getAuthHeader(token));
  return response.data;
};

export const updateNewsPost = async (token, id, data) => {
  const response = await apiClient.put(`${BASE_URL}/${id}`, data, getAuthHeader(token));
  return response.data;
};

export const deleteNewsPost = async (token, id) => {
  const response = await apiClient.delete(`${BASE_URL}/${id}`, getAuthHeader(token));
  return response.data;
};
