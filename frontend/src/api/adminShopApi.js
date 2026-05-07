import apiClient, { getAuthHeader } from './apiClient';

/**
 * adminShopApi.js — Gestión administrativa de productos.
 */

const API_URL = '/admin/products';

export const getAdminProducts = async (token) => {
  const response = await apiClient.get(API_URL, getAuthHeader(token));
  return response.data;
};

export const createProduct = async (token, productData) => {
  const response = await apiClient.post(API_URL, productData, getAuthHeader(token));
  return response.data;
};

export const updateProduct = async (token, id, productData) => {
  const response = await apiClient.put(`${API_URL}/${id}`, productData, getAuthHeader(token));
  return response.data;
};

export const deleteProduct = async (token, id) => {
  const response = await apiClient.delete(`${API_URL}/${id}`, getAuthHeader(token));
  return response.data;
};
