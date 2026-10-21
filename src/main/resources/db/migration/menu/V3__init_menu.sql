CREATE SCHEMA IF NOT EXISTS menu;

-- Таблица категорий меню (например, "Супы", "Горячее", "Напитки")
CREATE TABLE menu.categories (
                                 id BIGSERIAL PRIMARY KEY,
                                 name VARCHAR(100) NOT NULL UNIQUE,
                                 sort_order INT NOT NULL DEFAULT 0 -- Для красивого отображения на фронтенде
);

-- Таблица готовых блюд
CREATE TABLE menu.dishes (
                             id BIGSERIAL PRIMARY KEY,
                             category_id BIGINT NOT NULL,
                             name VARCHAR(150) NOT NULL UNIQUE,
                             description TEXT,
                             price NUMERIC(19, 4) NOT NULL, -- Строго NUMERIC для финансов!
                             is_available BOOLEAN NOT NULL DEFAULT TRUE, -- Стоп-лист (если закончилось на кухне)
                             image_url VARCHAR(255),

                             CONSTRAINT fk_dish_category FOREIGN KEY (category_id) REFERENCES menu.categories (id) ON DELETE RESTRICT
);

-- Индекс для быстрого поиска блюд при фильтрации меню по категориям
CREATE INDEX idx_dishes_category ON menu.dishes(category_id);
