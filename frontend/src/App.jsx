import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import WorkersPage from './pages/WorkersPage';
import StatisticsPage from './pages/StatisticsPage';
import AiAnalyticsPage from './pages/AiAnalyticsPage';
import SecurityPage from './pages/SecurityPage';
import ProfilePage from './pages/ProfilePage';
import NotificationsPage from './pages/NotificationsPage';
import AdminPage from './pages/AdminPage';
import CreatorPage from './pages/CreatorPage';
import AuditLogsPage from './pages/AuditLogsPage';
import SystemMonitoringPage from './pages/SystemMonitoringPage';
import SettingsPage from './pages/SettingsPage';

function AppLayout({ children }) {
  return <Layout>{children}</Layout>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route path="/dashboard" element={
        <ProtectedRoute><AppLayout><DashboardPage /></AppLayout></ProtectedRoute>
      } />
      <Route path="/workers" element={
        <ProtectedRoute><AppLayout><WorkersPage /></AppLayout></ProtectedRoute>
      } />
      <Route path="/statistics" element={
        <ProtectedRoute><AppLayout><StatisticsPage /></AppLayout></ProtectedRoute>
      } />
      <Route path="/ai-analytics" element={
        <ProtectedRoute><AppLayout><AiAnalyticsPage /></AppLayout></ProtectedRoute>
      } />
      <Route path="/security" element={
        <ProtectedRoute><AppLayout><SecurityPage /></AppLayout></ProtectedRoute>
      } />
      <Route path="/profile" element={
        <ProtectedRoute><AppLayout><ProfilePage /></AppLayout></ProtectedRoute>
      } />
      <Route path="/notifications" element={
        <ProtectedRoute><AppLayout><NotificationsPage /></AppLayout></ProtectedRoute>
      } />
      <Route path="/settings" element={
        <ProtectedRoute><AppLayout><SettingsPage /></AppLayout></ProtectedRoute>
      } />
      <Route path="/admin" element={
        <ProtectedRoute roles={['ROLE_ADMIN', 'ROLE_CREATOR']}>
          <AppLayout><AdminPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/audit-logs" element={
        <ProtectedRoute roles={['ROLE_ADMIN', 'ROLE_CREATOR']}>
          <AppLayout><AuditLogsPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/creator" element={
        <ProtectedRoute roles={['ROLE_CREATOR']}>
          <AppLayout><CreatorPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/system-monitoring" element={
        <ProtectedRoute roles={['ROLE_CREATOR']}>
          <AppLayout><SystemMonitoringPage /></AppLayout>
        </ProtectedRoute>
      } />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
