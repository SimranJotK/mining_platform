import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authApi } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored = localStorage.getItem('user');
    const token = localStorage.getItem('accessToken');
    if (stored && token) {
      setUser(JSON.parse(stored));
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (email, password, mfaCode) => {
    const { data } = await authApi.login({ email, password, mfaCode });
    const result = data.data;
    if (result.mfaRequired) return { mfaRequired: true };
    localStorage.setItem('accessToken', result.accessToken);
    localStorage.setItem('refreshToken', result.refreshToken);
    localStorage.setItem('user', JSON.stringify(result.user));
    setUser(result.user);
    return result;
  }, []);

  const register = useCallback(async (formData) => {
    const { data } = await authApi.register(formData);
    const result = data.data;
    localStorage.setItem('accessToken', result.accessToken);
    localStorage.setItem('refreshToken', result.refreshToken);
    localStorage.setItem('user', JSON.stringify(result.user));
    setUser(result.user);
    return result;
  }, []);

  const logout = useCallback(async () => {
    try { await authApi.logout(); } catch { /* ignore */ }
    localStorage.clear();
    setUser(null);
  }, []);

  const hasRole = useCallback((role) => {
    return user?.roles?.includes(role) || false;
  }, [user]);

  const isAdmin = hasRole('ROLE_ADMIN') || hasRole('ROLE_CREATOR');
  const isCreator = hasRole('ROLE_CREATOR');

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, hasRole, isAdmin, isCreator }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
