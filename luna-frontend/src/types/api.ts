
export interface JwtResponse {
    token: string;
    refreshToken: string;
    permissions: string[];
}

export interface DishResponse {
    id: number;
    name: string;
    categoryName: string;
    description: string;
    price: number;
    isAvailable: boolean;
    imageUrl?: string;
}

export interface RestaurantTable {
    id: number;
    tableNumber: number;
    capacity: number;
    status: 'FREE' | 'OCCUPIED' | 'RESERVED';
}

export interface AddItemRequest {
    dishId: number;
    quantity: number;
}

export interface OrderItemResponse {
    id: number;
    dishId: number;
    dishName?: string;
    quantity: number;
    price: number;
}

export interface OrderResponse {
    id: number;
    tableId: number;
    waiterId: number;
    customerId?: number;
    status: 'OPEN' | 'PAID' | 'CANCELLED';
    totalAmount: number;
    items: OrderItemResponse[];
}

export interface IngredientResponse {
    id: number;
    name: string;
    unit: string;
    minimumStock: number;
    currentStock: number;
}

export interface BatchRequest {
    ingredientId: number;
    quantity: number;
    costPerUnit: number;
}

export interface RecipeItemRequest {
    ingredientId: number;
    quantity: number;
}

export interface RecipeRequest {
    dishId: number;
    items: RecipeItemRequest[];
}

export interface DashboardStatsResponse {
    totalRevenue: number;
    totalOrders: number;
    averageCheck: number;
    activeShiftsCount: number;
}

export interface EmployeeBonusResponse {
    employeeId: number;
    employeeName: string;
    role: string;
    totalSales: number;
    bonusAmount: number;
}

export interface CustomerResponse {
    id: number;
    name: string;
    phoneNumber: string;
    bonusPoints: number;
    discountPercentage: number;
}

export interface CustomerSearchRequest {
    phoneNumber: string;
}

export interface AdjustBonusesRequest {
    customerId: number;
    points: number; // положительное число для начисления, отрицательное для списания
}