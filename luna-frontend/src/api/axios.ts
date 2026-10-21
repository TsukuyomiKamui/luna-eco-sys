import axios from 'axios';
import type { JwtResponse } from '../types/api';

const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    headers: { 'Content-Type': 'application/json' },
});

// 1. Перехватчик запросов: прикрепляем Access Token
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// 2. Перехватчик ответов: ловим 401 и делаем Silent Refresh
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;
            const refreshToken = localStorage.getItem('refreshToken');

            if (refreshToken) {
                try {
                    const { data } = await axios.post<JwtResponse>(
                        'http://localhost:8080/api/v1/auth/refresh',
                        { refreshToken }
                    );

                    localStorage.setItem('accessToken', data.token);
                    localStorage.setItem('refreshToken', data.refreshToken);
                    localStorage.setItem('permissions', JSON.stringify(data.permissions));

                    originalRequest.headers.Authorization = `Bearer ${data.token}`;
                    return api(originalRequest);
                } catch (refreshError) {
                    localStorage.clear();
                    window.location.href = '/login';
                }
            }
        }
        return Promise.reject(error);
    }
);

export default api;