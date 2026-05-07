import apiClient from './apiClient';

/**
 * newsletterApi.js — Gestión de suscripciones.
 */

export async function subscribe(email) {
  const response = await apiClient.post('/newsletter/subscribe', { email });
  return response.data;
}

export async function confirmSubscription(token) {
  const response = await apiClient.post('/newsletter/confirm', { token });
  return response.data;
}
