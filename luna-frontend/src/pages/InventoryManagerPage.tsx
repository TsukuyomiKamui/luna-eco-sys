import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import type { IngredientResponse, DishResponse, BatchRequest } from '../types/api';
import { Package, PlusCircle, ChefHat, AlertOctagon, RefreshCw } from 'lucide-react';

export const InventoryManagerPage: React.FC = () => {
    const [ingredients, setIngredients] = useState<IngredientResponse[]>([]);
    const [dishes, setDishes] = useState<DishResponse[]>([]);

    // Состояние формы оприходования партии
    const [selectedIngredientId, setSelectedIngredientId] = useState<number>(1);
    const [batchQty, setBatchQty] = useState<number>(100);
    const [costPerUnit, setCostPerUnit] = useState<number>(50);
    const [statusMsg, setStatusMsg] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            const [ingRes, dishRes] = await Promise.all([
                api.get<IngredientResponse[]>('/admin/inventory/ingredients').catch(() => ({
                    data: [
                        { id: 1, name: 'Говядина Рибай', unit: 'г', minimumStock: 1000, currentStock: 2500 },
                        { id: 2, name: 'Креветки тигровые', unit: 'г', minimumStock: 500, currentStock: 300 },
                        { id: 3, name: 'Сливки 33%', unit: 'мл', minimumStock: 2000, currentStock: 4500 },
                    ]
                })),
                api.get<DishResponse[]>('/menu')
            ]);
            setIngredients(ingRes.data);
            setDishes(dishRes.data);
            if (ingRes.data.length > 0) setSelectedIngredientId(ingRes.data[0].id);
        } catch (e) {
            showNotification('Не удалось загрузить данные склада', 'error');
        }
    };

    const showNotification = (text: string, type: 'success' | 'error') => {
        setStatusMsg({ text, type });
        setTimeout(() => setStatusMsg(null), 5000);
    };

    // Оприходование партии (FIFO)
    const handleCreateBatch = async (e: React.FormEvent) => {
        e.preventDefault();
        const payload: BatchRequest = {
            ingredientId: Number(selectedIngredientId),
            quantity: Number(batchQty),
            costPerUnit: Number(costPerUnit),
        };

        try {
            await api.post('/admin/inventory/batches', payload);
            showNotification('Партия успешно оприходована на склад!', 'success');
            loadData();
        } catch (err: any) {
            showNotification(err.response?.data?.message || 'Ошибка оприходования партии', 'error');
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 text-slate-100 p-8">
            {/* Шапка */}
            <div className="flex items-center justify-between mb-8 pb-4 border-b border-slate-800">
                <div className="flex items-center gap-3">
                    <div className="bg-indigo-600/20 p-2.5 rounded-xl border border-indigo-500/30 text-indigo-400">
                        <Package className="w-6 h-6" />
                    </div>
                    <div>
                        <h1 className="text-xl font-bold">Панель управляющего: Склад и Техкарты</h1>
                        <p className="text-sm text-slate-400">Контроль остатков, FIFO-партии и привязка ингредиентов к блюдам</p>
                    </div>
                </div>
                <button onClick={loadData} className="flex items-center gap-2 bg-slate-800 hover:bg-slate-700 px-4 py-2 rounded-xl text-sm transition">
                    <RefreshCw className="w-4 h-4" /> Обновить данные
                </button>
            </div>

            {statusMsg && (
                <div className={`mb-6 p-4 rounded-xl border flex items-center gap-3 ${
                    statusMsg.type === 'error' ? 'bg-red-950/60 border-red-500 text-red-200' : 'bg-emerald-950/60 border-emerald-500 text-emerald-200'
                }`}>
                    <span className="font-medium">{statusMsg.text}</span>
                </div>
            )}

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

                {/* Колонка 1: Остатки на складе (2 колонки шириной) */}
                <div className="lg:col-span-2 bg-slate-900 rounded-2xl p-6 border border-slate-800">
                    <h2 className="text-base font-semibold mb-4 flex items-center gap-2">
                        <Package className="w-5 h-5 text-indigo-400" /> Остатки ингредиентов
                    </h2>

                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                            <tr className="border-b border-slate-800 text-slate-400 text-xs uppercase tracking-wider">
                                <th className="py-3 px-4">Ингредиент</th>
                                <th className="py-3 px-4">Ед. изм.</th>
                                <th className="py-3 px-4">Мин. остаток</th>
                                <th className="py-3 px-4">Текущий запас</th>
                                <th className="py-3 px-4">Статус</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-800/60 text-sm">
                            {ingredients.map((ing) => {
                                const isLow = ing.currentStock <= ing.minimumStock;
                                return (
                                    <tr key={ing.id} className="hover:bg-slate-800/30 transition">
                                        <td className="py-3.5 px-4 font-medium">{ing.name}</td>
                                        <td className="py-3.5 px-4 text-slate-400">{ing.unit}</td>
                                        <td className="py-3.5 px-4 text-slate-400">{ing.minimumStock}</td>
                                        <td className="py-3.5 px-4 font-bold">{ing.currentStock}</td>
                                        <td className="py-3.5 px-4">
                                            {isLow ? (
                                                <span className="inline-flex items-center gap-1 bg-rose-500/20 text-rose-400 px-2.5 py-1 rounded-full text-xs font-semibold border border-rose-500/30">
                            <AlertOctagon className="w-3.5 h-3.5" /> Дефицит
                          </span>
                                            ) : (
                                                <span className="inline-flex items-center gap-1 bg-emerald-500/20 text-emerald-400 px-2.5 py-1 rounded-full text-xs font-semibold border border-emerald-500/30">
                            Норма
                          </span>
                                            )}
                                        </td>
                                    </tr>
                                );
                            })}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Колонка 2: Оприходование новой партии (FIFO) */}
                <div className="bg-slate-900 rounded-2xl p-6 border border-slate-800 flex flex-col justify-between">
                    <div>
                        <h2 className="text-base font-semibold mb-4 flex items-center gap-2">
                            <PlusCircle className="w-5 h-5 text-emerald-400" /> Приход партии (FIFO)
                        </h2>
                        <form onSubmit={handleCreateBatch} className="space-y-4">
                            <div>
                                <label className="block text-xs text-slate-400 mb-1 uppercase tracking-wider">Ингредиент</label>
                                <select
                                    value={selectedIngredientId}
                                    onChange={(e) => setSelectedIngredientId(Number(e.target.value))}
                                    className="w-full p-2.5 rounded-xl bg-slate-950 border border-slate-800 focus:border-indigo-500 focus:outline-none text-sm"
                                >
                                    {ingredients.map((ing) => (
                                        <option key={ing.id} value={ing.id}>{ing.name} ({ing.unit})</option>
                                    ))}
                                </select>
                            </div>

                            <div>
                                <label className="block text-xs text-slate-400 mb-1 uppercase tracking-wider">Количество</label>
                                <input
                                    type="number"
                                    value={batchQty}
                                    onChange={(e) => setBatchQty(Number(e.target.value))}
                                    className="w-full p-2.5 rounded-xl bg-slate-950 border border-slate-800 focus:border-indigo-500 focus:outline-none text-sm"
                                    required
                                />
                            </div>

                            <div>
                                <label className="block text-xs text-slate-400 mb-1 uppercase tracking-wider">Себестоимость за ед. (₽)</label>
                                <input
                                    type="number"
                                    step="0.01"
                                    value={costPerUnit}
                                    onChange={(e) => setCostPerUnit(Number(e.target.value))}
                                    className="w-full p-2.5 rounded-xl bg-slate-950 border border-slate-800 focus:border-indigo-500 focus:outline-none text-sm"
                                    required
                                />
                            </div>

                            <button
                                type="submit"
                                className="w-full bg-emerald-600 hover:bg-emerald-500 transition py-3 rounded-xl font-semibold text-sm shadow-lg shadow-emerald-600/20"
                            >
                                Оприходовать партию
                            </button>
                        </form>
                    </div>
                </div>

            </div>

            {/* Ниже: Справочник блюд и техкарт */}
            <div className="mt-8 bg-slate-900 rounded-2xl p-6 border border-slate-800">
                <h2 className="text-base font-semibold mb-4 flex items-center gap-2">
                    <ChefHat className="w-5 h-5 text-amber-400" /> Блюда и привязанные техкарты
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    {dishes.map((dish) => (
                        <div key={dish.id} className="bg-slate-950 p-4 rounded-xl border border-slate-800/80 flex flex-col justify-between">
                            <div>
                                <span className="text-xs text-indigo-400 font-medium">{dish.categoryName}</span>
                                <h3 className="font-bold text-base mt-0.5">{dish.name}</h3>
                                <p className="text-xs text-slate-400 mt-1">{dish.description || 'Тех карта настроена'}</p>
                            </div>
                            <div className="mt-4 pt-3 border-t border-slate-800/60 flex items-center justify-between text-sm">
                                <span className="text-emerald-400 font-bold">{dish.price} ₽</span>
                                <span className="text-xs text-slate-500">ID блюда: {dish.id}</span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};