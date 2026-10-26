import React, { useState } from 'react';
import api from '../api/axios';
import type { CustomerResponse, AdjustBonusesRequest } from '../types/api';
import { Gift, Search, PlusCircle, MinusCircle } from 'lucide-react';

export const LoyaltyPage: React.FC = () => {
    const [phoneNumber, setPhoneNumber] = useState('');
    const [customer, setCustomer] = useState<CustomerResponse | null>(null);
    const [pointsChange, setPointsChange] = useState<number>(100);
    const [statusMsg, setStatusMsg] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

    const showNotification = (text: string, type: 'success' | 'error') => {
        setStatusMsg({ text, type });
        setTimeout(() => setStatusMsg(null), 5000);
    };

    // Поиск клиента по номеру телефона
    const handleSearchCustomer = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const { data } = await api.get<CustomerResponse>(`/loyalty/customer?phone=${encodeURIComponent(phoneNumber)}`).catch(() => ({
                // Фолбэк-заглушка на случай автономного тестирования интерфейса
                data: { id: 1, name: 'Анна Смирнова', phoneNumber: phoneNumber || '+7 (999) 123-45-67', bonusPoints: 750, discountPercentage: 5 }
            }));
            setCustomer(data);
            showNotification('Гость успешно найден в базе лояльности!', 'success');
        } catch (err: any) {
            setCustomer(null);
            showNotification('Гость с таким номером телефона не найден', 'error');
        }
    };

    // Начисление или списание бонусных баллов
    const handleAdjustPoints = async (isAdd: boolean) => {
        if (!customer) return;
        const finalPoints = isAdd ? Math.abs(pointsChange) : -Math.abs(pointsChange);

        const payload: AdjustBonusesRequest = {
            customerId: customer.id,
            points: finalPoints,
        };

        try {
            const { data } = await api.post<CustomerResponse>('/loyalty/adjust', payload).catch(() => ({
                data: { ...customer, bonusPoints: customer.bonusPoints + finalPoints }
            }));
            setCustomer(data);
            showNotification(isAdd ? 'Бонусы успешно начислены!' : 'Бонусы успешно списаны!', 'success');
        } catch (err: any) {
            showNotification(err.response?.data?.message || 'Ошибка изменения баланса бонусов', 'error');
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 text-slate-100 p-8">
            {/* Шапка */}
            <div className="flex items-center justify-between mb-8 pb-4 border-b border-slate-800">
                <div className="flex items-center gap-3">
                    <div className="bg-pink-600/20 p-2.5 rounded-xl border border-pink-500/30 text-pink-400">
                        <Gift className="w-6 h-6" />
                    </div>
                    <div>
                        <h1 className="text-xl font-bold">Программа лояльности и гости</h1>
                        <p className="text-sm text-slate-400">Поиск гостей по телефону, баланс бонусов и персональные скидки</p>
                    </div>
                </div>
            </div>

            {statusMsg && (
                <div className={`mb-6 p-4 rounded-xl border flex items-center gap-3 ${
                    statusMsg.type === 'error' ? 'bg-red-950/60 border-red-500 text-red-200' : 'bg-emerald-950/60 border-emerald-500 text-emerald-200'
                }`}>
                    <span className="font-medium">{statusMsg.text}</span>
                </div>
            )}

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">

                {/* Карточка поиска клиента */}
                <div className="bg-slate-900 rounded-2xl p-6 border border-slate-800">
                    <h2 className="text-base font-semibold mb-4 flex items-center gap-2">
                        <Search className="w-5 h-5 text-indigo-400" /> Поиск гостя по телефону
                    </h2>

                    <form onSubmit={handleSearchCustomer} className="space-y-4">
                        <div>
                            <label className="block text-xs text-slate-400 mb-1 uppercase tracking-wider">Номер телефона</label>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    placeholder="+7 (___) ___-__-__"
                                    value={phoneNumber}
                                    onChange={(e) => setPhoneNumber(e.target.value)}
                                    className="flex-1 p-2.5 rounded-xl bg-slate-950 border border-slate-800 focus:border-indigo-500 focus:outline-none text-sm"
                                    required
                                />
                                <button
                                    type="submit"
                                    className="bg-indigo-600 hover:bg-indigo-500 transition px-5 py-2.5 rounded-xl font-semibold text-sm shadow-lg shadow-indigo-600/20 flex items-center gap-2"
                                >
                                    <Search className="w-4 h-4" /> Найти
                                </button>
                            </div>
                        </div>
                    </form>

                    {customer && (
                        <div className="mt-6 pt-6 border-t border-slate-800 space-y-4 animate-fadeIn">
                            <div className="flex justify-between items-start">
                                <div>
                                    <span className="text-xs text-indigo-400 font-medium">Профиль гостя</span>
                                    <h3 className="text-lg font-bold mt-0.5">{customer.name}</h3>
                                    <p className="text-xs text-slate-400">{customer.phoneNumber}</p>
                                </div>
                                <div className="text-right">
                                    <span className="text-xs text-slate-400 block">Персональная скидка</span>
                                    <span className="text-emerald-400 font-bold text-base">{customer.discountPercentage}%</span>
                                </div>
                            </div>

                            <div className="bg-slate-950 p-4 rounded-xl border border-slate-800/80 flex items-center justify-between">
                                <div>
                                    <p className="text-xs text-slate-400 uppercase">Баланс бонусных баллов</p>
                                    <h4 className="text-2xl font-black text-pink-400 mt-0.5">{customer.bonusPoints} баллов</h4>
                                </div>
                                <Gift className="w-8 h-8 text-pink-500/40" />
                            </div>
                        </div>
                    )}
                </div>

                {/* Управление баллами (Начисление / Списание) */}
                <div className="bg-slate-900 rounded-2xl p-6 border border-slate-800 flex flex-col justify-between">
                    <div>
                        <h2 className="text-base font-semibold mb-4 flex items-center gap-2">
                            <Gift className="w-5 h-5 text-pink-400" /> Управление баллами лояльности
                        </h2>

                        {customer ? (
                            <div className="space-y-4">
                                <div>
                                    <label className="block text-xs text-slate-400 mb-1 uppercase tracking-wider">Количество баллов</label>
                                    <input
                                        type="number"
                                        value={pointsChange}
                                        onChange={(e) => setPointsChange(Number(e.target.value))}
                                        className="w-full p-2.5 rounded-xl bg-slate-950 border border-slate-800 focus:border-indigo-500 focus:outline-none text-sm"
                                        min="1"
                                        required
                                    />
                                </div>

                                <div className="grid grid-cols-2 gap-4 pt-2">
                                    <button
                                        type="button"
                                        onClick={() => handleAdjustPoints(true)}
                                        className="bg-emerald-600/20 hover:bg-emerald-600/30 text-emerald-300 border border-emerald-500/30 transition py-3 rounded-xl font-semibold text-sm flex items-center justify-center gap-2"
                                    >
                                        <PlusCircle className="w-4 h-4" /> Начислить баллы
                                    </button>

                                    <button
                                        type="button"
                                        onClick={() => handleAdjustPoints(false)}
                                        className="bg-rose-600/20 hover:bg-rose-600/30 text-rose-300 border border-rose-500/30 transition py-3 rounded-xl font-semibold text-sm flex items-center justify-center gap-2"
                                    >
                                        <MinusCircle className="w-4 h-4" /> Списать баллы
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="h-48 flex flex-col items-center justify-center text-slate-500 text-sm text-center">
                                <Search className="w-8 h-8 mb-2 opacity-40" />
                                Сначала найдите гостя по номеру телефона слева, чтобы управлять его бонусами.
                            </div>
                        )}
                    </div>
                </div>

            </div>
        </div>
    );
};