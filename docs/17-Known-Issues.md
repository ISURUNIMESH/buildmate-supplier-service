# 17 — Known Issues

> Only issues **observed in the current repository** (code or docs mismatch). Fixed historical bugs are noted as resolved.

---

## Open / by-design limitations

| ID | Issue | Evidence |
|----|-------|----------|
| KI-01 | Auth Server has **no springdoc / Swagger UI** | No springdoc dependency in `auth-server/pom.xml` |
| KI-02 | Domain services do **not** validate end-user JWT | ApiKey-only filters; JWT only at Gateway/Auth `/api` |
| KI-03 | Direct ports `:28084–28087` bypass JWT perimeter | Architecture allows API-key-only access |
| KI-04 | Payment & Order API keys are **Mongo-only** (no env fallback) | `ApiKeyFilter` implementations |
| KI-05 | `OrderCreatedListener` **logs only** — does not create payments | Class comment / implementation |
| KI-06 | No consumers for `supplier.*` routing keys | Publisher-only in Supplier |
| KI-07 | `payment.queue` has **no** `@RabbitListener` | Declared; unused by consumers |
| KI-08 | Cart API is **minimal** (no qty update / checkout endpoints) | `CartController` surface |
| KI-09 | Gateway rate limiter is **in-memory** (not distributed) | Gateway config |
| KI-10 | OAuth / frontend success URLs are **localhost-oriented** | Compose + Auth defaults |
| KI-11 | Material lacks `SecurityFilterChain`; Order uses servlet filter only | Divergent security wiring vs Supplier/Payment |
| KI-12 | Root README may still mention Auth Swagger while Auth lacks OpenAPI | Prefer `docs/06` + OIDC/JWKS as source of truth |

---

## Resolved in current codebase (for history)

| Item | Resolution |
|------|------------|
| Supplier PUT required password from register DTO | `SupplierUpdateRequest` without password |
| Top-rated empty with threshold ≥ 4.0 on zero defaults | Top-rated listing without hard ≥4.0 gate + rating PATCH |

---

## Operational pitfalls (not code defects)

- Mongo init seeds run only on **first** empty volume  
- Recreating `auth-rsa-data` invalidates previously issued JWTs  
- Using Compose profile `api` omits the frontend container intentionally  
