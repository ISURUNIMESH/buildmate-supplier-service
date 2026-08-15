# 08 — RabbitMQ Documentation

Broker (Compose) — host ports in [`PORTS.md`](../PORTS.md):

| Setting | Value |
|---------|-------|
| AMQP (host) | `localhost:25672` → container `rabbitmq:5672` |
| Management UI | `http://localhost:25673` |
| Credentials | `${RABBITMQ_USERNAME}` / `${RABBITMQ_PASSWORD}` from `.env` (required) |
| VHost | `${RABBITMQ_VHOST:-/}` |
| Image | `rabbitmq:3-management` |

**RabbitMQ is a core BuildHub component** — do not remove or replace with REST-only flows.

Services using RabbitMQ: **Supplier** (publish), **Material** (consume supplier + publish material), **Order/Inventory** (publish + consume), **Payment** (publish + consume). **Auth** and **API Gateway** do not use RabbitMQ (sync HTTP only — by design).

---

## Exchanges

| Exchange | Type | Notes |
|----------|------|-------|
| `buildmate.exchange` | **Topic** | Primary domain events |
| `payment.exchange` | **Direct** | Legacy `payment.created` path |

---

## Queues, bindings, consumers

| Queue | Routing keys | Exchange | Consumer | Business effect |
|-------|--------------|----------|----------|-----------------|
| `material.supplier.events.queue` | `supplier.created`, `supplier.updated`, `supplier.status.changed`, `supplier.deleted` | `buildmate.exchange` | Material `SupplierEventListener` | Log / observe linked materials (no cascade delete) |
| `order.inventory.material.events.queue` | `material.created`, `material.updated`, `material.stock.updated`, `material.deleted` | `buildmate.exchange` | Order `MaterialEventListener` | Auto-create inventory; sync stock; no cascade delete |
| `order.created.queue` | `order.created` | `buildmate.exchange` | Payment `OrderCreatedListener` | Log only (payment still via REST) |
| `payment.completed.queue` | `payment.completed` | `buildmate.exchange` | Order `PaymentCompletedListener` | Order → **PAID** |
| `payment.queue` | `payment.created` | `payment.exchange` | **None** | Legacy / unused publisher path — retained, not deleted |

---

## Primary flows

```text
Supplier.*  → buildmate.exchange → material.supplier.events.queue → Material
Material.*  → buildmate.exchange → order.inventory.material.events.queue → Inventory projection
Order.created → buildmate.exchange → order.created.queue → Payment (log)
Payment.completed → buildmate.exchange → payment.completed.queue → Order PAID
```

---

## Live verification

```powershell
.\scripts\master-audit.ps1
```

Confirms publish → consume → Mongo effects, and RabbitMQ restart consumer reconnect.
