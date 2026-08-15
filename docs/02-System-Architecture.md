# 02 — System Architecture

## Host vs Docker-internal ports

| Audience | What to use |
|----------|-------------|
| Browser, curl, Compass, Vite on the **host** | Final host ports in [PORTS.md](../PORTS.md) (e.g. Gateway `28080`, Frontend `25173`, RabbitMQ AMQP `25672` / UI `25673`, Auth Mongo `27017`) |
| Service → service **inside Compose** | Container DNS + internal ports (e.g. `auth-mongo:27017`, `rabbitmq:5672`, `supplier-service:28084`) — **not** `localhost` |

Spring app listen ports match published host ports (`9000`, `28080`, `28084`–`28087`). Mongo and RabbitMQ map host ports onto container `27017` / `5672` / `15672`.

## 🏗 System architecture

```mermaid
flowchart TB
  subgraph Client["Presentation"]
    FE["React SPA host :25173"]
  end
  subgraph Edge["Edge"]
    GW["API Gateway :28080<br/>JWT · rate limit · X-API-KEY inject"]
  end
  subgraph Identity["Identity"]
    AUTH["Auth Server :9000<br/>Google OAuth2 · JWT · JWKS"]
  end
  subgraph Domain["Domain services"]
    SUP["Supplier :28084"]
    MAT["Material :28085"]
    PAY["Payment :28086"]
    ORD["Order & Inventory :28087"]
  end
  subgraph Data["Data (host ports → container :27017)"]
    M1[(auth-mongo host :27017)]
    M2[(material-mongo host :27020)]
    M3[(supplier-mongo host :27021)]
    M4[(payment-mongo host :27022)]
    M5[(order-mongo host :27023)]
  end
  subgraph Bus["Messaging"]
    RMQ["RabbitMQ host AMQP :25672 / UI :25673<br/>inside rabbitmq:5672"]
  end
  FE -->|OAuth| AUTH
  FE -->|Bearer /api| GW
  GW --> AUTH
  GW --> SUP & MAT & PAY & ORD
  AUTH --> M1
  SUP --> M3
  MAT --> M2
  PAY --> M4
  ORD --> M5
  ORD <--> RMQ
  PAY <--> RMQ
  SUP -.-> RMQ
```

## 🧩 Microservice architecture

```mermaid
flowchart LR
  GW[api-gateway] --> AUTH[auth-server]
  GW --> SUP[supplier-service]
  GW --> MAT[material-service]
  GW --> PAY[payment-service]
  GW --> ORD[order-inventory-service]
  SUP -->|publish supplier.*| X[buildmate.exchange]
  ORD -->|order.created| X
  PAY -->|consume order.created| X
  PAY -->|payment.completed| X
  ORD -->|consume payment.completed| X
```

## 🐳 Deployment architecture

```mermaid
flowchart TB
  subgraph net["buildmate-network"]
    AM[auth-mongo] --- AS[auth-server]
    MM[material-mongo] --- MS[material-service]
    SM[supplier-mongo] --- SS[supplier-service]
    PM[payment-mongo] --- PS[payment-service]
    OM[order-mongo] --- OS[order-inventory-service]
    RQ[rabbitmq] --- SS & PS & OS
    AS & SS & MS & PS & OS --- AG[api-gateway]
    AG --- BC[buildmate-client<br/>profile: full]
  end
```

## 🔐 OAuth flow

```mermaid
sequenceDiagram
  participant U as User
  participant FE as React :25173
  participant AS as Auth :9000
  participant G as Google
  U->>FE: /login
  FE->>AS: /oauth2/authorization/google
  AS->>G: Consent
  G-->>AS: Code
  AS->>AS: Upsert user + issue JWT
  AS-->>FE: /oauth/callback?token=...
  FE->>FE: Store Bearer token
```

## 🔏 JWT flow

```mermaid
sequenceDiagram
  participant FE as React
  participant GW as Gateway
  participant AS as Auth JWKS
  participant Svc as Domain service
  FE->>GW: Authorization Bearer JWT
  GW->>AS: Fetch JWKS / validate
  GW->>GW: Inject X-API-KEY
  GW->>Svc: Proxied request + API key
  Svc-->>GW: Response
  GW-->>FE: Response
```

## 🚪 API Gateway flow

```mermaid
flowchart LR
  REQ["HTTP /api/..."] --> SEC{"JWT valid?"}
  SEC -->|no| E401[401]
  SEC -->|yes| RL[Rate limiter]
  RL --> ROUTE{Route}
  ROUTE -->|/api/suppliers/**| SUP
  ROUTE -->|/api/materials|brands|categories| MAT
  ROUTE -->|/api/payments|invoices|reports| PAY
  ROUTE -->|/api/orders|inventory|cart|health| ORD
  ROUTE -->|/api/auth/**| AUTH
  SUP & MAT & PAY & ORD & AUTH --> REWRITE[RewritePath strip /api]
  REWRITE --> KEY[AddRequestHeader X-API-KEY]
  KEY --> UP[Upstream service]
```

## 📨 RabbitMQ payment flow

```mermaid
sequenceDiagram
  participant ORD as Order Service
  participant X as buildmate.exchange
  participant PAY as Payment Service
  ORD->>X: OrderCreatedEvent (order.created)
  X->>PAY: order.created.queue
  Note over PAY: OrderCreatedListener (logs)
  PAY->>X: PaymentCompletedEvent (payment.completed)
  X->>ORD: payment.completed.queue
  ORD->>ORD: PaymentCompletedListener → markPaid → PAID
```

## 📦 Container architecture

| Container | Role | Profile |
|-----------|------|---------|
| `auth-mongo` … `order-mongo` | Dedicated MongoDB | `api`, `full` |
| `rabbitmq` | Broker + management | `api`, `full` |
| `auth-server` … `order-inventory-service` | Spring APIs | `api`, `full` |
| `api-gateway` | Edge | `api`, `full` |
| `buildmate-client` | nginx SPA | `full` only |

Network: **`buildmate-network`** (bridge).

## Ports summary (host-facing)

| Port | Component |
|-----:|-----------|
| 25173 | Frontend |
| 28080 | Gateway |
| 9000 | Auth |
| 28084–28087 | Supplier / Material / Payment / Order |
| 27017, 27020–27023 | Mongo hosts (Auth on **27017**) |
| 25672 / 25673 | RabbitMQ AMQP / Management UI |
