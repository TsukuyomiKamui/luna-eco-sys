# Luna Restaurant System

Luna Restaurant System — backend MVP для автоматизации ресторана. Это модульный монолит на Spring Boot, который объединяет авторизацию, меню и зал, POS-заказы, складской учёт, смены сотрудников, финансы и программу лояльности.

Проект содержит не только SQL-схему: реализованы REST API, JWT-аутентификация, права доступа, бизнес-операции, транзакции, валидация, обработка ошибок, начальные данные, Swagger/OpenAPI, тесты и Docker-окружение.

## Статус проекта

Текущая версия — рабочий backend MVP. Реализован сквозной сценарий:

1. Сотрудник входит в систему и открывает смену.
2. Получает меню и список столов.
3. Создаёт заказ для стола и добавляет блюда.
4. Система фиксирует актуальную цену блюда в позиции заказа.
5. При оплате проверяются рецепты и складские остатки.
6. Ингредиенты списываются по партиям.
7. Заказ становится оплаченным, а стол освобождается.
8. После commit создаётся доход, начисляется бонус сотруднику и обновляется лояльность гостя.

В MVP пока не входят frontend, банковский эквайринг, онлайн-касса, возвраты, бронирование, кухонный экран, внешняя очередь событий и полный production-мониторинг. `POST /api/v1/pos/orders/{orderId}/pay` сейчас подтверждает оплату внутри системы, но не списывает реальные деньги.

## Что реализовано

### Авторизация и доступ

- регистрация гостя и создание сотрудников;
- login, refresh и logout;
- JWT access token на 15 минут и refresh token на 7 дней;
- BCrypt-хеширование паролей;
- stateless Spring Security;
- доступ по permissions;
- получение ID текущего пользователя из JWT, а не из клиентского запроса.

### Меню

- просмотр доступных блюд и категорий;
- создание категорий и блюд;
- изменение блюда;
- управление стоп-листом;
- запрет заказа недоступного блюда.

### Зал и POS

- создание, просмотр и удаление столов;
- статусы стола `FREE` и `OCCUPIED`;
- один открытый заказ на стол;
- закрепление заказа за создавшим его официантом;
- добавление нескольких позиций одним запросом;
- фиксация цены блюда в момент добавления;
- расчёт суммы позиции и всего заказа;
- просмотр и фильтрация заказов по `OPEN`, `PAID`, `CANCELLED`;
- оплата и отмена заказа;
- блокировка заказа от конкурентной оплаты;
- освобождение стола после оплаты или отмены.

### Склад

- справочник ингредиентов;
- приём партий с количеством, ценой и сроком годности;
- технологические карты блюд;
- списание с учётом количества проданных блюд;
- расход партий по ближайшему сроку годности;
- атомарный отказ оплаты при нехватке ингредиента.

Склад, заказ и стол обновляются одной транзакцией. Если остатка не хватает, заказ остаётся `OPEN`, стол — `OCCUPIED`, а частичное списание откатывается.

### Сотрудники и смены

- создание сотрудника с ролью, должностью, ставкой и процентом продаж;
- открытие и закрытие смены;
- защита от двух одновременных открытых смен;
- автоматический бонус от оплаченного заказа.

Если открытой смены нет, заказ оплатится, но сменный бонус не начислится.

### Финансы и лояльность

- регистрация дохода после оплаты;
- журнал финансовых операций;
- регистрация выплаты зарплаты и премии;
- бонусный баланс гостя;
- cashback: `BRONZE` — 5%, `SILVER` — 10%, `GOLD` — 15%;
- статистика блюд и определение любимого блюда;
- просмотр гостем собственного профиля.

Для начисления cashback в оплате передаётся `customerId` — ID профиля гостя из ответа регистрации, а не ID пользователя.

## Архитектура

```text
src/main/java/com/kamui/luna
├── core
│   ├── config       # bootstrap администратора, OpenAPI
│   ├── event        # доменное событие оплаты
│   └── exception    # единый формат ошибок
└── modules
    ├── auth         # пользователи, роли, JWT, refresh tokens
    ├── menu         # категории и блюда
    ├── pos          # столы, заказы и оплата
    ├── inventory    # ингредиенты, партии и рецепты
    ├── staff        # сотрудники, смены и бонусы
    ├── finance      # доходы, расходы и выплаты
    └── loyalty      # гости, cashback и любимые блюда
```

PostgreSQL разделена на схемы `auth`, `menu`, `pos`, `inventory`, `staff`, `finance`, `loyalty`.

### Поток оплаты

```text
POST /api/v1/pos/orders/{id}/pay
                 │
                 ▼
        блокировка заказа
                 │
                 ▼
   проверка статуса и customerId
                 │
                 ▼
   расчёт ингредиентов по рецептам
                 │
                 ▼
     списание партий со склада
                 │
                 ▼
 заказ = PAID, стол = FREE, COMMIT
                 │
                 ▼
     асинхронное OrderPaidEvent
        ├── запись выручки
        ├── бонус сотруднику
        └── cashback и статистика гостя
```

Дополнительные обработчики запускаются только после успешного commit. Поэтому финансы, бонус и лояльность могут появиться спустя короткое время после ответа на оплату.

## Технологии

- Java 25;
- Spring Boot 3.5.14;
- Spring Web, Security, Data JPA, Validation, Actuator;
- PostgreSQL 15 и Flyway;
- Redis 7;
- JJWT 0.11.5;
- Springdoc OpenAPI 2.8.13;
- Maven Wrapper;
- JUnit 5, Mockito и H2;
- Docker Compose.

Redis включён в окружение, но критический сценарий MVP хранит состояние в PostgreSQL и пока не зависит от Redis. Он подготовлен для будущего кэширования и распределённых механизмов.

## Быстрый запуск через Docker

Нужны Docker Desktop с Linux engine и Docker Compose v2.

### 1. Создайте `.env`

PowerShell:

```powershell
Copy-Item .env.example .env
```

Bash:

```bash
cp .env.example .env
```

Содержимое шаблона:

```dotenv
JWT_SECRET=replace-with-a-random-secret-at-least-32-characters
BOOTSTRAP_ADMIN_PASSWORD=replace-with-a-strong-password
DB_NAME=luna_db
DB_USERNAME=luna_admin
DB_PASSWORD=replace-with-a-strong-database-password
```

### 2. Запустите проект

```powershell
docker compose up -d --build
```

Первый запуск дольше последующих: Docker скачивает образы и Maven-зависимости.

### 3. Проверьте сервисы

```powershell
docker compose ps
docker compose logs -f luna-backend
```

| Сервис | Адрес/порт | Назначение |
|---|---:|---|
| Backend | `http://localhost:8080` | REST API и Swagger |
| PostgreSQL | `localhost:5432` | основное хранилище |
| Redis | `localhost:6379` | инфраструктура кэша |

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Ожидается `{"status":"UP"}`.

### Остановка и сброс

```powershell
# остановка с сохранением БД
docker compose down

# полный сброс локальной БД
docker compose down -v
docker compose up -d --build
```

Внимание: `down -v` безвозвратно удаляет локальные данные PostgreSQL этого Compose-проекта.

## Локальный запуск

```powershell
docker compose up -d postgres redis
.\mvnw.cmd spring-boot:run
```

Для Linux/macOS используйте `./mvnw spring-boot:run`. Нужен JDK 25.

По умолчанию приложение подключается к `jdbc:postgresql://localhost:5432/luna_db` и Redis на `localhost:6379`.

## Переменные окружения

| Переменная | По умолчанию | Описание |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/luna_db` | JDBC URL локального запуска |
| `DB_NAME` | `luna_db` | имя БД в Compose |
| `DB_USERNAME` | `luna_admin` | пользователь PostgreSQL |
| `DB_PASSWORD` | `luna_password` | пароль PostgreSQL |
| `JWT_SECRET` | dev-секрет | ключ JWT, минимум 32 символа |
| `BOOTSTRAP_ENABLED` | `true` | создавать администратора |
| `BOOTSTRAP_ADMIN_USERNAME` | `admin` | логин администратора |
| `BOOTSTRAP_ADMIN_PASSWORD` | `admin123` | пароль администратора |
| `SHOW_SQL` | `false` | печатать SQL Hibernate |

Значения по умолчанию только для локальной разработки. В общем окружении обязательно замените JWT-секрет и пароли.

## Начальные данные

Flyway создаёт:

- роли `ADMIN`, `MANAGER`, `WAITER`, `GUEST` и permissions;
- категории `Основные блюда` и `Напитки`;
- блюда `Паста Luna` (650) и `Лимонад` (250);
- столы №1, №2 и №3;
- ингредиенты, рецепты и стартовые партии;
- финансовые категории.

Bootstrap создаёт администратора и профиль сотрудника:

```text
login:    admin
password: admin123
role:     ADMIN
sales %:  5
```

Если в `.env` задан другой `BOOTSTRAP_ADMIN_PASSWORD`, используйте его. Bootstrap идемпотентен и не создаёт дубликаты. Его можно отключить через `BOOTSTRAP_ENABLED=false`.

## Роли

| Возможность | ADMIN | MANAGER | WAITER | GUEST |
|---|:---:|:---:|:---:|:---:|
| Просмотр меню | ✓ | ✓ | ✓ | ✓ |
| Создание/просмотр/закрытие заказов | ✓ | ✓ | ✓ | — |
| Управление столами | ✓ | ✓ | — | — |
| Управление меню и стоп-листом | ✓ | ✓ | — | — |
| Просмотр склада | ✓ | ✓ | ✓ | — |
| Управление складом и рецептами | ✓ | ✓ | — | — |
| Управление своей сменой | ✓ | ✓ | ✓ | — |
| Создание сотрудников | ✓ | ✓ | — | — |
| Просмотр финансов | ✓ | ✓ | — | — |
| Проведение выплат | ✓ | — | — | — |
| Свой профиль лояльности | ✓* | — | — | ✓ |

`*` ADMIN имеет permission, но у bootstrap-администратора нет профиля гостя, поэтому `/guest/loyalty/me` вернёт `404`.

## Swagger / OpenAPI

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Swagger доступен без авторизации, но почти все API-операции требуют JWT.

### Авторизация в Swagger

1. Откройте Swagger UI.
2. Раскройте `POST /api/v1/auth/login` и нажмите **Try it out**.
3. Отправьте:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

4. Скопируйте поле `token` из ответа — не `refreshToken`.
5. Нажмите **Authorize** вверху страницы.
6. Вставьте только токен. `Bearer` вручную добавлять не нужно.
7. Нажмите **Authorize**, затем **Close**.

Swagger начнёт отправлять `Authorization: Bearer <access-token>`. Если появился `401`, выполните login заново или обновите access token через `/auth/refresh`, затем повторите Authorize.

### Сквозная проверка в Swagger

1. `POST /api/v1/auth/login` — войти.
2. `POST /api/v1/staff/shifts/clock-in` — открыть смену, тело не нужно.
3. `GET /api/v1/menu` — получить ID блюд.
4. `GET /api/v1/admin/pos/tables` — выбрать свободный стол.
5. `POST /api/v1/staff/pos/tables/{tableId}/items` — создать заказ:

```json
[
  { "dishId": 1, "quantity": 2 },
  { "dishId": 2, "quantity": 1 }
]
```

6. Скопировать `id` заказа из ответа.
7. `POST /api/v1/pos/orders/{orderId}/pay` — отправить `{}` или не отправлять body.
8. Проверить:

   - заказ через `GET /api/v1/pos/orders/{orderId}` — статус `PAID`;
   - столы через `GET /api/v1/admin/pos/tables` — стол снова `FREE`;
   - партии через `GET /api/v1/inventory/batches` — остатки уменьшились;
   - финансы через `GET /api/v1/finance/transactions` — появился доход;
   - `POST /api/v1/staff/shifts/clock-out` — смена закрыта, бонус показан.

Не полагайтесь на ID `1` в клиентском коде: после изменения БД реальные ID могут отличаться.

### Проверка лояльности

Зарегистрируйте гостя без JWT:

```http
POST /api/v1/auth/register-guest
```

```json
{
  "username": "guest1",
  "password": "guest1234",
  "firstName": "Анна",
  "phoneNumber": "+7 900 000-00-00"
}
```

Ответ — числовой `customerId`. Передайте его при оплате заказа:

```json
{
  "customerId": 1
}
```

Затем войдите под `guest1`, замените токен в **Authorize** и вызовите `GET /api/v1/guest/loyalty/me`. Обновление асинхронное, поэтому иногда запрос нужно повторить спустя короткое время.

## API endpoints

Все endpoints, кроме `/api/v1/auth/**`, Swagger/OpenAPI и health, требуют access token.

| Метод | Endpoint | Назначение | Permission |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | вход | публичный |
| `POST` | `/api/v1/auth/refresh` | обновить access token | публичный |
| `POST` | `/api/v1/auth/register-guest` | зарегистрировать гостя | публичный |
| `POST` | `/api/v1/auth/logout` | удалить refresh token | любой JWT |
| `GET` | `/api/v1/menu` | доступные блюда | любой JWT |
| `GET` | `/api/v1/menu/categories` | категории | любой JWT |
| `POST` | `/api/v1/menu/categories` | создать категорию | `menu:dish:edit` |
| `POST` | `/api/v1/menu` | создать блюдо | `menu:dish:edit` |
| `PUT` | `/api/v1/menu/{id}` | изменить блюдо | `menu:dish:edit` |
| `PATCH` | `/api/v1/menu/{id}/stoplist?available=false` | доступность блюда | `menu:stoplist:manage` |
| `GET` | `/api/v1/admin/pos/tables` | список столов | любой JWT |
| `POST` | `/api/v1/admin/pos/tables` | создать стол | `pos:table:manage` |
| `DELETE` | `/api/v1/admin/pos/tables/{id}` | удалить свободный стол без истории | `pos:table:manage` |
| `POST` | `/api/v1/staff/pos/tables/{tableId}/items` | создать/дополнить заказ | `pos:order:create` |
| `GET` | `/api/v1/pos/orders` | заказы; фильтр `?status=` | `pos:order:view` |
| `GET` | `/api/v1/pos/orders/{orderId}` | один заказ | `pos:order:view` |
| `POST` | `/api/v1/pos/orders/{orderId}/pay` | оплатить заказ | `pos:order:close` |
| `POST` | `/api/v1/pos/orders/{orderId}/cancel` | отменить заказ | `pos:order:close` |
| `GET` | `/api/v1/inventory/ingredients` | ингредиенты | `inventory:stock:view` |
| `POST` | `/api/v1/inventory/ingredients` | создать ингредиент | `inventory:stock:manage` |
| `GET` | `/api/v1/inventory/batches` | складские партии | `inventory:stock:view` |
| `POST` | `/api/v1/inventory/batches` | принять партию | `inventory:stock:manage` |
| `PUT` | `/api/v1/inventory/recipes` | создать/заменить рецепт | `inventory:stock:manage` |
| `POST` | `/api/v1/staff/shifts/clock-in` | открыть смену | `staff:shift:manage` |
| `POST` | `/api/v1/staff/shifts/clock-out` | закрыть смену | `staff:shift:manage` |
| `POST` | `/api/v1/admin/staff` | создать сотрудника | `staff:employee:manage` |
| `GET` | `/api/v1/finance/transactions` | журнал финансов | `finance:transaction:view` |
| `POST` | `/api/v1/finance/payroll` | провести выплату | `finance:payout:execute` |
| `GET` | `/api/v1/guest/loyalty/me` | профиль гостя | `guest:profile:view` |

Swagger содержит актуальные request/response-схемы и правила валидации.

## Примеры запросов

### PowerShell

```powershell
$login = Invoke-RestMethod `
  -Method Post `
  -Uri 'http://localhost:8080/api/v1/auth/login' `
  -ContentType 'application/json' `
  -Body '{"username":"admin","password":"admin123"}'

$headers = @{ Authorization = "Bearer $($login.token)" }
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/menu' -Headers $headers
```

### curl

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'

curl http://localhost:8080/api/v1/menu \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN'
```

### Refresh token

```json
{
  "refreshToken": "значение-из-login"
}
```

Отправляется в `POST /api/v1/auth/refresh`.

### Создание сотрудника

```json
{
  "username": "waiter1",
  "password": "waiter123",
  "firstName": "Иван",
  "lastName": "Петров",
  "position": "Официант",
  "role": "WAITER",
  "hourlyRate": 350,
  "salesPercentage": 3
}
```

Допустимые роли: `ADMIN`, `MANAGER`, `WAITER`.

### Партия и рецепт

```json
{
  "ingredientId": 1,
  "quantity": 10000,
  "purchasePrice": 0.15,
  "expirationDate": "2027-01-01T12:00:00"
}
```

```json
{
  "dishId": 1,
  "items": [
    { "ingredientId": 1, "amount": 200 },
    { "ingredientId": 2, "amount": 120 }
  ]
}
```

`amount` задаётся в базовой единице ингредиента; дата партии должна быть в будущем.

## Ошибки API

Единый формат ошибки:

```json
{
  "timestamp": "2026-09-13T06:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Блюдо находится в стоп-листе",
  "path": "/api/v1/staff/pos/tables/1/items",
  "validationErrors": {}
}
```

- `400` — ошибка валидации;
- `401` — отсутствует или истёк JWT;
- `403` — нет permission;
- `404` — сущность не найдена;
- `409` — бизнес-конфликт: нехватка склада, повторная смена, занятый логин, неверный статус;
- `500` — непредвиденная ошибка без раскрытия внутренних деталей.

## Миграции

| Версия | Назначение |
|---|---|
| `V1` | пользователи, роли, permissions |
| `V2` | refresh tokens |
| `V3` | категории и блюда |
| `V4` | столы и заказы |
| `V5` | ингредиенты, партии и рецепты |
| `V6` | сотрудники и смены |
| `V7` | гости и статистика блюд |
| `V8` | финансы |
| `V9` | роли, права и demo-данные |
| `V10` | ограничения и уникальные индексы |

Hibernate использует `ddl-auto=validate`: схему меняет только Flyway. Новые изменения оформляйте новой миграцией; уже применённые миграции не редактируйте.

## Тесты и сборка

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
docker compose config --quiet
```

Есть Spring smoke test контекста и OpenAPI, unit-тесты заказов и складского списания, а также тестовая H2-конфигурация.

## Полезные команды

```powershell
docker compose ps
docker compose logs -f luna-backend
docker compose logs -f postgres
docker compose restart luna-backend
docker compose up -d --build luna-backend
docker compose down
```

## Устранение проблем

### Swagger не открывается

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
docker compose logs --tail=200 luna-backend
```

### `401` или `403`

Для `401` получите новый access token и повторите **Authorize**. Для `403` у пользователя нет нужного permission — сверяйтесь с таблицей ролей.

### Оплата возвращает `409`

Проверьте, что заказ имеет статус `OPEN`, содержит позиции, у всех блюд есть рецепты, на складе достаточно ингредиентов, а `customerId` существует.

### Docker сообщает I/O или read-only error

Полностью перезапустите Docker Desktop. Для зависшего WSL2 иногда требуется:

```powershell
wsl --shutdown
```

После запуска Docker Desktop и состояния Running повторите `docker compose up -d --build`. Не удаляйте Docker data volume/VHDX, если в них есть нужные данные.

## Перед production

- заменить секреты и пароли;
- отключить или ограничить bootstrap;
- закрыть наружные порты PostgreSQL и Redis;
- настроить HTTPS, reverse proxy и CORS;
- добавить rate limiting и ротацию refresh tokens;
- использовать outbox/очередь для надёжных событий;
- настроить метрики, логи, alerting и backup;
- добавить Testcontainers/e2e-тесты;
- подключить эквайринг и фискальный контур;
- добавить аудит действий персонала.

## Лицензия

Лицензия пока не указана. До появления `LICENSE` проект следует считать закрытым для внешнего распространения.
