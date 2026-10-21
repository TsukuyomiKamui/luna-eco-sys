import React, { createContext, useContext, useState } from 'react';
import type { JwtResponse } from '../types/api';

interface AuthContextType {
    isAuthenticated: boolean;
    permissions: string[];
    login: (data: JwtResponse) => void;
    logout: () => void;
    hasPermission: (perm: string) => boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [token, setToken] = useState<string | null>(localStorage.getItem('accessToken'));
    const [permissions, setPermissions] = useState<string[]>(() => {
        const saved = localStorage.getItem('permissions');
        return saved ? JSON.parse(saved) : [];
    });

    const login = (data: JwtResponse) => {
        localStorage.setItem('accessToken', data.token);
        localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('permissions', JSON.stringify(data.permissions));
        setToken(data.token);
        setPermissions(data.permissions);
    };

    const logout = () => {
        localStorage.clear();
        setToken(null);
        setPermissions([]);
    };

    const hasPermission = (perm: string) => permissions.includes(perm);

    return (
        <AuthContext.Provider value={{ isAuthenticated: !!token, permissions, login, logout, hasPermission }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
};