# BuildHub — Final Port Allocation

## Host port table

| Component | Host Port |
|-----------|----------:|
| Auth MongoDB | **27017** |
| Material MongoDB | **27020** |
| Supplier MongoDB | **27021** |
| Payment MongoDB | **27022** |
| Order/Inventory MongoDB | **27023** |
| Auth Server | **9000** |
| API Gateway | **28080** |
| Supplier Service | **28084** |
| Material Service | **28085** |
| Payment Service | **28086** |
| Order/Inventory Service | **28087** |
| Frontend | **25173** |
| RabbitMQ AMQP | **25672** |
| RabbitMQ Management UI | **25673** |

## MongoDB (host ↔ container)

| Service | Host Port | Container Port | Database |
|---------|----------:|---------------:|----------|
| auth-mongo | **27017** | 27017 | `buildmate_auth_db` |
| material-mongo | **27020** | 27017 | `material_db` |
| supplier-mongo | **27021** | 27017 | `supplier_db` |
| payment-mongo | **27022** | 27017 | `payment_db` |
| order-mongo | **27023** | 27017 | `order_inventory_db` |

### Docker internal (container → container)

```text
mongodb://auth-mongo:27017/buildmate_auth_db
mongodb://material-mongo:27017/material_db
mongodb://supplier-mongo:27017/supplier_db
mongodb://payment-mongo:27017/payment_db
mongodb://order-mongo:27017/order_inventory_db
```

### Host / Compass / native Java

```text
mongodb://localhost:27017/buildmate_auth_db   → Auth
mongodb://localhost:27020/material_db         → Material
mongodb://localhost:27021/supplier_db         → Supplier
mongodb://localhost:27022/payment_db          → Payment
mongodb://localhost:27023/order_inventory_db  → Order/Inventory
```

Import Compass favorites from `docker/mongodb-compass-connections.json` (see `docker/MONGODB-COMPASS.md`).

## Application ports (listen = host = container for Spring services)

| Service | Port |
|---------|-----:|
| auth-server | **9000** |
| api-gateway | **28080** |
| supplier-service | **28084** |
| material-service | **28085** |
| payment-service | **28086** |
| order-inventory-service | **28087** |
| buildmate-client (nginx) | **80** inside → host **25173** |
| rabbitmq AMQP | **5672** inside → host **25672** |
| rabbitmq management | **15672** inside → host **25673** |

## Start

```bash
docker compose --profile full up -d --build
```
