import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { LoginPage } from './pages/LoginPage';
import { PosTerminalPage } from './pages/PosTerminalPage';

const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
};

export default function App() {
  return (
      <AuthProvider>
        <BrowserRouter>
          <Routes>
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
          </Routes>
        </BrowserRouter>
      </AuthProvider>
  );
}