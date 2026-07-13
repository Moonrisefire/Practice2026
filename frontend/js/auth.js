const ACCESS_KEY = 'accessToken';
const REFRESH_KEY = 'refreshToken';

function getApiBase() {
  return localStorage.getItem('apiBase') || 'http://localhost:8080';
}

function decodeJwt(token) {
  try {
    const payload = token.split('.')[1];
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function getAccessToken() {
  return localStorage.getItem(ACCESS_KEY);
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_KEY);
}

function saveTokens(accessToken, refreshToken) {
  localStorage.setItem(ACCESS_KEY, accessToken);
  localStorage.setItem(REFRESH_KEY, refreshToken);
}

export function isAuthenticated() {
  return !!getAccessToken();
}

export function getUsername() {
  const token = getAccessToken();
  if (!token) return null;
  const payload = decodeJwt(token);
  return payload?.sub || null;
}

export function getRole() {
  const token = getAccessToken();
  if (!token) return null;
  const payload = decodeJwt(token);
  return payload?.role || null;
}

async function authFetch(path, body) {
  const res = await fetch(`${getApiBase()}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const data = await res.json();
  if (!res.ok) {
    const err = new Error(data.error || `Ошибка ${res.status}`);
    err.details = data.details;
    throw err;
  }
  return data;
}

export async function login(username, password) {
  const data = await authFetch('/api/auth/login', { username, password });
  saveTokens(data.accessToken, data.refreshToken);
  return data;
}

export async function refreshTokens() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error('No refresh token');
  }
  const data = await authFetch('/api/auth/refresh', { refreshToken });
  saveTokens(data.accessToken, data.refreshToken);
  return data;
}

export function logout() {
  localStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
  window.location.hash = 'login';
  window.location.reload();
}
