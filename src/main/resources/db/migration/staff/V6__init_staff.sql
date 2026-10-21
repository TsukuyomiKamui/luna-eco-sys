CREATE SCHEMA IF NOT EXISTS staff;

-- 1. Карточки сотрудников (HR профиль)
CREATE TABLE staff.employees (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id BIGINT NOT NULL UNIQUE, -- Мягкая ссылка на auth.users (ID для авторизации)
                                 first_name VARCHAR(100) NOT NULL,
                                 last_name VARCHAR(100) NOT NULL,
                                 position VARCHAR(100) NOT NULL, -- Должность для отображения
                                 hourly_rate NUMERIC(19, 4) NOT NULL DEFAULT 0.0, -- Почасовая ставка
                                 sales_percentage NUMERIC(5, 2) NOT NULL DEFAULT 0.0 -- Процент от личных продаж (например, 5.00%)
);

-- 2. Рабочие смены сотрудников
CREATE TABLE staff.shifts (
                              id BIGSERIAL PRIMARY KEY,
                              employee_id BIGINT NOT NULL,
                              start_time TIMESTAMP NOT NULL DEFAULT NOW(),
                              end_time TIMESTAMP,
                              status VARCHAR(50) NOT NULL DEFAULT 'OPEN', -- OPEN (на смене), CLOSED (смена закрыта)
                              earned_bonus NUMERIC(19, 4) NOT NULL DEFAULT 0.0, -- Накопленный бонус за пробитые чеки за эту смену

                              CONSTRAINT fk_shift_employee FOREIGN KEY (employee_id) REFERENCES staff.employees (id) ON DELETE CASCADE
);

-- Индекс для быстрого поиска текущей открытой смены сотрудника
CREATE INDEX idx_shifts_employee_status ON staff.shifts(employee_id, status);
