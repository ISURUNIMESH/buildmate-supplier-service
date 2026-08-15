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

## Update inventory
