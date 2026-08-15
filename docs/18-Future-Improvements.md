# 18 — Future Improvements

> Practical enhancements that **do not** describe unfinished work as already shipped. No code changes are implied by this document.

---

## Security & identity

- Add SpringDoc OpenAPI to Auth Server (health, `/me`, OIDC notes)
- Optional JWT validation (or mTLS) on domain services for zero-trust beyond the Gateway
- Externalize secrets (Google client, API keys, Rabbit creds) via secret manager / Compose secrets
- Distributed rate limiting (Redis) for Gateway
- Configurable non-localhost OAuth redirect URIs for non-dev environments

---

## Messaging & payments

- Auto-create Payment (or payment intent) inside `OrderCreatedListener`
- Add consumers for `supplier.*` events (notifications, audit, search index)
- Consume or remove unused `payment.queue` / align refund routing keys with dedicated keys
- Dead-letter queues and retry policies for commerce events
- Idempotency keys on payment completion → order PAID

---

## Domain product depth

- Cart line quantity update, remove-single-item, and checkout → order
- Richer invoice PDF/export and invoice listing endpoints/UI
- Stronger inventory ↔ material stock synchronization
- Pagination, filtering, and sorting consistency across list APIs

---

## Platform & ops

- CI pipeline: build all modules, run smoke Compose profile, publish images
- Centralized structured logging / correlation IDs across Gateway → services
- Health readiness probes distinguishing Mongo/Rabbit dependency failure
- Helm/Kubernetes manifests mirroring Compose topology
- Automated contract tests (OpenAPI) between Gateway routes and services

---

## Frontend

- Stronger empty/error/loading consistency on all domain pages
- Role-based navigation if Auth accumulates roles/claims
- E2E tests (Playwright/Cypress) for OAuth callback + CRUD happy paths
- Document or retire legacy `buildmate-client/server` Express proxy

---

## Documentation hygiene

- Keep root README Swagger table aligned with springdoc presence
- Version documentation alongside Compose profile changes
