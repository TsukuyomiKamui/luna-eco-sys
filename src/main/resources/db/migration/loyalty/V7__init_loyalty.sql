CREATE SCHEMA IF NOT EXISTS loyalty;

-- 1. Профили гостей
CREATE TABLE loyalty.customers (
                                   id BIGSERIAL PRIMARY KEY,
                                   user_id BIGINT NOT NULL UNIQUE, -- Мягкая ссылка на auth.users
                                   first_name VARCHAR(100) NOT NULL,
                                   phone_number VARCHAR(20) UNIQUE,
                                   bonus_balance NUMERIC(19, 4) NOT NULL DEFAULT 0.0,
                                   loyalty_level VARCHAR(20) NOT NULL DEFAULT 'BRONZE', -- BRONZE, SILVER, GOLD
                                   favorite_dish_id BIGINT -- ID любимого блюда из модуля menu
);

-- 2. Статистика по блюдам (для автоматического расчета "Любимого блюда")
CREATE TABLE loyalty.customer_dish_stats (
                                             id BIGSERIAL PRIMARY KEY,
                                             customer_id BIGINT NOT NULL,
                                             dish_id BIGINT NOT NULL,
                                             order_count INT NOT NULL DEFAULT 1,

                                             CONSTRAINT fk_stat_customer FOREIGN KEY (customer_id) REFERENCES loyalty.customers (id) ON DELETE CASCADE,
                                             CONSTRAINT uc_customer_dish UNIQUE (customer_id, dish_id) -- Один счетчик на связку гость+блюдо
);

-- 3. Сезонные акции и скидки
CREATE TABLE loyalty.promotions (
                                    id BIGSERIAL PRIMARY KEY,
                                    name VARCHAR(150) NOT NULL,
                                    dish_id BIGINT NOT NULL, -- На какое блюдо скидка
                                    discount_percentage NUMERIC(5, 2) NOT NULL, -- Процент скидки (например, 15.00)
                                    start_date TIMESTAMP NOT NULL,
                                    end_date TIMESTAMP NOT NULL,
                                    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_promotions_active ON loyalty.promotions(dish_id, is_active);
