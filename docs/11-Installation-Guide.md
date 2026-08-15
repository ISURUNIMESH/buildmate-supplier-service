# 11 — Installation Guide

## Requirements

| Tool | Purpose |
|------|---------|
| JDK **21** | Build/run Spring Boot modules |
| Maven | Per-service builds (`mvnw` if present) |
| Node.js **≥ 18** (LTS recommended) | Frontend |
| npm | Frontend deps |
| Docker + Docker Compose | Recommended full stack |
| MongoDB 7 (×5) / RabbitMQ | Required for local non-Docker infra |
| Google OAuth client | Auth login |

---

## Clone

```bash
cd /path/to/workspace
# clone your fork/remote of this repository, then:
cd supplier-service
```

Repository root contains Compose + all service folders. Canonical host ports: [PORTS.md](../PORTS.md).

---

## Option A — Docker (recommended)

### API stack only

```bash
docker compose --profile api up -d --build
```

### Full stack (API + frontend)

```bash
docker compose --profile full up -d --build
```

Ensure Google credentials are available for Auth (from `.env` or `local` profile config under `auth-server/config/` — do not commit real secrets).

### Stop

```bash
docker compose --profile full down
# or
docker compose --profile api down
```

---

## Option B — Run locally (without Compose apps)

### 1. Infrastructure

Start five MongoDB listeners on host ports **27017, 27020–27023** (Auth on **27017**) and RabbitMQ.

- **Native broker on the host:** apps typically use AMQP **5672** / UI **15672** on localhost.
- **Compose infra only:** host publishes RabbitMQ as **25672** (AMQP) / **25673** (UI); containers still talk to `rabbitmq:5672`.

Seed API keys into Payment/Order (and others) `api_keys` collections if empty (see `docker/mongo-init/*/01-seed-api-key.js`).

### 2. Build & run each Spring service

From each module directory:

```bash
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```

Suggested start order: Auth → domain services → Gateway. Listen ports match the final host table (`9000`, `28084`–`28087`, `28080`).

### 3. Frontend

```bash
cd buildmate-client
npm install
npm run dev
```

Vite serves `http://localhost:25173` and proxies `/api` → `http://localhost:28080`.

---

## Environment variables (local)

Copy / edit:

- `buildmate-client/.env.example` → `.env`
- Auth Google secrets / `application-local.yml`
- Root / Compose `.env` for Rabbit and API keys (credentials come from `.env` — never hardcode passwords in docs)

Key frontend vars:

| Variable | Typical |
|----------|---------|
| `VITE_API_BASE` | `/api` |
| `VITE_AUTH_SERVER_URL` | `http://localhost:9000` |
| `VITE_GOOGLE_LOGIN_URL` | Auth Google authorize URL |

---

## Ports checklist (host-facing)

| Port | Component |
|-----:|-----------|
| 25173 | Frontend |
| 28080 | Gateway |
| 9000 | Auth |
| 28084 | Supplier |
| 28085 | Material |
| 28086 | Payment |
| 28087 | Order |
| 27017 / 27020–27023 | Mongo (Auth **27017**) |
| 25672 / 25673 | RabbitMQ AMQP / UI (Compose host maps) |

Docker-internal Mongo/Rabbit remain `*-mongo:27017` and `rabbitmq:5672`.

---

## Verify after install

See [12-Testing-Guide](./12-Testing-Guide.md) and [16-System-Verification-Report](./16-System-Verification-Report.md).

Quick probes:

```bash
curl http://localhost:28080/actuator/health
curl http://localhost:9000/api/auth/health
curl http://localhost:9000/oauth2/jwks
```
