import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import type { JwtResponse } from '../types/api';
import { Utensils } from 'lucide-react';

export const LoginPage: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        try {
            const response = await api.post<JwtResponse>('/auth/login', { username, password });
            login(response.data);
            navigate('/pos');
        } catch (err: any) {
            setError(err.response?.data?.message || 'Ошибка авторизации. Проверьте логин и пароль.');
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-900 text-white">
            <form onSubmit={handleSubmit} className="bg-slate-800 p-8 rounded-2xl shadow-xl w-96 border border-slate-700">
                <div className="flex flex-col items-center mb-6">
                    <div className="bg-indigo-600 p-3 rounded-full mb-3">
                        <Utensils className="w-8 h-8 text-white" />
                    </div>
                    <h1 className="text-2xl font-bold tracking-wide">LUNA POS</h1>
                    <p className="text-slate-400 text-sm">Вход в экосистему ресторана</p>
                </div>

                {error && <div className="bg-red-500/20 border border-red-500 text-red-300 p-3 rounded-lg text-sm mb-4">{error}</div>}

                <div className="mb-4">
                    <label className="block text-sm text-slate-400 mb-1">Имя пользователя</label>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="w-full p-2.5 rounded-lg bg-slate-900 border border-slate-700 focus:border-indigo-500 focus:outline-none"
                        required
                    />
                </div>

                <div className="mb-6">
                    <label className="block text-sm text-slate-400 mb-1">Пароль</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="w-full p-2.5 rounded-lg bg-slate-900 border border-slate-700 focus:border-indigo-500 focus:outline-none"
                        required
                    />
                </div>

                <button
                    type="submit"
                    className="w-full bg-indigo-600 hover:bg-indigo-500 transition font-semibold py-3 rounded-lg shadow-lg"
                >
                    Войти в терминал
                </button>
            </form>
        </div>
    );
};