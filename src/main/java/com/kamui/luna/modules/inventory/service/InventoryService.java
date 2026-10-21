package com.kamui.luna.modules.inventory.service;

import com.kamui.luna.modules.inventory.entity.ProductBatch;
import com.kamui.luna.modules.inventory.entity.Recipe;
import com.kamui.luna.modules.inventory.entity.RecipeItem;
import com.kamui.luna.modules.inventory.exception.InsufficientStockException;
import com.kamui.luna.modules.inventory.repository.ProductBatchRepository;
import com.kamui.luna.modules.inventory.repository.RecipeRepository;
import com.kamui.luna.modules.inventory.repository.IngredientRepository;
import com.kamui.luna.modules.inventory.entity.Ingredient;
import com.kamui.luna.modules.inventory.dto.*;
import com.kamui.luna.modules.menu.repository.DishRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import com.kamui.luna.core.event.SoldItem;
import com.kamui.luna.core.exception.NotFoundException;

@Service
public class InventoryService {

    private final RecipeRepository recipeRepository;
    private final ProductBatchRepository batchRepository;
    private final IngredientRepository ingredientRepository;
    private final DishRepository dishRepository;

    public InventoryService(RecipeRepository recipeRepository, ProductBatchRepository batchRepository,
                            IngredientRepository ingredientRepository, DishRepository dishRepository) {
        this.recipeRepository = recipeRepository;
        this.batchRepository = batchRepository;
        this.ingredientRepository = ingredientRepository;
        this.dishRepository = dishRepository;
    }

    /**
     * Основной метод, вызываемый при оплате заказа.
     * Проходит по каждому проданному блюду и списывает ингредиенты по его рецепту.
     */
    @Transactional
    public void writeOffIngredientsForItems(List<SoldItem> soldItems) {
        for (SoldItem sold : soldItems) {
            Recipe recipe = recipeRepository.findByDishId(sold.dishId())
                    .orElseThrow(() -> new NotFoundException("Технологическая карта не найдена для блюда ID: " + sold.dishId()));

            for (RecipeItem item : recipe.getItems()) {
                writeOffFifo(item.getIngredient().getId(),
                        item.getAmount().multiply(BigDecimal.valueOf(sold.quantity())));
            }
        }
    }

    /**
     * Алгоритм списания конкретного ингредиента по методу FIFO
     */
    private void writeOffFifo(Long ingredientId, BigDecimal amountToTake) {
        // Достаем партии, где есть остаток, отсортированные от старых к новым
        List<ProductBatch> availableBatches = batchRepository
                .findByIngredientIdAndQuantityGreaterThanOrderByExpirationDateAsc(ingredientId, BigDecimal.ZERO);

        BigDecimal remainingToTake = amountToTake;

        for (ProductBatch batch : availableBatches) {
            // Если уже всё списали — выходим из цикла
            if (remainingToTake.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            if (batch.getQuantity().compareTo(remainingToTake) >= 0) {
                // СЛУЧАЙ 1: В текущей партии хватает остатка, чтобы покрыть нужду целиком
                batch.setQuantity(batch.getQuantity().subtract(remainingToTake));
                batchRepository.save(batch);
                remainingToTake = BigDecimal.ZERO;
            } else {
                // СЛУЧАЙ 2: В партии меньше, чем нам нужно. Забираем всё, что есть, и идем к следующей партии
                remainingToTake = remainingToTake.subtract(batch.getQuantity());
                batch.setQuantity(BigDecimal.ZERO);
                batchRepository.save(batch);
            }
        }

        // Если после проверки всех партий мы так и не набрали нужный вес
        if (remainingToTake.compareTo(BigDecimal.ZERO) > 0) {
            throw new InsufficientStockException("Невозможно пробить чек: на складе не хватает "
                    + remainingToTake + " ед. ингредиента (ID: " + ingredientId + "). Позовите менеджера.");
        }
    }

    @Transactional
    public Ingredient createIngredient(IngredientRequest request) {
        Ingredient value = new Ingredient();
        value.setName(request.name().trim());
        value.setSku(request.sku().trim().toUpperCase());
        value.setBaseUnit(request.baseUnit().trim().toUpperCase());
        return ingredientRepository.save(value);
    }

    @Transactional(readOnly = true)
    public List<Ingredient> ingredients() { return ingredientRepository.findAll(); }

    @Transactional
    public BatchResponse receiveBatch(BatchRequest request) {
        Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
                .orElseThrow(() -> new NotFoundException("Ингредиент не найден"));
        ProductBatch batch = new ProductBatch();
        batch.setIngredient(ingredient);
        batch.setQuantity(request.quantity());
        batch.setPurchasePrice(request.purchasePrice());
        batch.setExpirationDate(request.expirationDate());
        return batchResponse(batchRepository.save(batch));
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> batches() {
        return batchRepository.findAllByOrderByExpirationDateAsc().stream().map(this::batchResponse).toList();
    }

    @Transactional
    public Long saveRecipe(RecipeRequest request) {
        if (!dishRepository.existsById(request.dishId())) throw new NotFoundException("Блюдо не найдено");
        Recipe recipe = recipeRepository.findByDishId(request.dishId()).orElseGet(Recipe::new);
        recipe.setDishId(request.dishId());
        recipe.getItems().clear();
        for (RecipeItemRequest requestedItem : request.items()) {
            Ingredient ingredient = ingredientRepository.findById(requestedItem.ingredientId())
                    .orElseThrow(() -> new NotFoundException("Ингредиент ID " + requestedItem.ingredientId() + " не найден"));
            RecipeItem item = new RecipeItem();
            item.setRecipe(recipe);
            item.setIngredient(ingredient);
            item.setAmount(requestedItem.amount());
            recipe.getItems().add(item);
        }
        return recipeRepository.save(recipe).getId();
    }

    private BatchResponse batchResponse(ProductBatch batch) {
        return new BatchResponse(batch.getId(), batch.getIngredient().getId(), batch.getIngredient().getName(),
                batch.getQuantity(), batch.getPurchasePrice(), batch.getExpirationDate(), batch.getReceivedAt());
    }
}
