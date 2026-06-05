import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || '/api/v1';

const client = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const { data } = await axios.post(`${API_BASE}/auth/refresh`, { refreshToken });
          if (data.data?.accessToken) {
            localStorage.setItem('accessToken', data.data.accessToken);
            localStorage.setItem('refreshToken', data.data.refreshToken);
            original.headers.Authorization = `Bearer ${data.data.accessToken}`;
            return client(original);
          }
        } catch {
          localStorage.clear();
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  login: (data) => client.post('/auth/login', data),
  register: (data) => client.post('/auth/register', data),
  logout: () => client.post('/auth/logout'),
};

export const dashboardApi = {
  getSummary: () => client.get('/dashboard/summary'),
};

export const miningApi = {
  getWorkers: () => client.get('/mining/workers'),
  createWorker: (data) => client.post('/mining/workers', data),
  getStatistics: (hours = 24) => client.get(`/mining/statistics?hours=${hours}`),
  getPools: () => client.get('/mining/pools'),
};

export const aiApi = {
  getPredictions: () => client.get('/ai/predictions'),
  generatePredictions: () => client.post('/ai/predictions/generate'),
};

export const userApi = {
  getProfile: () => client.get('/users/profile'),
  updateProfile: (data) => client.put('/users/profile', data),
  storeEncryptedData: (data) => client.post('/users/encrypted-data', data),
  retrieveEncryptedData: (data) => client.post('/users/encrypted-data/retrieve', data),
  enableMfa: () => client.post('/users/mfa/enable'),
  confirmMfa: (code) => client.post('/users/mfa/confirm', { code }),
};

export const notificationApi = {
  getAll: () => client.get('/notifications'),
  markAsRead: (id) => client.post(`/notifications/${id}/read`),
  markAllAsRead: () => client.post('/notifications/read-all'),
};

export const adminApi = {
  getUsers: () => client.get('/admin/users'),
  suspendUser: (id) => client.post(`/admin/users/${id}/suspend`),
  activateUser: (id) => client.post(`/admin/users/${id}/activate`),
  getAuditLogs: (page = 0) => client.get(`/admin/audit-logs?page=${page}&size=20`),
  getAnalytics: () => client.get('/admin/analytics'),
};

export const creatorApi = {
  getHealth: () => client.get('/creator/health'),
  getConfigurations: () => client.get('/creator/configurations'),
  updateConfiguration: (key, value) => client.put(`/creator/configurations/${key}`, { value }),
  deploy: (service) => client.post('/creator/deploy', { service }),
};

export const systemApi = {
  getHealth: () => client.get('/system/health'),
};

export default client;
