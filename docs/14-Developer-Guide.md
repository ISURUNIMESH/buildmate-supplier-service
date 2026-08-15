# 14 — Developer Guide

## Getting oriented

1. Read [01-Project-Overview](./01-Project-Overview.md) and [02-System-Architecture](./02-System-Architecture.md)
2. Run stack via [11-Installation-Guide](./11-Installation-Guide.md)
3. Use Swagger on domain host ports (`28084`–`28087`) + SPA for UX flows
4. Prefer Gateway (`28080`) for any feature that the React app uses

---

## Repository map

```text
supplier-service/                 # monorepo root
├── auth-server/
├── api-gateway/
├── supplier-service/
├── material-service-main/
├── payment-service-main/
├── order-inventory-service-main/
├── buildmate-client/
├── docker/mongo-init/
├── docker-compose.yml
├── docs/
└── README.md
```

---

## Local development conventions

| Area | Guidance |
|------|----------|
| Java | 21; Spring Boot 4.1 modules |
| Config | Keep `localhost` defaults for IDE runs; Compose overrides via env |
| Frontend | `npm run dev`; do not hardcode service ports in SPA — use `/api` |
| Secrets | Never commit real Google client secrets; use local config / env |
| Docs | Documentation-only changes go under `docs/` |

---

## Adding an endpoint (pattern)

1. DTO + validation annotations  
2. Service method + repository  
3. Controller mapping  
4. If exposed to SPA: Axios helper in `buildmate-client/src/services/`  
5. Confirm Gateway route already covers path prefix (`/api/...`)  
6. Update [06-API-Documentation](./06-API-Documentation.md) when documenting

---

## Messaging changes

- Declare beans in the service’s RabbitMQ config
- Keep exchange name `buildmate.exchange` unless intentionally introducing a new topology
- Document publishers/consumers in [08-RabbitMQ-Documentation](./08-RabbitMQ-Documentation.md)

---

## Security checklist for new routes

| Question | Action |
|----------|--------|
| Called from SPA? | Must work through Gateway with JWT |
| New Gateway path? | Add route + API key header injection |
| Domain filter exclusions? | Only for docs/actuator/health as today |

---

## Debugging tips

- Domain logs: `docker compose logs -f payment-service`
- JWT contents: decode payload (`iss`, `exp`, `sub`) without trusting unverified signatures for prod decisions
- Trace paid flow: Order create → Rabbit → Payment complete → Order listener

---

## Code style

- Match existing package layout and Lombok/`Record` usage per module
- Prefer existing DTO split (e.g. Supplier register vs update) over overloading validated password fields
- Do not reinvent auth — extend Auth Server / Gateway patterns already present

---

## Optional Express proxy

`buildmate-client/server` is legacy. Prefer Vite proxy / nginx Docker image for API access.
