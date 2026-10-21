CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE auth.permissions (
                                  id BIGSERIAL PRIMARY KEY,
                                  name VARCHAR(100) NOT NULL UNIQUE,
                                  description VARCHAR(255)
);

CREATE TABLE auth.roles (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE auth.role_permission (
                                      permission_id BIGINT NOT NULL,
                                      role_id BIGINT NOT NULL,
                                      PRIMARY KEY (permission_id, role_id),
                                      CONSTRAINT fk_rolper_on_permisson FOREIGN KEY (permission_id) REFERENCES auth.permissions (id) ON DELETE CASCADE,
                                      CONSTRAINT fk_rolper_on_role FOREIGN KEY (role_id) REFERENCES auth.roles (id) ON DELETE CASCADE
);

CREATE TABLE auth.users (
                            id BIGSERIAL PRIMARY KEY,
                            username VARCHAR(50) NOT NULL UNIQUE,
                            password_hash VARCHAR(100) NOT NULL,
                            is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE auth.user_roles (
                                 role_id BIGINT NOT NULL,
                                 user_id BIGINT NOT NULL,

                                 PRIMARY KEY (role_id, user_id),
                                 CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES auth.roles (id) ON DELETE CASCADE,
                                 CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES auth.users (id) ON DELETE CASCADE
);

INSERT INTO auth.permissions (name, description) VALUES
                                                     ('pos:order:create', 'Создание заказов в зале'),
                                                     ('pos:order:close', 'Закрытие чеков и прием оплаты'),
                                                     ('inventory:stock:view', 'Просмотр остатков на складе'),
                                                     ('menu:dish:edit', 'Управление элементами меню')
    ON CONFLICT DO NOTHING;