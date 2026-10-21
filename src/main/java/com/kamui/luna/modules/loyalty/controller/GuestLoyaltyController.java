package com.kamui.luna.modules.loyalty.controller;

import com.kamui.luna.modules.loyalty.entity.Customer;
import com.kamui.luna.modules.loyalty.repository.CustomerRepository;
import com.kamui.luna.modules.auth.service.CurrentUserService;
import com.kamui.luna.core.exception.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/guest/loyalty")
public class GuestLoyaltyController {

    private final CustomerRepository customerRepository;
    private final CurrentUserService currentUser;

    public GuestLoyaltyController(CustomerRepository customerRepository, CurrentUserService currentUser) {
        this.customerRepository = customerRepository;
        this.currentUser = currentUser;
    }

    /**
     * Эндпоинт для личного кабинета гостя.
     * Возвращает баланс бонусов, текущий уровень и ID любимого блюда.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('guest:profile:view')") // Доступно только авторизованным гостям
    public ResponseEntity<Customer> getMyProfile() {
        Customer customer = customerRepository.findByUserId(currentUser.id())
                .orElseThrow(() -> new NotFoundException("Профиль гостя не найден"));

        return ResponseEntity.ok(customer);
    }
}
