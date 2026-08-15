# BuildHub

Construction supplier platform — microservices, API Gateway, Auth, React UI.

**UI branding:** BuildHub. Technical identifiers (`buildmate-*`, Docker service names, RabbitMQ exchange names, Java packages) are unchanged.

**Authoritative port map:** [`PORTS.md`](PORTS.md)

---

## Final port map

| Component | Host Port |
|-----------|----------:|
| Auth MongoDB | 27017 |
| Material MongoDB | 27020 |
| Supplier MongoDB | 27021 |
| Payment MongoDB | 27022 |
| Order/Inventory MongoDB | 27023 |
| Auth Server | 9000 |
| API Gateway | 28080 |
| Supplier Service | 28084 |
| Material Service | 28085 |
| Payment Service | 28086 |
| Order/Inventory Service | 28087 |
| Frontend | 25173 |
| RabbitMQ AMQP | 25672 |
| RabbitMQ Management UI | 25673 |

---

## Architecture

```text
Frontend (25173)
    ↓  /api
API Gateway (28080)  — JWT + injects X-API-KEY
    ↓
Auth (9000) | Supplier (28084) | Material (28085) | Payment (28086) | Order (28087)

MongoDB (isolated):
  Auth → auth-mongo
  Material → material-mongo
  Supplier → supplier-mongo
  Payment → payment-mongo
  Order → order-mongo

RabbitMQ (topic buildmate.exchange):
  Supplier → Material
  Material → Order/Inventory
  Order → Payment
  Payment → Order (payment.completed → order PAID)
```

Docker containers talk by **service DNS** (`auth-mongo:27017`, `rabbitmq:5672`, `supplier-service:28084`, …). Never use `localhost` between containers.

From the Windows host / Compass / native tools, use `localhost:<host-port>`.

---

## Prerequisites

- Docker Desktop
- Git
- Java 21 + Maven (only if running services natively)
- Node.js 22+ (only if running the frontend with Vite)

---

## Start complete Docker stack

1. Copy environment template and set secrets locally:

```powershell
copy .env.example .env
# Edit .env — set RABBITMQ_USERNAME / RABBITMQ_PASSWORD (required)
```

2. Start full stack (API + frontend):

```powershell
docker compose --profile full up -d --build
```

API-only (no React image):

```powershell
docker compose --profile api up -d --build
```

### Check status

```powershell
docker compose ps
```

### Check logs

```powershell
docker compose logs -f api-gateway
docker compose logs -f <service>
```

### Stop

```powershell
docker compose --profile full down
```

> **DATA-DESTRUCTIVE:** `docker compose --profile full down -v` removes volumes (Mongo/RabbitMQ data). Do not use unless you intend to wipe data.

---

## Health checks

| Service | URL |
|---------|-----|
| Auth | http://localhost:9000/actuator/health |
| Gateway | http://localhost:28080/actuator/health |
| Supplier | http://localhost:28084/actuator/health |
| Material | http://localhost:28085/actuator/health |
| Payment | http://localhost:28086/actuator/health |
| Order/Inventory | http://localhost:28087/actuator/health |
| Frontend | http://localhost:25173 |
| RabbitMQ UI | http://localhost:25673 (credentials from `.env`) |

---

## MongoDB

| Service | Host URI |
|---------|----------|
| Auth | `mongodb://localhost:27017/buildmate_auth_db` |
| Material | `mongodb://localhost:27020/material_db` |
| Supplier | `mongodb://localhost:27021/supplier_db` |
| Payment | `mongodb://localhost:27022/payment_db` |
| Order | `mongodb://localhost:27023/order_inventory_db` |

Inside Docker: `mongodb://auth-mongo:27017/...` (and peers). See [`docker/MONGODB-COMPASS.md`](docker/MONGODB-COMPASS.md).

---

## RabbitMQ

| | Host |
|--|------|
| AMQP | `localhost:25672` |
| Management UI | http://localhost:25673 |

Credentials: read from local `.env` (`RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD`). Do not commit real passwords.

Important topology:

- Exchange: `buildmate.exchange` (topic)
- Queues: `material.supplier.events.queue`, `order.inventory.material.events.queue`, `order.created.queue`, `payment.completed.queue`
- `payment.queue` may have zero consumers (known gap)

Inside Docker, services use `rabbitmq:5672`.

---

## API / Swagger

Prefer the **Gateway** for authenticated calls:

`http://localhost:28080/api/...` with `Authorization: Bearer <JWT>`

Swagger / OpenAPI (host ports):

| Service | Swagger UI | OpenAPI |
|---------|------------|---------|
| Auth | http://localhost:9000/swagger-ui.html | `/v3/api-docs` |
| Gateway | http://localhost:28080/swagger-ui.html | `/v3/api-docs` |
| Supplier | http://localhost:28084/swagger-ui.html | `/v3/api-docs` |
| Material | http://localhost:28085/swagger-ui.html | `/v3/api-docs` |
| Payment | http://localhost:28086/swagger-ui.html | `/v3/api-docs` |
| Order | http://localhost:28087/swagger-ui.html | `/v3/api-docs` |

Direct service calls use root paths (e.g. `GET http://localhost:28085/materials`) with header `X-API-KEY`. Gateway paths use `/api/...` and strip the prefix.

Auth: JWT Bearer (Gateway). Downstream: `X-API-KEY` (injected by Gateway).

### E2E smoke / master audit

```powershell
.\scripts\e2e-smoke.ps1
.\scripts\master-audit.ps1
```

`master-audit.ps1` verifies Gateway JWT, API keys, Mongo persistence, RabbitMQ events (including restart recovery), cart, invoice, Swagger, and persistence after service restart. It does not print secrets.

---

## Frontend

- Docker: http://localhost:25173
- Native Vite: `cd buildmate-client && npm run dev:web` (port **25173**)
- API base: `/api` → Gateway `http://localhost:28080` (do not call microservices from the browser)

Google OAuth redirect URI:

`http://localhost:9000/login/oauth2/code/google`

---

## Native development defaults

| Dependency | Host |
|------------|------|
| Auth Mongo | `localhost:27017` |
| Material/Supplier/Payment/Order Mongo | `27020` / `27021` / `27022` / `27023` |
| Auth / Gateway / services | `9000` / `28080` / `28084–28087` |
| Frontend | `25173` |
| RabbitMQ AMQP | `25672` |

Compose overrides Mongo/Rabbit URLs to Docker DNS when running in containers.

---

## Repository layout

```text
api-gateway/
auth-server/
supplier-service/
material-service-main/
payment-service-main/
order-inventory-service-main/
buildmate-client/
docker/
scripts/
docs/
docker-compose.yml
PORTS.md
```

More detail: [`docs/`](docs/).

---

## Security notes

- Never commit real `.env` secrets
- Gateway is the JWT perimeter; keep direct `:28084–28087` private in production
- API keys and RabbitMQ passwords live in local `.env` only

# BuildMate - Dockerized Microservices API Testing Guide

## Running the Project

### Build all services

bash
docker compose build --no-cache


### Start all containers

bash
docker compose up -d


### Check running containers

bash
docker compose ps


---

## Service Ports

| Service | Port |
| --- | --- |
| Client | 25173 |
| API Gateway | 28080 |
| Auth Service | 9000 |
| Supplier Service | 28084 |
| Material Service | 28085 |
| Payment Service | 28086 |
| Order Inventory Service | 28087 |
| RabbitMQ Management | 25673 |

---

# RabbitMQ

## Open RabbitMQ Dashboard

text
http://localhost:25673


## Default Login

text
Username: guest
Password: guest


---

# Postman API Testing

---

## Auth Service

### Create User

http
POST http://localhost:9000/auth/register


json
{
    "username": "testuser",
    "email": "test@test.com",
    "password": "123456"
}


---

### Login

http
POST http://localhost:9000/auth/login


json
{
    "email": "test@test.com",
    "password": "123456"
}


---

## Supplier Service

### Get All Suppliers

http
GET http://localhost:28084/suppliers


---

### Create Supplier

http
POST http://localhost:28084/suppliers


json
{
    "supplierCode": "S_020",
    "companyName": "ABC Company",
    "email": "abc@gmail.com",
    "phone": "0712345678"
}


---

### Get Materials by Supplier

http
GET http://localhost:28084/suppliers/{supplierId}/materials


Example:

http
GET http://localhost:28084/suppliers/6874abcd123/materials


---

## Material Service

### Get All Materials

http
GET http://localhost:28085/materials


---

### Create Material

http
POST http://localhost:28085/materials


json
{
    "materialCode": "M_020",
    "name": "Cement",
    "price": 1500,
    "supplierId": "SUPPLIER_ID"
}


---

## Payment Service

### Get All Payments

http
GET http://localhost:28086/payments


---

### Create Payment

http
POST http://localhost:28086/payments


json
{
    "orderId": "ORDER_001",
    "userId": "USER_001",
    "amount": 1200,
    "paymentMethod": "CARD",
    "currency": "LKR",
    "status": "SUCCESS"
}


---

## Order Inventory Service

### Get All Inventory Records

http
GET http://localhost:28087/inventory


---

### Create Inventory

http
POST http://localhost:28087/inventory


json
{
    "materialId": "MATERIAL_ID",
    "availableQuantity": 100,
    "reservedQuantity": 0,
    "minimumStock": 10
}


---

## RabbitMQ Verification

### List Exchanges

bash
docker exec rabbitmq rabbitmqctl list_exchanges


---

### List Queues

bash
docker exec rabbitmq rabbitmqctl list_queues


---

### List Consumers

bash
docker exec rabbitmq rabbitmqctl list_consumers


---

### List Bindings

bash
docker exec rabbitmq rabbitmqctl list_bindings source_name destination_name routing_key


---

## Docker Commands

### Rebuild Everything

bash
docker compose build --no-cache


### Restart Containers

bash
docker compose restart


### Stop Containers

bash
docker compose down


### View Logs

bash
docker compose logs -f  


