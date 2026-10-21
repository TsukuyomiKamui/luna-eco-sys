INSERT INTO auth.permissions (name, description) VALUES
 ('pos:order:create', 'Создание заказов'),
 ('pos:order:close', 'Оплата заказов'),
 ('pos:order:view', 'Просмотр заказов'),
 ('pos:table:manage', 'Управление столами'),
 ('menu:dish:edit', 'Управление меню'),
 ('menu:stoplist:manage', 'Управление стоп-листом'),
 ('inventory:stock:view', 'Просмотр склада'),
 ('inventory:stock:manage', 'Управление складом'),
 ('staff:shift:manage', 'Управление своей сменой'),
 ('staff:employee:manage', 'Управление сотрудниками'),
 ('finance:payout:execute', 'Проведение выплат'),
 ('finance:transaction:view', 'Просмотр финансов'),
 ('guest:profile:view', 'Просмотр профиля гостя')
ON CONFLICT (name) DO NOTHING;

INSERT INTO auth.roles (name) VALUES ('ADMIN'), ('WAITER'), ('MANAGER'), ('GUEST')
ON CONFLICT (name) DO NOTHING;

INSERT INTO auth.role_permission (role_id, permission_id)
SELECT r.id, p.id FROM auth.roles r CROSS JOIN auth.permissions p WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO auth.role_permission (role_id, permission_id)
SELECT r.id, p.id FROM auth.roles r JOIN auth.permissions p ON p.name IN
 ('pos:order:create', 'pos:order:close', 'pos:order:view', 'staff:shift:manage', 'inventory:stock:view')
WHERE r.name = 'WAITER' ON CONFLICT DO NOTHING;

INSERT INTO auth.role_permission (role_id, permission_id)
SELECT r.id, p.id FROM auth.roles r JOIN auth.permissions p ON p.name IN
 ('pos:order:create', 'pos:order:close', 'pos:order:view', 'pos:table:manage',
  'menu:dish:edit', 'menu:stoplist:manage', 'inventory:stock:view', 'inventory:stock:manage',
  'staff:shift:manage', 'staff:employee:manage', 'finance:transaction:view')
WHERE r.name = 'MANAGER' ON CONFLICT DO NOTHING;

INSERT INTO auth.role_permission (role_id, permission_id)
SELECT r.id, p.id FROM auth.roles r JOIN auth.permissions p ON p.name = 'guest:profile:view'
WHERE r.name = 'GUEST' ON CONFLICT DO NOTHING;

INSERT INTO menu.categories (name, sort_order) VALUES
 ('Основные блюда', 10), ('Напитки', 20)
ON CONFLICT (name) DO NOTHING;

INSERT INTO menu.dishes (category_id, name, description, price, is_available)
SELECT id, 'Паста Luna', 'Паста с томатным соусом', 650.00, TRUE FROM menu.categories WHERE name = 'Основные блюда'
ON CONFLICT (name) DO NOTHING;
INSERT INTO menu.dishes (category_id, name, description, price, is_available)
SELECT id, 'Лимонад', 'Домашний лимонад', 250.00, TRUE FROM menu.categories WHERE name = 'Напитки'
ON CONFLICT (name) DO NOTHING;

INSERT INTO pos.restaurant_tables (table_number, capacity) VALUES (1, 2), (2, 4), (3, 6)
ON CONFLICT (table_number) DO NOTHING;

INSERT INTO inventory.ingredients (name, sku, base_unit) VALUES
 ('Паста', 'PASTA-001', 'GRAM'), ('Томатный соус', 'SAUCE-001', 'GRAM'), ('Лимонадная основа', 'LEMON-001', 'MILLILITER')
ON CONFLICT (name) DO NOTHING;

INSERT INTO inventory.recipes (dish_id)
SELECT id FROM menu.dishes WHERE name IN ('Паста Luna', 'Лимонад') ON CONFLICT (dish_id) DO NOTHING;

INSERT INTO inventory.recipe_items (recipe_id, ingredient_id, amount)
SELECT r.id, i.id, 200 FROM inventory.recipes r
JOIN menu.dishes d ON d.id = r.dish_id JOIN inventory.ingredients i ON i.name = 'Паста'
WHERE d.name = 'Паста Luna' AND NOT EXISTS (SELECT 1 FROM inventory.recipe_items x WHERE x.recipe_id=r.id AND x.ingredient_id=i.id);
INSERT INTO inventory.recipe_items (recipe_id, ingredient_id, amount)
SELECT r.id, i.id, 120 FROM inventory.recipes r
JOIN menu.dishes d ON d.id = r.dish_id JOIN inventory.ingredients i ON i.name = 'Томатный соус'
WHERE d.name = 'Паста Luna' AND NOT EXISTS (SELECT 1 FROM inventory.recipe_items x WHERE x.recipe_id=r.id AND x.ingredient_id=i.id);
INSERT INTO inventory.recipe_items (recipe_id, ingredient_id, amount)
SELECT r.id, i.id, 300 FROM inventory.recipes r
JOIN menu.dishes d ON d.id = r.dish_id JOIN inventory.ingredients i ON i.name = 'Лимонадная основа'
WHERE d.name = 'Лимонад' AND NOT EXISTS (SELECT 1 FROM inventory.recipe_items x WHERE x.recipe_id=r.id AND x.ingredient_id=i.id);

INSERT INTO inventory.product_batches (ingredient_id, quantity, purchase_price, expiration_date)
SELECT id, 100000, 0.15, NOW() + INTERVAL '180 days' FROM inventory.ingredients i
WHERE NOT EXISTS (SELECT 1 FROM inventory.product_batches b WHERE b.ingredient_id=i.id);
