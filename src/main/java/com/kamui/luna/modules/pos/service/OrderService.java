package com.kamui.luna.modules.pos.service;

import com.kamui.luna.core.event.OrderPaidEvent;
import com.kamui.luna.core.event.SoldItem;
import com.kamui.luna.core.exception.BusinessException;
import com.kamui.luna.core.exception.NotFoundException;
import com.kamui.luna.modules.pos.dto.AddItemRequest;
import com.kamui.luna.modules.pos.dto.OrderItemResponse;
import com.kamui.luna.modules.pos.dto.OrderResponse;
import com.kamui.luna.modules.pos.entity.Order;
import com.kamui.luna.modules.pos.entity.OrderItem;
import com.kamui.luna.modules.pos.entity.RestaurantTable;
import com.kamui.luna.modules.pos.repository.OrderRepository;
import com.kamui.luna.modules.pos.repository.RestaurantTableRepository;
import com.kamui.luna.modules.menu.entity.Dish;
import com.kamui.luna.modules.menu.repository.DishRepository;
import com.kamui.luna.modules.inventory.service.InventoryService;
import com.kamui.luna.modules.loyalty.repository.CustomerRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RestaurantTableRepository tableRepository;
    private final DishRepository dishRepository;
    private final InventoryService inventoryService;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, ApplicationEventPublisher eventPublisher,
                        RestaurantTableRepository tableRepository, DishRepository dishRepository,
                        InventoryService inventoryService, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.tableRepository = tableRepository;
        this.dishRepository = dishRepository;
        this.inventoryService = inventoryService;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public OrderResponse addItemsToTable(Long tableId, Long waiterId, List<AddItemRequest> requestedItems) {
        if (requestedItems == null || requestedItems.isEmpty()) throw new BusinessException("Список блюд пуст");
        // 1. Проверяем, существует ли стол
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Стол не найден"));

        // 2. Ищем открытый заказ или создаем новый
        Order order = orderRepository.findByTableIdAndStatus(tableId, "OPEN")
                .orElseGet(() -> {
                    Order newOrder = new Order();
                    newOrder.setTableId(tableId);
                    newOrder.setWaiterId(waiterId);
                    // Переводим стол в статус "ЗАНЯТ"
                    table.setStatus("OCCUPIED");
                    tableRepository.save(table);
                    return newOrder;
                });
        if (!order.getWaiterId().equals(waiterId))
            throw new BusinessException("Заказ этого стола закреплён за другим официантом");

        for (AddItemRequest req : requestedItems) {
            Dish dish = dishRepository.findById(req.dishId())
                    .orElseThrow(() -> new NotFoundException("Блюдо ID " + req.dishId() + " не найдено"));
            if (!dish.isAvailable()) throw new BusinessException("Блюдо «" + dish.getName() + "» находится в стоп-листе");
            OrderItem item = new OrderItem();
            item.setDishId(req.dishId());
            item.setQuantity(req.quantity());
            item.setPrice(dish.getPrice());
            order.addItem(item);
        }
        return toResponse(orderRepository.save(order));
    }
    /**
     * Оплата заказа и генерация системного события
     */
    @Transactional
    public OrderResponse payOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        if (!"OPEN".equals(order.getStatus())) {
            throw new BusinessException("Заказ уже оплачен или отменен");
        }

        if (order.getItems().isEmpty()) throw new BusinessException("Нельзя оплатить пустой заказ");
        if (customerId != null && !customerRepository.existsById(customerId))
            throw new NotFoundException("Профиль гостя не найден");
        order.setCustomerId(customerId);

        List<SoldItem> soldItems = order.getItems().stream()
                .map(item -> new SoldItem(item.getDishId(), item.getQuantity())).toList();

        // Склад списывается в той же транзакции: при нехватке заказ не будет оплачен.
        inventoryService.writeOffIngredientsForItems(soldItems);

        // 1. Меняем статус заказа на ОПЛАЧЕНО
        order.setStatus("PAID");
        orderRepository.save(order);
        RestaurantTable table = tableRepository.findById(order.getTableId())
                .orElseThrow(() -> new NotFoundException("Стол не найден"));
        table.setStatus("FREE");
        tableRepository.save(table);

        // 3. ПУБЛИКУЕМ СОБЫТИЕ
        // Теперь Склад, Лояльность, HR и Аналитика поймают его и сделают свою работу!
        OrderPaidEvent event = new OrderPaidEvent(
                order.getId(),
                order.getTableId(),
                order.getWaiterId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                soldItems
        );

        eventPublisher.publishEvent(event);
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return toResponse(orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Заказ не найден")));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(String status) {
        if (status != null && !List.of("OPEN", "PAID", "CANCELLED").contains(status.toUpperCase()))
            throw new BusinessException("Неизвестный статус заказа: " + status);
        List<Order> orders = status == null ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase());
        return orders.stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        Order order = orderRepository.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Заказ не найден"));
        if (!"OPEN".equals(order.getStatus())) throw new BusinessException("Можно отменить только открытый заказ");
        order.setStatus("CANCELLED");
        RestaurantTable table = tableRepository.findById(order.getTableId())
                .orElseThrow(() -> new NotFoundException("Стол не найден"));
        table.setStatus("FREE"); tableRepository.save(table);
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream().map(item -> {
            String name = dishRepository.findById(item.getDishId()).map(Dish::getName).orElse("Удалённое блюдо");
            BigDecimal amount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new OrderItemResponse(item.getId(), item.getDishId(), name, item.getQuantity(), item.getPrice(), amount);
        }).toList();
        return new OrderResponse(order.getId(), order.getTableId(), order.getWaiterId(), order.getCustomerId(),
                order.getStatus(), order.getTotalAmount(), order.getCreatedAt(), items);
    }
}
