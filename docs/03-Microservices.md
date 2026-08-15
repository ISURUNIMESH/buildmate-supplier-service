# 03 — Microservices

## Overview

| Service | Host port | Database | Mongo (Compose DNS) | RabbitMQ |
|---------|----------:|----------|---------------------|----------|
| Auth Server | 9000 | `buildmate_auth_db` | `auth-mongo:27017` | — |
| API Gateway | 28080 | — | — | — |
| Supplier | 28084 | `supplier_db` | `supplier-mongo:27017` | Publisher to `buildmate.exchange` |
| Material | 28085 | `material_db` | `material-mongo:27017` | — |
| Payment | 28086 | `payment_db` | `payment-mongo:27017` | Consumer + publisher |
| Order & Inventory | 28087 | `order_inventory_db` | `order-mongo:27017` | Consumer + publisher |

Host Mongo ports (Compass / native): Auth **27017**, Material **27020**, Supplier **27021**, Payment **27022**, Order **27023**. Inside the network, always use `*-mongo:27017` — not `localhost`.

---

## Auth Server (`auth-server`)

**Responsibilities**

- Google OAuth2 login (Spring Security OAuth2 Client)
- Persist marketplace users in Mongo (`users`)
- Issue BuildHub RSA JWT; expose JWKS / OIDC
- Custom API: `GET /api/auth/health`, `GET /api/auth/me`

**Dependencies:** MongoDB Auth only  

**Config notes:** `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` or `local` profile + `config/application-local.yml` (Docker). Credentials come from `.env` / local config — never hardcode secrets in docs.

---

## API Gateway (`api-gateway`)

**Responsibilities**

- JWT resource-server validation
- Route `/api/**` to upstream container/service URLs
- Inject service-specific `X-API-KEY`
- CORS + in-memory rate limiting

**Dependencies:** Auth (JWKS), all domain services  

**Does not:** own a database or RabbitMQ client

---

## Supplier Service (`supplier-service`)

**Responsibilities**

- Supplier register / login / CRUD / status / rating / top-rated
- Document upload/list under `/suppliers/{id}/documents`
- API-key filter (Mongo `api_keys` + env fallback)
- Publish lifecycle events (`supplier.created|updated|status.changed|deleted`)

**Dependencies:** `supplier-mongo`, RabbitMQ (publish only)

---

## Material Service (`material-service-main`)

**Responsibilities**

- Materials, brands, categories CRUD
- Stock and price patches; search; low-stock listing
- API-key filter (`MATERIAL_API_KEY` env fallback)

**Dependencies:** `material-mongo`  

**RabbitMQ:** not used

---

## Payment Service (`payment-service-main`)

**Responsibilities**

- Payments lifecycle (create, status, refund, retry)
- Invoices create/get
- Reports: revenue, monthly, top-customers
- Consume `OrderCreatedEvent`; publish `PaymentCompletedEvent`
- Also publishes to legacy `payment.exchange` / `payment.created`

**Dependencies:** `payment-mongo`, RabbitMQ

---

## Order & Inventory Service (`order-inventory-service-main`)

**Responsibilities**

- Orders CRUD / status / filters
- Cart (post/get/delete by user)
- Inventory create/list/reserve/release/history
- Publish `OrderCreatedEvent`; consume `PaymentCompletedEvent` → status **PAID**
- Public `/health` (+ Actuator)

**Dependencies:** `order-mongo`, RabbitMQ

---

## Dependency matrix

| Consumer | Auth | Supplier | Material | Payment | Order | Mongo | Rabbit |
|----------|:----:|:--------:|:--------:|:-------:|:-----:|:-----:|:------:|
| Gateway | JWKS | proxy | proxy | proxy | proxy | — | — |
| Frontend | OAuth | via GW | via GW | via GW | via GW | — | — |
| Supplier | — | — | — | — | — | ✓ | publish |
| Payment | — | — | — | — | events | ✓ | ✓ |
| Order | — | — | — | events | — | ✓ | ✓ |
