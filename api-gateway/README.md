# BuildHub API Gateway

API Gateway for BuildHub microservices (JWT + rate limit + `X-API-KEY` injection).

Technical module name remains `api-gateway`.

## Architecture

```text
Client
  │  Authorization: Bearer <access_token>
  ▼
API Gateway (:28080)
  ├─ OAuth2 JWT validation (Resource Server)
  ├─ CORS
  ├─ In-memory rate limiting (token bucket)
  └─ Routes + injects X-API-KEY per service
        │
        ├── /api/auth/**          → auth-server              (:9000)
        ├── /api/suppliers/**     → supplier-service         (:28084)
        ├── /api/materials|brands|categories/** → material-service (:28085)
        ├── /api/payments|invoices|reports/** → payment-service (:28086)
        └── /api/orders|inventory|cart|health/** → order-inventory (:28087)

OAuth Authorization Server (:9000)
  └─ POST /oauth2/token  (client_credentials / authorization_code)
```

Docker routes use service DNS (`http://supplier-service:28084`, …). Native defaults use `http://localhost:28084`, …

## Prerequisites

- Java 21
- Maven 3.9+
- Auth server must be running before the gateway (JWT issuer metadata)

## Start auth-server

```bash
cd auth-server
mvn spring-boot:run
```

- Issuer: `http://localhost:9000`
- Client ID / secret: see Auth server config (do not commit production secrets)

## Start api-gateway

```bash
cd api-gateway
mvn spring-boot:run
```

Gateway: `http://localhost:28080`

## Get an access token (client credentials)

```bash
curl -u buildmate-client:buildmate-secret \
  -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=api.read api.write"
```

## Call a service through the gateway

```bash
curl -H "Authorization: Bearer <access_token>" \
  http://localhost:28080/api/suppliers
```

## CORS

Allowed origins include:

- `http://localhost:25173`
- `http://localhost:3000`
- `http://localhost:5173` (legacy Vite)

## Security layers

1. **OAuth 2.0 at the edge** — JWT required on Gateway routes
2. **API keys per microservice** — Gateway injects `X-API-KEY`
3. **CORS** — approved browser origins
4. **Rate limiting** — token bucket (default 10/s, burst 20)
