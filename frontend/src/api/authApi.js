/**
 * authApi.js — Capa de comunicación con el backend para autenticación y perfil.
 * 
 * Ahora utiliza apiClient.js para centralizar el manejo de errores.
 */
import apiClient, { getAuthHeader } from './apiClient';

// ============================================================
// Autenticación
// ============================================================

export async function login(email, password) {
  const response = await apiClient.post('/auth/login', { email, password });
  return response.data;
}

export async function verifyTwoFactor(tempToken, code) {
  const response = await apiClient.post('/auth/verify-2fa', { code }, getAuthHeader(tempToken));
  return response.data;
}

export async function register(email, username, password) {
  const response = await apiClient.post('/auth/register', { email, username, password });
  return response.data;
}

export async function logout(token) {
  const response = await apiClient.post('/auth/logout', {}, getAuthHeader(token));
  return response.data;
}

export async function reactivateAccount(token) {
  const response = await apiClient.post('/auth/reactivate', { token });
  return response.data;
}

// ============================================================
// Usuario y 2FA
// ============================================================

export async function getProfile(token) {
  const response = await apiClient.get('/user/profile', getAuthHeader(token));
  return response.data;
}

export async function setup2FA(token) {
  const response = await apiClient.get('/user/2fa/setup', getAuthHeader(token));
  return response.data;
}

export async function enable2FA(token, secret, code) {
  const response = await apiClient.post('/user/2fa/enable', { secret, code }, getAuthHeader(token));
  return response.data;
}

export async function disable2FA(token, code) {
  const response = await apiClient.post('/user/2fa/disable', { code }, getAuthHeader(token));
  return response.data;
}

export async function deleteAccount(token) {
  const response = await apiClient.delete('/user/me', getAuthHeader(token));
  return response.data;
}

export async function updateProfile(token, profileData) {
  const response = await apiClient.put('/user/profile', profileData, getAuthHeader(token));
  return response.data;
}

// ============================================================
// Administrador (Usuarios)
// ============================================================

export async function getAdminUsers(token) {
  const response = await apiClient.get('/admin/users', getAuthHeader(token));
  return response.data;
}

export async function updateUser(token, userId, userData) {
  const response = await apiClient.put(`/admin/users/${userId}`, userData, getAuthHeader(token));
  return response.data;
}

export async function deleteUser(token, userId) {
  const response = await apiClient.delete(`/admin/users/${userId}`, getAuthHeader(token));
  return response.data;
}

export async function getAdminMetrics(token) {
  const response = await apiClient.get('/admin/metrics', getAuthHeader(token));
  return response.data;
}
