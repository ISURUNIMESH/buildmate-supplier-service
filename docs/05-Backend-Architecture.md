# 05 — Backend Architecture

> Spring Boot **4.1** / Java **21** microservices under the repository root.

---

## 🏛 Modules

| Module folder | Artifact role | Host / listen port |
|---------------|---------------|-------------------:|
| `auth-server/` | OAuth2 + JWT issuer | 9000 |
| `api-gateway/` | Spring Cloud Gateway | 28080 |
| `supplier-service/` | Supplier domain | 28084 |
| `material-service-main/` | Catalog domain | 28085 |
| `payment-service-main/` | Payments / invoices / reports | 28086 |
| `order-inventory-service-main/` | Orders / cart / inventory | 28087 |

---

## 📦 Typical package layout (domain services)

```text
com.buildmate.<service>/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Spring Data MongoDB
├── model/          # @Document entities
├── dto/            # Request/response / events
├── config/         # Security, RabbitMQ, OpenAPI, filters
├── exception/      # Handlers / error payloads (where present)
├── producer|publisher|consumer/  # Messaging (supplier, payment, order)
└── *Application.java
```

Material service uses package `com.construction.materialservice` (historical naming); behavior is otherwise aligned with other domain services.

---

## Controllers

| Service | Controllers |
|---------|-------------|
| Auth | `AuthController` (`/api/auth`) |
| Supplier | `SupplierController`, `DocumentController` |
| Material | `MaterialController`, `BrandController`, `CategoryController` |
| Payment | `PaymentController`, `InvoiceController`, `ReportController` |
| Order | `OrderController`, `CartController`, `InventoryController`, `HealthController` |

Gateway exposes no domain controllers; it defines **routes** in configuration.

---

## Services & repositories

- Each controller delegates to a `*Service`
- Persistence via Spring Data Mongo repositories (`*Repository`)
- Payment/Order messaging via dedicated publisher/listener classes

---

## Configuration highlights

| Concern | Where |
|---------|-------|
| Mongo URI / DB | `application.yml` / `application.properties` + env overrides |
| RabbitMQ | host/port/user + `RabbitMQConfig` beans |
| API keys | `ApiKeyFilter` + `api_keys` collection (+ env fallback on supplier/material) |
| OpenAPI | springdoc on Supplier, Material, Payment, Order |
| Auth RSA / OIDC | Auth Server config + `local` profile / `config/` mount in Docker |
| Gateway routes / JWT | Gateway `application.yml` + env (`JWT_*`, `*_SERVICE_URL`, `*_API_KEY`) |

---

## DTOs & entities

| Pattern | Examples |
|---------|----------|
| Write DTOs | `SupplierRegisterRequest`, `SupplierUpdateRequest`, `CreateOrderRequest`, `CartRequest` |
| Patch DTOs | `StockUpdateRequest`, `PriceUpdateRequest`, `SupplierStatusUpdateRequest`, `SupplierRatingUpdateRequest` |
| Entities as body | Material `Material`, Payment `Payment` / `Invoice` (validated entity on create) |
| Events | `OrderCreatedEvent`, `PaymentCompletedEvent`, supplier event payloads |

---

## Exception handling & validation

- Jakarta Validation (`@Valid`, `@NotBlank`, `@Positive`, …) on request models
- Domain services return `ResponseEntity` (`404`, `201`, etc.) from controllers
- Payment includes `ErrorResponse` model for structured errors where handlers are wired
- Validation failures typically surface as **400** with field messages (framework defaults + local handlers)

---

## Cross-cutting filters

| Filter | Services |
|--------|----------|
| `ApiKeyFilter` (`X-API-KEY`) | Supplier, Material, Payment, Order |
| JWT resource server | Auth (`/api/**`), Gateway (all non-public exchanges) |

<details>
<summary>Service security style differences (as implemented)</summary>

| Service | Style |
|---------|-------|
| Supplier / Payment | `SecurityFilterChain` + ApiKey filter |
| Material | ApiKey `@Component` only (no `SecurityFilterChain`) |
| Order | Servlet `FilterRegistrationBean` for ApiKey (no SecurityFilterChain) |

</details>

---

## Actuator

Domain services and gateway expose Actuator health (used by Docker `HEALTHCHECK`). Order also exposes custom `GET /health`.

---

## Related

- [03-Microservices](./03-Microservices.md)
- [06-API-Documentation](./06-API-Documentation.md)
- [09-Security-Documentation](./09-Security-Documentation.md)
