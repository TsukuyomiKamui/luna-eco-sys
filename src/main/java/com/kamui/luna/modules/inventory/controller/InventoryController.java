package com.kamui.luna.modules.inventory.controller;

import com.kamui.luna.modules.inventory.dto.*;
import com.kamui.luna.modules.inventory.entity.Ingredient;
import com.kamui.luna.modules.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final InventoryService service;
    public InventoryController(InventoryService service) { this.service = service; }

    @GetMapping("/ingredients")
    @PreAuthorize("hasAuthority('inventory:stock:view')")
    public List<Ingredient> ingredients() { return service.ingredients(); }

    @PostMapping("/ingredients")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('inventory:stock:manage')")
    public Ingredient createIngredient(@Valid @RequestBody IngredientRequest request) { return service.createIngredient(request); }

    @GetMapping("/batches")
    @PreAuthorize("hasAuthority('inventory:stock:view')")
    public List<BatchResponse> batches() { return service.batches(); }

    @PostMapping("/batches")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('inventory:stock:manage')")
    public BatchResponse receive(@Valid @RequestBody BatchRequest request) { return service.receiveBatch(request); }

    @PutMapping("/recipes")
    @PreAuthorize("hasAuthority('inventory:stock:manage')")
    public Long saveRecipe(@Valid @RequestBody RecipeRequest request) { return service.saveRecipe(request); }
}
