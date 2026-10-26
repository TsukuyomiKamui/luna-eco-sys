import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { LoginPage } from './pages/LoginPage';
import { PosTerminalPage } from './pages/PosTerminalPage';
import { InventoryManagerPage } from './pages/InventoryManagerPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { LoyaltyPage } from './pages/LoyaltyPage';

const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
};

export default function App() {
  return (
      <AuthProvider>
        <BrowserRouter>
          <Routes>
              <Route
                  path="/admin/inventory"
                  element={
                      <PrivateRoute>
                          <InventoryManagerPage />
                      </PrivateRoute>
                  }
              />

            <Route path="/login" element={<LoginPage />} />
            <Route
                path="/pos"
                element={
                  <PrivateRoute>
                    <PosTerminalPage />
                  </PrivateRoute>
                }
            />

            <Route path="*" element={<Navigate to="/pos" />} />

              <Route
                  path="/admin/analytics"
                  element={
                      <PrivateRoute>
                          <AnalyticsPage />
                      </PrivateRoute>
                  }
              />

              <Route
                  path="/admin/loyalty"
                  element={
                      <PrivateRoute>
                          <LoyaltyPage />
                      </PrivateRoute>
                  }
              />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
  );
}