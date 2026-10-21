CREATE TABLE auth.refresh_tokens (
                                     id BIGSERIAL PRIMARY KEY,
                                     user_id BIGINT NOT NULL,
                                     token VARCHAR(255) NOT NULL UNIQUE,
                                     expiry_date TIMESTAMP NOT NULL,
                                     CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE
);

-- Индекс для быстрой проверки токена при обновлении
CREATE INDEX idx_refresh_token_string ON auth.refresh_tokens(token);