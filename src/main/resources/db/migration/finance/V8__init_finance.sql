CREATE SCHEMA IF NOT EXISTS finance;

-- 1. Справочник статей движения денежных средств (ДДС)
CREATE TABLE finance.transaction_categories (
                                                id BIGSERIAL PRIMARY KEY,
                                                name VARCHAR(100) NOT NULL UNIQUE, -- Например: "Выручка зала", "Закупка продуктов", "Зарплата и премии"
                                                type VARCHAR(20) NOT NULL          -- INCOME (Доход) или EXPENSE (Расход)
);

-- 2. Журнал финансовых транзакций (Неизменяемый!)
CREATE TABLE finance.transactions (
                                      id BIGSERIAL PRIMARY KEY,
                                      category_id BIGINT NOT NULL,
                                      amount NUMERIC(19, 4) NOT NULL, -- Всегда положительное число, направление определяет type категории
                                      description VARCHAR(255),       -- Пояснение (например: "Оплата заказа #123" или "Премия официанту ID 5")
                                      created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                                      CONSTRAINT fk_transaction_category FOREIGN KEY (category_id) REFERENCES finance.transaction_categories (id) ON DELETE RESTRICT
);

-- Базовое наполнение категорий для работы системы
INSERT INTO finance.transaction_categories (name, type) VALUES
                                                            ('Выручка POS (Зал)', 'INCOME'),
                                                            ('Закупка сырья', 'EXPENSE'),
                                                            ('Зарплата и премии', 'EXPENSE'),
                                                            ('Аренда и коммуналка', 'EXPENSE')
    ON CONFLICT DO NOTHING;
