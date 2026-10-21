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