import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import type { DishResponse, RestaurantTable, AddItemRequest } from '../types/api';
import { useAuth } from '../context/AuthContext';
import { LogOut, Clock, Plus, CheckCircle, AlertTriangle, UtensilsCrossed } from 'lucide-react';

export const PosTerminalPage: React.FC = () => {
    const { logout } = useAuth();
    const [tables, setTables] = useState<RestaurantTable[]>([]);
    const [dishes, setDishes] = useState<DishResponse[]>([]);
    const [selectedTable, setSelectedTable] = useState<RestaurantTable | null>(null);
    const [cart, setCart] = useState<{ dish: DishResponse; quantity: number }[]>([]);
    const [activeOrderId, setActiveOrderId] = useState<number | null>(null);
    const [statusMsg, setStatusMsg] = useState<{ text: string; type: 'info' | 'error' } | null>(null);

    useEffect(() => {
        loadInitialData();
    }, []);

    const loadInitialData = async () => {
        try {
            const [menuRes, tablesRes] = await Promise.all([
                api.get<DishResponse[]>('/menu'),
                api.get<RestaurantTable[]>('/admin/pos/tables').catch(() => ({ data: [
                        { id: 1, tableNumber: 1, capacity: 4, status: 'FREE' as const },
                        { id: 2, tableNumber: 2, capacity: 2, status: 'FREE' as const },
                        { id: 3, tableNumber: 3, capacity: 6, status: 'OCCUPIED' as const },
                    ]}))
            ]);
            setDishes(menuRes.data);
            setTables(tablesRes.data);
        } catch (e) {
            showNotification('Не удалось загрузить справочники', 'error');
        }
    };

    const showNotification = (text: string, type: 'info' | 'error' = 'info') => {
        setStatusMsg({ text, type });
        setTimeout(() => setStatusMsg(null), 5000);
    };

    // Открытие / закрытие смены (модуль staff)
    const handleClockIn = async () => {
        try {
            const res = await api.post('/staff/shifts/clock-in');
            showNotification(res.data, 'info');
        } catch (err: any) {
            showNotification(err.response?.data?.message || 'Ошибка открытия смены', 'error');
        }
    };

    const handleClockOut = async () => {
        try {
            const res = await api.post('/staff/shifts/clock-out');
            showNotification(res.data, 'info');
        } catch (err: any) {
            showNotification(err.response?.data?.message || 'Ошибка закрытия смены', 'error');
        }
    };

    // Добавление блюда в локальный пречек
    const addToCart = (dish: DishResponse) => {
        setCart((prev) => {
            const existing = prev.find((i) => i.dish.id === dish.id);
            if (existing) {
                return prev.map((i) => i.dish.id === dish.id ? { ...i, quantity: i.quantity + 1 } : i);
            }
            return [...prev, { dish, quantity: 1 }];
        });
    };

    // Отправка блюд на стол (WaiterController)
    const handleSendToKitchen = async () => {
        if (!selectedTable || cart.length === 0) return;
        const payload: AddItemRequest[] = cart.map((i) => ({
            dishId: i.dish.id,
            quantity: i.quantity,
        }));

        try {
            const res = await api.post(`/staff/pos/tables/${selectedTable.id}/items`, payload);
            showNotification('Блюда отправлены на кухню!', 'info');
            setCart([]);
            if (res.data?.id) setActiveOrderId(res.data.id);
            loadInitialData();
        } catch (err: any) {
            showNotification(err.response?.data?.message || 'Ошибка добавления блюд', 'error');
        }
    };

    // Оплата заказа и запуск FIFO списания (OrderController)
    const handlePayOrder = async () => {
        const orderIdToPay = activeOrderId || Number(prompt('Введите ID открытого заказа для оплаты:'));
        if (!orderIdToPay) return;

        try {
            const res = await api.post(`/pos/orders/${orderIdToPay}/pay`);
            showNotification(typeof res.data === 'string' ? res.data : 'Заказ успешно оплачен!', 'info');
            setActiveOrderId(null);
            loadInitialData();
        } catch (err: any) {
            // Здесь сработает твой жесткий блок InsufficientStockException!
            showNotification(err.response?.data?.message || 'Ошибка оплаты (Проверьте остатки на складе!)', 'error');
        }
    };

    const totalCartPrice = cart.reduce((sum, item) => sum + item.dish.price * item.quantity, 0);

    return (
        <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
            {/* Верхняя панель */}
            <header className="bg-slate-900 border-b border-slate-800 px-6 py-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <UtensilsCrossed className="text-indigo-500 w-6 h-6" />
                    <span className="font-bold text-lg tracking-wider">LUNA TERMINAL</span>
                </div>

                <div className="flex items-center gap-3">
                    <button onClick={handleClockIn} className="flex items-center gap-2 bg-emerald-600/20 text-emerald-400 border border-emerald-600/40 px-3 py-1.5 rounded-lg text-sm hover:bg-emerald-600/30">
                        <Clock className="w-4 h-4" /> Открыть смену
                    </button>
                    <button onClick={handleClockOut} className="flex items-center gap-2 bg-amber-600/20 text-amber-400 border border-amber-600/40 px-3 py-1.5 rounded-lg text-sm hover:bg-amber-600/30">
                        <Clock className="w-4 h-4" /> Закрыть смену
                    </button>
                    <button onClick={logout} className="flex items-center gap-2 bg-slate-800 hover:bg-slate-700 px-3 py-1.5 rounded-lg text-sm text-slate-300">
                        <LogOut className="w-4 h-4" /> Выйти
                    </button>
                </div>
            </header>

            {/* Уведомления и ошибки склада */}
            {statusMsg && (
                <div className={`mx-6 mt-4 p-4 rounded-xl border flex items-center gap-3 ${
                    statusMsg.type === 'error' ? 'bg-red-950/60 border-red-500 text-red-200' : 'bg-indigo-950/60 border-indigo-500 text-indigo-200'
                }`}>
                    <AlertTriangle className="w-5 h-5 shrink-0" />
                    <span className="font-medium">{statusMsg.text}</span>
                </div>
            )}

            {/* Основная рабочая сетка */}
            <div className="flex-1 grid grid-cols-12 gap-6 p-6">

                {/* Колонка 1: Сетка столов (3 колонки) */}
                <div className="col-span-3 bg-slate-900 rounded-2xl p-4 border border-slate-800">
                    <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400 mb-4">Столы зала</h2>
                    <div className="grid grid-cols-2 gap-3">
                        {tables.map((table) => (
                            <button
                                key={table.id}
                                onClick={() => setSelectedTable(table)}
                                className={`p-4 rounded-xl border text-left transition ${
                                    selectedTable?.id === table.id
                                        ? 'border-indigo-500 bg-indigo-600/20'
                                        : table.status === 'OCCUPIED'
                                            ? 'border-rose-500/40 bg-rose-950/20'
                                            : 'border-emerald-500/40 bg-emerald-950/20'
                                }`}
                            >
                                <div className="text-lg font-bold">Стол #{table.tableNumber}</div>
                                <div className="text-xs text-slate-400">Мест: {table.capacity}</div>
                                <div className={`text-xs mt-2 font-semibold ${table.status === 'OCCUPIED' ? 'text-rose-400' : 'text-emerald-400'}`}>
                                    {table.status === 'OCCUPIED' ? 'ЗАНЯТ' : 'СВОБОДЕН'}
                                </div>
                            </button>
                        ))}
                    </div>
                </div>

                {/* Колонка 2: Меню блюд (5 колонок) */}
                <div className="col-span-5 bg-slate-900 rounded-2xl p-4 border border-slate-800">
                    <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400 mb-4">Активное меню</h2>
                    <div className="grid grid-cols-2 gap-3">
                        {dishes.map((dish) => (
                            <div
                                key={dish.id}
                                onClick={() => selectedTable && addToCart(dish)}
                                className={`p-4 rounded-xl bg-slate-800/60 border border-slate-700/60 hover:border-indigo-500 transition cursor-pointer flex flex-col justify-between ${
                                    !selectedTable ? 'opacity-50 cursor-not-allowed' : ''
                                }`}
                            >
                                <div>
                                    <span className="text-xs text-indigo-400 font-medium">{dish.categoryName}</span>
                                    <h3 className="font-semibold text-base mt-0.5">{dish.name}</h3>
                                </div>
                                <div className="flex items-center justify-between mt-4">
                                    <span className="font-bold text-emerald-400">{dish.price} ₽</span>
                                    <Plus className="w-5 h-5 text-slate-400" />
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Колонка 3: Чек выбранного стола (4 колонки) */}
                <div className="col-span-4 bg-slate-900 rounded-2xl p-4 border border-slate-800 flex flex-col justify-between">
                    <div>
                        <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400 mb-4">
                            {selectedTable ? `Заказ: Стол #${selectedTable.tableNumber}` : 'Выберите стол слева'}
                        </h2>

                        {cart.length === 0 ? (
                            <p className="text-slate-500 text-sm py-12 text-center">Пречек пуст. Нажмите на блюда в меню.</p>
                        ) : (
                            <div className="space-y-2">
                                {cart.map((item) => (
                                    <div key={item.dish.id} className="flex justify-between items-center bg-slate-800/50 p-3 rounded-lg">
                                        <div>
                                            <div className="font-medium">{item.dish.name}</div>
                                            <div className="text-xs text-slate-400">{item.quantity} шт. × {item.dish.price} ₽</div>
                                        </div>
                                        <div className="font-bold">{item.quantity * item.dish.price} ₽</div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    <div className="border-t border-slate-800 pt-4 mt-4 space-y-3">
                        <div className="flex justify-between text-lg font-bold">
                            <span>Итого к добавлению:</span>
                            <span className="text-indigo-400">{totalCartPrice} ₽</span>
                        </div>

                        <button
                            onClick={handleSendToKitchen}
                            disabled={!selectedTable || cart.length === 0}
                            className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-800 disabled:text-slate-600 py-3 rounded-xl font-semibold transition"
                        >
                            Отправить на кухню (В чек)
                        </button>

                        <button
                            onClick={handlePayOrder}
                            disabled={!selectedTable}
                            className="w-full bg-emerald-600 hover:bg-emerald-500 disabled:bg-slate-800 disabled:text-slate-600 py-3 rounded-xl font-semibold flex items-center justify-center gap-2 transition"
                        >
                            <CheckCircle className="w-5 h-5" /> Оплатить и закрыть счет
                        </button>
                    </div>
                </div>

            </div>
        </div>
    );
};