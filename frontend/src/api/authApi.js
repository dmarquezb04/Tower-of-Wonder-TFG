/**
 * authApi.js — Capa de comunicación con el backend Spring Boot
 *
 * Centraliza todas las llamadas a /api/auth/* y /api/user/*
 * Ahora utiliza Axios para mayor consistencia y mejores funcionalidades.
 */

import axios from 'axios';

const API_BASE = '/api';

// Instancia de axios para centralizar configuración si fuera necesario
const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * Helper para añadir el token a las cabeceras
 */
const getAuthHeader = (token) => ({
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// ============================================================
// Autenticación
// ============================================================

export async function login(email, password) {
  const response = await api.post('/auth/login', { email, password });
  return response.data;
}

export async function verifyTwoFactor(tempToken, code) {
  const response = await api.post('/auth/verify-2fa', { code }, getAuthHeader(tempToken));
  return response.data;
}

export async function register(email, username, password) {
  const response = await api.post('/auth/register', { email, username, password });
  return response.data;
}

export async function logout(token) {
  const response = await api.post('/auth/logout', {}, getAuthHeader(token));
  return response.data;
}

// ============================================================
// Usuario y 2FA
// ============================================================

export async function getProfile(token) {
  const response = await api.get('/user/profile', getAuthHeader(token));
  return response.data;
}

export async function setup2FA(token) {
  const response = await api.get('/user/2fa/setup', getAuthHeader(token));
  return response.data;
}

export async function enable2FA(token, secret, code) {
  const response = await api.post('/user/2fa/enable', { secret, code }, getAuthHeader(token));
  return response.data;
}

export async function disable2FA(token, code) {
  const response = await api.post('/user/2fa/disable', { code }, getAuthHeader(token));
  return response.data;
}

export async function deleteAccount(token) {
  const response = await api.delete('/user/me', getAuthHeader(token));
  return response.data;
}

export async function updateProfile(token, profileData) {
  const response = await api.put('/user/profile', profileData, getAuthHeader(token));
  return response.data;
}

// ============================================================
// Administrador (Usuarios)
// ============================================================

export async function getAdminUsers(token) {
  const response = await api.get('/admin/users', getAuthHeader(token));
  return response.data;
}

export async function updateUser(token, userId, userData) {
  const response = await api.put(`/admin/users/${userId}`, userData, getAuthHeader(token));
  return response.data;
}

export async function deleteUser(token, userId) {
  const response = await api.delete(`/admin/users/${userId}`, getAuthHeader(token));
  return response.data;
}

export async function getAdminMetrics(token) {
  const response = await api.get('/admin/metrics', getAuthHeader(token));
  return response.data;
}
