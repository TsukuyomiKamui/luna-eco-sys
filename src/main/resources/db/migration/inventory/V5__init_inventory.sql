CREATE SCHEMA IF NOT EXISTS inventory;

-- 1. Справочник сырья
CREATE TABLE inventory.ingredients (
                                       id BIGSERIAL PRIMARY KEY,
                                       name VARCHAR(150) NOT NULL UNIQUE,
                                       sku VARCHAR(50) NOT NULL UNIQUE, -- Артикул
                                       base_unit VARCHAR(20) NOT NULL   -- Единица измерения (GRAM, MILLILITER, PIECE)
);

-- 2. Партии продуктов (для алгоритма FIFO)
CREATE TABLE inventory.product_batches (
                                           id BIGSERIAL PRIMARY KEY,
                                           ingredient_id BIGINT NOT NULL,
                                           quantity NUMERIC(19, 4) NOT NULL,       -- Текущий остаток в партии
                                           purchase_price NUMERIC(19, 4) NOT NULL, -- Закупочная цена (для фудкоста)
                                           expiration_date TIMESTAMP NOT NULL,     -- Годен до
                                           received_at TIMESTAMP NOT NULL DEFAULT NOW(),

                                           CONSTRAINT fk_batch_ingredient FOREIGN KEY (ingredient_id) REFERENCES inventory.ingredients (id) ON DELETE RESTRICT
);

-- Индекс для мгновенного поиска самых старых партий (FIFO)
CREATE INDEX idx_batches_fifo ON inventory.product_batches (ingredient_id, expiration_date);

-- 3. Технологические карты (Рецепты)
CREATE TABLE inventory.recipes (
                                   id BIGSERIAL PRIMARY KEY,
                                   dish_id BIGINT NOT NULL UNIQUE -- Ссылка на ID блюда из модуля menu
);

-- 4. Состав технологической карты (Ингредиенты в рецепте)
CREATE TABLE inventory.recipe_items (
                                        id BIGSERIAL PRIMARY KEY,
                                        recipe_id BIGINT NOT NULL,
                                        ingredient_id BIGINT NOT NULL,
                                        amount NUMERIC(19, 4) NOT NULL, -- Сколько списывать (в базовых единицах)

                                        CONSTRAINT fk_item_recipe FOREIGN KEY (recipe_id) REFERENCES inventory.recipes (id) ON DELETE CASCADE,
                                        CONSTRAINT fk_item_ingredient FOREIGN KEY (ingredient_id) REFERENCES inventory.ingredients (id) ON DELETE RESTRICT
);
