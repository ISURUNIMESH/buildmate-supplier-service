# 16 — System Verification Report

> Evidence based on repository implementation and a prior successful **Docker Compose `api` profile** verification (health, JWT, API keys, CRUD, RabbitMQ E2E) against an earlier port layout. **Port tables below are aligned to the FINAL BuildHub host architecture** in [PORTS.md](../PORTS.md). Re-run [12-Testing-Guide](./12-Testing-Guide.md) on your machine to refresh evidence — do not treat historical PASS rows as a fresh re-verification on the new binds.

**Report date context:** documentation freeze aligned with current codebase layout + final host ports.  
**Compose project:** `buildmate`  
**Profiles exercised (historically):** `api` (full stack `full` available for SPA)

---

## Summary

| Area | Result |
|------|--------|
| Services / containers | ✅ Present & healthchecked in Compose |
| Ports | ✅ Docs/Compose aligned to FINAL BuildHub host table (see below) |
| Security (Gateway JWT) | ✅ 401 without token / 200 with valid JWT *(prior run)* |
| API keys | ✅ 401 without / 200 with seeded keys *(prior run)* |
| RabbitMQ paid flow | ✅ Order → PaymentCompleted → **PAID** *(prior run)* |
| Databases | ✅ Dedicated Mongo per service |
| Gateway routing | ✅ `/api/**` prefixes configured |
| OAuth / JWKS | ✅ Issuer + JWKS endpoints implemented |
| Frontend | ✅ Vite SPA routes + Docker nginx (`full`) |
| Auth OpenAPI | ⚠️ No springdoc on Auth |

---

## Services & ports (FINAL host architecture)

| Component | Host port | Health probe |
|-----------|----------:|--------------|
| Auth Server | 9000 | `/api/auth/health`, Actuator |
| API Gateway | 28080 | `/actuator/health` |
| Supplier | 28084 | Actuator |
| Material | 28085 | Actuator |
| Payment | 28086 | Actuator |
| Order | 28087 | Actuator + `/health` |
| Frontend | 25173 | HTTP (profile `full`) |
| Mongo ×5 | 27017, 27020–27023 | mongosh ping (Auth on **27017**) |
| RabbitMQ | 25672 / 25673 | diagnostics ping (maps → container `5672` / `15672`) |

**Architecture note:** Prior verification evidence used older host binds (e.g. Gateway `8080`, domain `8084–8087`, frontend `5173`, Rabbit `5672`/`15672`). Behavior claims below remain from that earlier run; only the **port mapping** has been updated here for alignment. Inside Docker, Mongo stays `*-mongo:27017` and RabbitMQ `rabbitmq:5672`.

---

## Security verification matrix

| Test | Expected | Observed (prior run) |
|------|----------|----------------------|
| Gateway no JWT | 401 | Pass |
| Gateway bad JWT | 401 | Pass |
| Gateway valid JWT | 200 | Pass |
| Direct API no key | 401 | Pass |
| Direct API valid key | 200 | Pass |
| JWKS fetch | 200 + keys | Pass |
| `/api/auth/me` with JWT | 200 | Pass |

---

## RabbitMQ verification

| Item | Status |
|------|--------|
| Exchange `buildmate.exchange` | Declared by services |
| Queues `order.created.queue`, `payment.completed.queue` | Bound |
| E2E PAID transition | Pass on prior API profile verification |

---

## Database verification

| DB | Seed API key script | Role |
|----|---------------------|------|
| `buildmate_auth_db` | — (users via OAuth) | Auth |
| `material_db` | yes | Material |
| `supplier_db` | yes | Supplier |
| `payment_db` | yes | Payment |
| `order_inventory_db` | yes | Order |

---

## CRUD / domain smoke

| Domain | Prior verification |
|--------|--------------------|
| Supplier / Material / Order / Payment sample CRUD | Pass |
| Gateway injection of `X-API-KEY` | Pass |

---

## Docker verification

| Check | Status |
|-------|--------|
| Multi-stage Dockerfiles for all Spring + client | Present |
| `--profile api up` healthy stack | Pass (prior) |
| `--profile full` includes client | Implemented |

---

## Frontend verification

| Check | Status |
|-------|--------|
| Routes for dashboard & domains | Present in `App.jsx` |
| Axios Bearer + `/api` proxy | Present |
| OAuth callback page | Present |

---

## Gaps vs ideal enterprise coverage

Documented in [17-Known-Issues](./17-Known-Issues.md) — do not count as verification failures of stated design (edge JWT + service keys).
