CREATE SCHEMA IF NOT EXISTS pos;

-- Таблица столиков ресторана
CREATE TABLE pos.restaurant_tables (
                                       id BIGSERIAL PRIMARY KEY,
                                       table_number INT NOT NULL UNIQUE,
                                       capacity INT NOT NULL,
                                       status VARCHAR(50) NOT NULL DEFAULT 'FREE' -- FREE, OCCUPIED, RESERVED
);

-- Таблица заказов (чеков)
CREATE TABLE pos.orders (
                            id BIGSERIAL PRIMARY KEY,
                            table_id BIGINT NOT NULL,
                            waiter_id BIGINT NOT NULL, -- ID сотрудника из модуля auth/staff
                            customer_id BIGINT,        -- ID гостя из модуля loyalty (может быть NULL)
                            status VARCHAR(50) NOT NULL DEFAULT 'OPEN', -- OPEN, PAID, CANCELLED
                            total_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0, -- Строго NUMERIC
                            created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                            CONSTRAINT fk_order_table FOREIGN KEY (table_id) REFERENCES pos.restaurant_tables (id) ON DELETE RESTRICT
);

-- Таблица позиций в заказе (что конкретно заказали)
CREATE TABLE pos.order_items (
                                 id BIGSERIAL PRIMARY KEY,
                                 order_id BIGINT NOT NULL,
                                 dish_id BIGINT NOT NULL, -- Ссылка на ID блюда из модуля menu
                                 quantity INT NOT NULL DEFAULT 1,
                                 price NUMERIC(19, 4) NOT NULL, -- Зафиксированная цена на момент продажи

                                 CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES pos.orders (id) ON DELETE CASCADE
);
