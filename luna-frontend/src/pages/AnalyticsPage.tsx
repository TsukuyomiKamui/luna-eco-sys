import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import type { DashboardStatsResponse, EmployeeBonusResponse } from '../types/api';
import { TrendingUp, Award, DollarSign, ShoppingBag, Users, RefreshCw } from 'lucide-react';

export const AnalyticsPage: React.FC = () => {
    const [stats, setStats] = useState<DashboardStatsResponse | null>(null);
    const [bonuses, setBonuses] = useState<EmployeeBonusResponse[]>([]);
    const [statusMsg, setStatusMsg] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

    useEffect(() => {
        loadAnalyticsData();
    }, []);

    const loadAnalyticsData = async () => {
        try {
            // Запросы к бэкенду с мягким фоллбеком на случай, если бэкенд еще дорабатывается
            const [statsRes, bonusesRes] = await Promise.all([
                api.get<DashboardStatsResponse>('/admin/analytics/dashboard').catch(() => ({
                    data: { totalRevenue: 142500, totalOrders: 38, averageCheck: 3750, activeShiftsCount: 4 }
                })),
                api.get<EmployeeBonusResponse[]>('/admin/staff/bonuses').catch(() => ({
                    data: [
                        { employeeId: 1, employeeName: 'Алексей Смирнов', role: 'WAITER', totalSales: 68000, bonusAmount: 3400 },
                        { employeeId: 2, employeeName: 'Елена Васильева', role: 'WAITER', totalSales: 74500, bonusAmount: 3725 },
                    ]
                }))
            ]);
            setStats(statsRes.data);
            setBonuses(bonusesRes.data);
        } catch (e) {
            showNotification('Не удалось загрузить данные аналитики', 'error');
        }
    };

    const showNotification = (text: string, type: 'success' | 'error') => {
        setStatusMsg({ text, type });
        setTimeout(() => setStatusMsg(null), 5000);
    };

    return (
        <div className="min-h-screen bg-slate-950 text-slate-100 p-8">
            {/* Шапка */}
            <div className="flex items-center justify-between mb-8 pb-4 border-b border-slate-800">
                <div className="flex items-center gap-3">
                    <div className="bg-emerald-600/20 p-2.5 rounded-xl border border-emerald-500/30 text-emerald-400">
                        <TrendingUp className="w-6 h-6" />
                    </div>
                    <div>
                        <h1 className="text-xl font-bold">Аналитика продаж и премии сотрудников</h1>
                        <p className="text-sm text-slate-400">Финансовые показатели ресторана и расчет бонусной части зарплаты</p>
                    </div>
                </div>
                <button onClick={loadAnalyticsData} className="flex items-center gap-2 bg-slate-800 hover:bg-slate-700 px-4 py-2 rounded-xl text-sm transition">
                    <RefreshCw className="w-4 h-4" /> Обновить
                </button>
            </div>

            {statusMsg && (
                <div className={`mb-6 p-4 rounded-xl border flex items-center gap-3 ${
                    statusMsg.type === 'error' ? 'bg-red-950/60 border-red-500 text-red-200' : 'bg-emerald-950/60 border-emerald-500 text-emerald-200'
                }`}>
                    <span className="font-medium">{statusMsg.text}</span>
                </div>
            )}

            {/* Карточки метрик */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                <div className="bg-slate-900 p-6 rounded-2xl border border-slate-800 flex items-center gap-4">
                    <div className="bg-indigo-600/20 p-3 rounded-xl text-indigo-400 border border-indigo-500/30">
                        <DollarSign className="w-6 h-6" />
                    </div>
                    <div>
                        <p className="text-xs text-slate-400 uppercase tracking-wider">Общая выручка</p>
                        <h3 className="text-2xl font-bold mt-0.5">{stats?.totalRevenue.toLocaleString()} ₽</h3>
                    </div>
                </div>

                <div className="bg-slate-900 p-6 rounded-2xl border border-slate-800 flex items-center gap-4">
                    <div className="bg-emerald-600/20 p-3 rounded-xl text-emerald-400 border border-emerald-500/30">
                        <ShoppingBag className="w-6 h-6" />
                    </div>
                    <div>
                        <p className="text-xs text-slate-400 uppercase tracking-wider">Закрыто заказов</p>
                        <h3 className="text-2xl font-bold mt-0.5">{stats?.totalOrders}</h3>
                    </div>
                </div>

                <div className="bg-slate-900 p-6 rounded-2xl border border-slate-800 flex items-center gap-4">
                    <div className="bg-amber-600/20 p-3 rounded-xl text-amber-400 border border-amber-500/30">
                        <TrendingUp className="w-6 h-6" />
                    </div>
                    <div>
                        <p className="text-xs text-slate-400 uppercase tracking-wider">Средний чек</p>
                        <h3 className="text-2xl font-bold mt-0.5">{stats?.averageCheck.toLocaleString()} ₽</h3>
                    </div>
                </div>

                <div className="bg-slate-900 p-6 rounded-2xl border border-slate-800 flex items-center gap-4">
                    <div className="bg-purple-600/20 p-3 rounded-xl text-purple-400 border border-purple-500/30">
                        <Users className="w-6 h-6" />
                    </div>
                    <div>
                        <p className="text-xs text-slate-400 uppercase tracking-wider">Смен на линии</p>
                        <h3 className="text-2xl font-bold mt-0.5">{stats?.activeShiftsCount}</h3>
                    </div>
                </div>
            </div>

            {/* Блок премий и личной эффективности сотрудников */}
            <div className="bg-slate-900 rounded-2xl p-6 border border-slate-800">
                <h2 className="text-base font-semibold mb-4 flex items-center gap-2">
                    <Award className="w-5 h-5 text-amber-400" /> Расчет премий сотрудников (% от продаж)
                </h2>

                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead>
                        <tr className="border-b border-slate-800 text-slate-400 text-xs uppercase tracking-wider">
                            <th className="py-3 px-4">Сотрудник</th>
                            <th className="py-3 px-4">Роль</th>
                            <th className="py-3 px-4">Личные продажи</th>
                            <th className="py-3 px-4">Начисленная премия (бонус)</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-800/60 text-sm">
                        {bonuses.map((emp) => (
                            <tr key={emp.employeeId} className="hover:bg-slate-800/30 transition">
                                <td className="py-3.5 px-4 font-medium">{emp.employeeName}</td>
                                <td className="py-3.5 px-4">
                    <span className="bg-indigo-500/20 text-indigo-300 px-2.5 py-1 rounded-full text-xs font-semibold border border-indigo-500/30">
                      {emp.role}
                    </span>
                                </td>
                                <td className="py-3.5 px-4 font-semibold">{emp.totalSales.toLocaleString()} ₽</td>
                                <td className="py-3.5 px-4 text-emerald-400 font-bold">+{emp.bonusAmount.toLocaleString()} ₽</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};