package com.kamui.luna.modules.pos;

import com.kamui.luna.core.event.SoldItem;
import com.kamui.luna.core.event.OrderPaidEvent;
import com.kamui.luna.modules.inventory.service.InventoryService;
import com.kamui.luna.modules.loyalty.repository.CustomerRepository;
import com.kamui.luna.modules.menu.entity.Dish;
import com.kamui.luna.modules.menu.repository.DishRepository;
import com.kamui.luna.modules.pos.dto.AddItemRequest;
import com.kamui.luna.modules.pos.dto.OrderResponse;
import com.kamui.luna.modules.pos.entity.Order;
import com.kamui.luna.modules.pos.entity.OrderItem;
import com.kamui.luna.modules.pos.entity.RestaurantTable;
import com.kamui.luna.modules.pos.repository.OrderRepository;
import com.kamui.luna.modules.pos.repository.RestaurantTableRepository;
import com.kamui.luna.modules.pos.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    private OrderRepository orders;
    private RestaurantTableRepository tables;
    private DishRepository dishes;
    private InventoryService inventory;
    private ApplicationEventPublisher events;
    private OrderService service;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepository.class); tables = mock(RestaurantTableRepository.class);
        dishes = mock(DishRepository.class); inventory = mock(InventoryService.class);
        events = mock(ApplicationEventPublisher.class);
        service = new OrderService(orders, events, tables, dishes, inventory, mock(CustomerRepository.class));
    }

    @Test
    void addsItemsUsingActualMenuPrice() {
        RestaurantTable table = table(1L, "FREE");
        Dish dish = new Dish(); dish.setId(5L); dish.setName("Паста");
        dish.setPrice(new BigDecimal("650.00")); dish.setAvailable(true);
        when(tables.findById(1L)).thenReturn(Optional.of(table));
        when(orders.findByTableIdAndStatus(1L, "OPEN")).thenReturn(Optional.empty());
        when(dishes.findById(5L)).thenReturn(Optional.of(dish));
        when(orders.save(any(Order.class))).thenAnswer(invocation -> {
            Order value = invocation.getArgument(0); value.setId(10L); return value;
        });

        OrderResponse response = service.addItemsToTable(1L, 7L, List.of(new AddItemRequest(5L, 2)));

        assertThat(response.totalAmount()).isEqualByComparingTo("1300.00");
        assertThat(response.items()).singleElement().extracting(i -> i.quantity()).isEqualTo(2);
        assertThat(table.getStatus()).isEqualTo("OCCUPIED");
    }

    @Test
    void paymentWritesOffQuantityAndFreesTable() {
        RestaurantTable table = table(1L, "OCCUPIED");
        Order order = new Order(); order.setId(10L); order.setTableId(1L); order.setWaiterId(7L);
        OrderItem item = new OrderItem(); item.setDishId(5L); item.setQuantity(3); item.setPrice(new BigDecimal("100"));
        order.addItem(item);
        when(orders.findByIdForUpdate(10L)).thenReturn(Optional.of(order));
        when(tables.findById(1L)).thenReturn(Optional.of(table));
        when(dishes.findById(5L)).thenReturn(Optional.empty());

        OrderResponse response = service.payOrder(10L, null);

        verify(inventory).writeOffIngredientsForItems(List.of(new SoldItem(5L, 3)));
        verify(events).publishEvent(any(OrderPaidEvent.class));
        assertThat(response.status()).isEqualTo("PAID");
        assertThat(table.getStatus()).isEqualTo("FREE");
    }

    private RestaurantTable table(Long id, String status) {
        RestaurantTable table = new RestaurantTable(); table.setId(id); table.setTableNumber(1);
        table.setCapacity(4); table.setStatus(status); return table;
    }
}
