# 06 — API Documentation

> Paths below are shown as **via API Gateway** (`http://localhost:28080`) unless noted. Domain services use the same path **without** `/api` (Gateway rewrites). Auth keeps `/api/auth/**`.

---

## Auth conventions

| Entry point | Auth header | Notes |
|-------------|-------------|-------|
| Gateway (`:28080`) | `Authorization: Bearer <BuildHub JWT>` | Required except public paths |
| Direct service ports | `X-API-KEY: <service-key>` | JWT not enforced by domain services |
| Auth health | none | Public |
| Auth `/me` | Bearer JWT | Validated by Auth resource server |

Default seeded / Compose API keys:

| Service | Key (defaults) |
|---------|----------------|
| Supplier | `buildmate-supplier-key` |
| Material | `buildmate-material-key` |
| Payment | `buildmate-payment-key` |
| Order | `buildmate-order-key` |

---

## Auth Server (`:9000`)

### `GET /api/auth/health`

| | |
|--|--|
| **Auth** | Public |
| **Response** | Health payload (200) |

### `GET /api/auth/me`

| | |
|--|--|
| **Auth** | Bearer JWT |
| **Response** | Current user profile (`AuthUserResponse`) |
| **Status** | 200 / 401 |

### OIDC / OAuth (framework)

| Path | Purpose |
|------|---------|
| `/oauth2/authorization/google` | Start Google login |
| `/oauth2/jwks` | JWKS for Gateway JWT validation |
| `/oauth2/**`, `/login/**` | Login/consent plumbing |

---

## Supplier (`/api/suppliers/**`)

### Register — `POST /api/suppliers`

**Headers (direct):** `X-API-KEY` · **Via gateway:** Bearer JWT  

**Body:**

```json
{
  "supplierCode": "SUP-001",
  "companyName": "Concrete Co",
  "ownerName": "Alex Owner",
  "email": "alex@example.com",
  "password": "Secret123!",
  "phone": "+94000000000",
  "address": "12 Main St",
  "district": "Colombo",
  "businessRegistrationNo": "BR-12345"
}
```

**Status:** 201 / 400 / 401

### Login — `POST /api/suppliers/login`

```json
{ "email": "alex@example.com", "password": "Secret123!" }
```

### List — `GET /api/suppliers`  
### Get — `GET /api/suppliers/{id}`  

### Update — `PUT /api/suppliers/{id}`

Uses `SupplierUpdateRequest` (no password):

```json
{
  "companyName": "Concrete Co Ltd",
  "ownerName": "Alex Owner",
  "phone": "+94000000001",
  "address": "14 Main St",
  "district": "Gampaha"
}
```

### Delete — `DELETE /api/suppliers/{id}`

### Status — `PATCH /api/suppliers/{id}/status`

Body includes status field per `SupplierStatusUpdateRequest`.

### Rating — `PATCH /api/suppliers/{id}/rating`

Body per `SupplierRatingUpdateRequest`.

### Top rated — `GET /api/suppliers/top-rated`

### Documents

| Method | Path |
|--------|------|
| POST | `/api/suppliers/{id}/documents` |
| GET | `/api/suppliers/{id}/documents` |

---

## Material (`/api/materials/**`, `/api/brands/**`, `/api/categories/**`)

### Materials

| Method | Path | Body / query |
|--------|------|----------------|
| GET | `/api/materials` | — |
| GET | `/api/materials/{id}` | — |
| POST | `/api/materials` | Material JSON |
| PUT | `/api/materials/{id}` | Material JSON |
| DELETE | `/api/materials/{id}` | — |
| GET | `/api/materials/category/{category}` | — |
| GET | `/api/materials/search?keyword=` | query |
| GET | `/api/materials/low-stock` | — |
| PATCH | `/api/materials/{id}/stock` | stock DTO |
| PATCH | `/api/materials/{id}/price` | price DTO |

**Example create:**

```json
{
  "name": "Cement 50kg",
  "description": "OPC cement bag",
  "category": "Cement",
  "price": 1850.0,
  "stock": 100,
  "unit": "bag",
  "supplierId": "<supplierId>"
}
```

### Brands / Categories

| Method | Brands | Categories |
|--------|--------|------------|
| GET | `/api/brands` | `/api/categories` |
| POST | `/api/brands` | `/api/categories` |
| PUT | `/api/brands/{id}` | `/api/categories/{id}` |
| DELETE | `/api/brands/{id}` | `/api/categories/{id}` |

---

## Order & Inventory

### Orders (`/api/orders/**`)

| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/orders` | Create + publish `order.created` |
| GET | `/api/orders` | List |
| GET | `/api/orders/{id}` | |
| PATCH | `/api/orders/{id}/status?status=` | Query param |
| DELETE | `/api/orders/{id}` | |
| GET | `/api/orders/user/{userId}` | |
| GET | `/api/orders/status/{status}` | |

**Create body:**

```json
{
  "userId": "user-123",
  "items": [
    { "materialId": "mat-1", "quantity": 2, "price": 1850.00 }
  ]
}
```

### Cart (`/api/cart/**`)

| Method | Path |
|--------|------|
| POST | `/api/cart` |
| GET | `/api/cart/{userId}` |
| DELETE | `/api/cart/{userId}` |

```json
{
  "userId": "user-123",
  "materialId": "mat-1",
  "quantity": 1,
  "price": 1850.00
}
```

### Inventory (`/api/inventory/**`)

| Method | Path |
|--------|------|
| GET | `/api/inventory` |
| POST | `/api/inventory` |
| PATCH | `/api/inventory/{materialId}/reserve` |
| PATCH | `/api/inventory/{materialId}/release` |
| GET | `/api/inventory/history` |

### Health — `GET /api/health` (and service `GET /health`)

Public at domain filter level for `/health`; via gateway still requires JWT unless matching a gateway public path (`/api/auth/health`, actuator). Prefer Actuator for ops checks.

---

## Payment (`/api/payments/**`, `/api/invoices/**`, `/api/reports/**`)

### Payments

| Method | Path | Status notes |
|--------|------|--------------|
| POST | `/api/payments` | **201** |
| GET | `/api/payments/{id}` | 200 / 404 |
| GET | `/api/payments` | list |
| GET | `/api/payments/history/{userId}` | |
| GET | `/api/payments/user/{userId}` | |
| GET | `/api/payments/pending` | |
| GET | `/api/payments/status/{status}` | |
| PATCH | `/api/payments/{id}/status?status=` | Completing can publish `payment.completed` |
| POST | `/api/payments/{id}/refund` | |
| POST | `/api/payments/{id}/retry` | |

**Create example:**

```json
{
  "orderId": "<orderId>",
  "userId": "user-123",
  "amount": 3700.00,
  "currency": "LKR",
  "paymentMethod": "CARD",
  "status": "PENDING"
}
```

### Invoices

| Method | Path |
|--------|------|
| POST | `/api/invoices` |
| GET | `/api/invoices/{id}` |

### Reports

| Method | Path |
|--------|------|
| GET | `/api/reports/revenue` |
| GET | `/api/reports/monthly` |
| GET | `/api/reports/top-customers` |

---

## Common status codes

| Code | Meaning in this stack |
|-----:|------------------------|
| 200 | Success |
| 201 | Created (e.g. payment) |
| 400 | Validation / bad request |
| 401 | Missing/invalid JWT (gateway) or API key (domain) |
| 404 | Resource not found |
| 429 | Possible when gateway rate limit is exceeded |

---

## OpenAPI / Swagger UI (host URLs)

| Service | Typical UI path |
|---------|-----------------|
| Gateway | `http://localhost:28080/swagger-ui.html` (if springdoc enabled on gateway) |
| Supplier | `http://localhost:28084/swagger-ui.html` |
| Material | `http://localhost:28085/swagger-ui/index.html` |
| Payment | `http://localhost:28086/swagger-ui.html` (springdoc default) |
| Order | `http://localhost:28087/swagger-ui.html` |
| Auth | **No springdoc** — use OIDC/JWKS; do not expect `http://localhost:9000/swagger-ui.html` |

<details>
<summary>Gateway vs direct calling</summary>

- **SPA path:** always Gateway + JWT; Gateway injects `X-API-KEY`.
- **Direct curl to :28084–28087:** must send correct `X-API-KEY`; no end-user JWT check on those services.

</details>
