import { getAccessToken, refreshTokens, logout } from './auth.js';

export function getApiBase() {
  return localStorage.getItem('apiBase') || 'http://localhost:8080';
}

export function setApiBase(url) {
  localStorage.setItem('apiBase', url.replace(/\/$/, ''));
}

export class ApiError extends Error {
  constructor(message, status, details) {
    super(message);
    this.status = status;
    this.details = details;
  }
}

async function parseResponse(res) {
  const text = await res.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export async function api(method, path, body, options = {}) {
  const { auth = true, multipart = false, retry = true } = options;
  const headers = {};

  if (!multipart) {
    headers['Content-Type'] = 'application/json';
  }

  if (auth) {
    const token = getAccessToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
  }

  const fetchOptions = { method, headers };
  if (body !== undefined && body !== null) {
    fetchOptions.body = multipart ? body : JSON.stringify(body);
  }

  const res = await fetch(`${getApiBase()}${path}`, fetchOptions);
  const data = await parseResponse(res);

  if (res.status === 401 && auth && retry) {
    try {
      await refreshTokens();
      return api(method, path, body, { ...options, retry: false });
    } catch {
      logout();
      throw new ApiError('Сессия истекла, войдите снова', 401);
    }
  }

  if (!res.ok) {
    const message = data?.error || `Ошибка ${res.status}`;
    throw new ApiError(message, res.status, data?.details);
  }

  return data;
}

export async function apiPublic(method, path, body) {
  return api(method, path, body, { auth: false });
}
