package com.kamui.luna.modules.inventory;

import com.kamui.luna.core.event.SoldItem;
import com.kamui.luna.modules.inventory.entity.*;
import com.kamui.luna.modules.inventory.repository.*;
import com.kamui.luna.modules.inventory.service.InventoryService;
import com.kamui.luna.modules.menu.repository.DishRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InventoryServiceTest {
    @Test
    void writesOffOldestBatchesAndRespectsDishQuantity() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        ProductBatchRepository batches = mock(ProductBatchRepository.class);
        InventoryService service = new InventoryService(recipes, batches, mock(IngredientRepository.class), mock(DishRepository.class));
        Ingredient ingredient = new Ingredient(); ingredient.setId(2L); ingredient.setName("Паста");
        Recipe recipe = new Recipe(); recipe.setDishId(5L);
        RecipeItem recipeItem = new RecipeItem(); recipeItem.setIngredient(ingredient); recipeItem.setAmount(new BigDecimal("200"));
        recipe.getItems().add(recipeItem);
        ProductBatch old = batch(ingredient, "300", 2); ProductBatch fresh = batch(ingredient, "500", 20);
        when(recipes.findByDishId(5L)).thenReturn(Optional.of(recipe));
        when(batches.findByIngredientIdAndQuantityGreaterThanOrderByExpirationDateAsc(2L, BigDecimal.ZERO))
                .thenReturn(List.of(old, fresh));

        service.writeOffIngredientsForItems(List.of(new SoldItem(5L, 2)));

        assertThat(old.getQuantity()).isZero();
        assertThat(fresh.getQuantity()).isEqualByComparingTo("400");
    }

    private ProductBatch batch(Ingredient ingredient, String quantity, int days) {
        ProductBatch batch = new ProductBatch(); batch.setIngredient(ingredient);
        batch.setQuantity(new BigDecimal(quantity)); batch.setPurchasePrice(BigDecimal.ONE);
        batch.setExpirationDate(LocalDateTime.now().plusDays(days)); return batch;
    }
}
