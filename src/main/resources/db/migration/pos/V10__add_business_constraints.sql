CREATE UNIQUE INDEX IF NOT EXISTS ux_orders_one_open_per_table
    ON pos.orders(table_id) WHERE status = 'OPEN';

ALTER TABLE pos.restaurant_tables
    ADD CONSTRAINT ck_restaurant_table_capacity CHECK (capacity > 0),
    ADD CONSTRAINT ck_restaurant_table_status CHECK (status IN ('FREE', 'OCCUPIED', 'RESERVED'));

ALTER TABLE pos.orders
    ADD CONSTRAINT ck_order_status CHECK (status IN ('OPEN', 'PAID', 'CANCELLED')),
    ADD CONSTRAINT ck_order_total CHECK (total_amount >= 0);

ALTER TABLE pos.order_items
    ADD CONSTRAINT ck_order_item_quantity CHECK (quantity > 0),
    ADD CONSTRAINT ck_order_item_price CHECK (price >= 0);

ALTER TABLE inventory.product_batches
    ADD CONSTRAINT ck_batch_quantity CHECK (quantity >= 0),
    ADD CONSTRAINT ck_batch_price CHECK (purchase_price >= 0);

ALTER TABLE inventory.recipe_items
    ADD CONSTRAINT ck_recipe_amount CHECK (amount > 0);
