import apiClient from './apiClient';

/**
 * newsletterApi.js — Gestión de suscripciones.
 */

export async function subscribe(email) {
  const response = await apiClient.post('/newsletter/subscribe', { email });
  return response.data;
}

export async function confirmSubscription(token) {
  // Se envía como query param según espera el backend
  const response = await apiClient.post(`/newsletter/confirm?token=${token}`);
  return response.data;
}

export async function broadcastNewsletter(token, broadcastData) {
  const response = await apiClient.post(
    '/newsletter/admin/broadcast', 
    broadcastData, 
    {
      headers: { Authorization: `Bearer ${token}` }
    }
  );
  return response.data;
}

export async function unsubscribeNewsletter(token) {
  const response = await apiClient.post(`/newsletter/unsubscribe?token=${token}`);
  return response.data;
}