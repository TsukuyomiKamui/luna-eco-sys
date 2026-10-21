package com.kamui.luna.modules.menu.controller;

import com.kamui.luna.modules.menu.dto.DishRequest;
import com.kamui.luna.modules.menu.dto.DishResponse;
import com.kamui.luna.modules.menu.service.MenuService;
import com.kamui.luna.modules.menu.dto.CategoryRequest;
import com.kamui.luna.modules.menu.entity.Category;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * Получить активное меню.
     * Доступно всем аутентифицированным пользователям (и официантам, и гостям).
     */
    @GetMapping
    public ResponseEntity<List<DishResponse>> getMenu() {
        return ResponseEntity.ok(menuService.getAllAvailableDishes());
    }

    /**
     * Добавить новое блюдо в систему.
     * Выполнить может только сотрудник (например, Менеджер), у которого в Роли есть право 'menu:dish:edit'.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('menu:dish:edit')")
    public ResponseEntity<DishResponse> createDish(@Valid @RequestBody DishRequest request) {
        DishResponse response = menuService.createDish(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('menu:dish:edit')")
    public DishResponse updateDish(@PathVariable Long id, @Valid @RequestBody DishRequest request) {
        return menuService.updateDish(id, request);
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('menu:dish:edit')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createCategory(request));
    }

    @GetMapping("/categories")
    public List<Category> categories() { return menuService.categories(); }

    /**
     * Добавить или убрать блюдо из стоп-листа.
     * Могут делать менеджеры или су-шефы с правом 'menu:stoplist:manage'.
     */
    @PatchMapping("/{id}/stoplist")
    @PreAuthorize("hasAuthority('menu:stoplist:manage')")
    public ResponseEntity<String> toggleStopList(@PathVariable Long id, @RequestParam boolean available) {
        menuService.toggleStopList(id, available);
        String status = available ? "убрано из стоп-листа" : "добавлено в стоп-лист";
        return ResponseEntity.ok("Блюдо успешно " + status);
    }
}
