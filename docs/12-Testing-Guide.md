# 12 — Testing Guide

Manual / smoke procedures aligned with the implemented stack. No invented automated test suites beyond what exists in modules. Host ports follow [PORTS.md](../PORTS.md).

---

## Smoke testing

| Check | Command / action | Expect |
|-------|------------------|--------|
| Gateway health | `GET http://localhost:28080/actuator/health` | UP |
| Auth health | `GET http://localhost:9000/api/auth/health` | 200 |
| JWKS | `GET http://localhost:9000/oauth2/jwks` | keys JSON |
| Rabbit UI | `http://localhost:25673` (Compose host map) | login works (creds from `.env`) |
| Frontend | `http://localhost:25173` | Login page |
| Domain Actuator | `:28084`–`:28087` `/actuator/health` | UP |

---

## OAuth testing

1. Open `http://localhost:25173/login`
2. Start Google login → Auth `:9000`
3. Complete consent
4. Land on `/oauth/callback` with token
5. Confirm `localStorage` keys `buildmate_access_token`, `buildmate_user`
6. `GET http://localhost:9000/api/auth/me` with Bearer → 200

Negative: call `/api/auth/me` without token → 401.

---

## JWT / Gateway testing

| Case | Expect |
|------|--------|
| `GET /api/suppliers` without Bearer | **401** |
| Invalid Bearer | **401** |
| Valid Bearer from OAuth | **200** (or empty list) |

Use browser SPA or curl against Gateway `http://localhost:28080` with `Authorization: Bearer …`.

---

## API key testing (direct ports)

```bash
# Missing key → 401
curl -i http://localhost:28084/suppliers

# Valid key → 200
curl -i -H "X-API-KEY: buildmate-supplier-key" http://localhost:28084/suppliers
```

Repeat pattern for Material (`buildmate-material-key` :28085), Payment (`buildmate-payment-key` :28086), Order (`buildmate-order-key` :28087).

---

## CRUD testing (via Gateway preferred)

| Domain | Minimal flow |
|--------|----------------|
| Supplier | POST register → GET list → PUT update → PATCH status/rating → GET top-rated |
| Material | POST material → GET → PATCH stock/price → search/low-stock → brands/categories CRUD |
| Order | POST order → GET by id → PATCH status → filters by user/status |
| Cart | POST item → GET by userId → DELETE cart |
| Inventory | POST → reserve → release → history |
| Payment | POST payment → PATCH status COMPLETED → GET reports/invoices |

Validate **400** on invalid bodies (e.g. supplier update missing required fields).

---

## RabbitMQ testing (E2E)

1. Create order via `POST /api/orders` (JWT) → confirm message on `order.created.queue`
2. Create payment for `orderId` → set status to completed path that publishes `PaymentCompletedEvent`
3. Confirm order status becomes **PAID**
4. Inspect RabbitMQ management UI at `http://localhost:25673` (bindings/rates)

> `OrderCreatedListener` logs only; payment create remains an API step.

---

## Gateway testing

| Scenario | Expect |
|----------|--------|
| Routing rewrite `/api/materials` → material service | Data returns |
| CORS preflight OPTIONS | Allowed |
| Burst beyond rate limit | Possible **429** |
| Upstream down | Gateway error / fallback behavior per config |

---

## Docker testing

```bash
docker compose --profile api up -d --build
docker compose --profile api ps
# All api profile containers healthy

docker compose --profile full up -d --build
# + buildmate-client healthy; UI on host :25173
```

Re-run smoke, JWT, API key, and Rabbit E2E against published **host** ports (not Docker-internal `5672` / `*-mongo:27017` from the host).

---

## Frontend testing

| Area | Check |
|------|-------|
| Login / logout | Token cleared on logout |
| Each sidebar route | Loads without console fatal errors |
| Theme toggle | Persists `buildmate_theme` |
| Protected routes | Redirect to `/login` when logged out |

---

## Suggested checklist export

- [ ] Infra UP  
- [ ] OAuth + `/me`  
- [ ] Gateway JWT 401/200  
- [ ] API keys 401/200  
- [ ] CRUD sample per service  
- [ ] Order → Payment → PAID  
- [ ] Docker profile `api` / `full`  
