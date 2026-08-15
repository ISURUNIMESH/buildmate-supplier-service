# 01 — Project Overview

> **BuildHub** is a Service-Oriented Computing (SOC) academic microservices platform for construction marketplace operations.

---

## 🎯 Purpose

BuildHub demonstrates a production-style microservice architecture for a construction materials marketplace: supplier onboarding, material catalog, inventory control, ordering, payments, invoicing, and operational reporting.

## 🎯 Objectives

| Objective | How BuildHub addresses it |
|-----------|----------------------------|
| Decompose domain into services | Separate Auth, Gateway, Supplier, Material, Payment, Order & Inventory |
| Secure edge & service access | Google OAuth2 → BuildHub JWT at Gateway; `X-API-KEY` per domain service |
| Async cross-service workflow | RabbitMQ `OrderCreated` / `PaymentCompleted` events |
| Independent data storage | One MongoDB instance (and database) per service |
| Operable deployment | Docker multi-stage images + Compose profiles `api` / `full` |
| Usable admin UI | React + Vite SPA for all major business domains |

## 📋 Business problem

Construction buyers and operators need a consistent way to:

- manage **supplier** partners and compliance documents;
- maintain a **materials** catalog (with brands/categories);
- **order** stock while tracking **inventory**;
- process **payments** and generate **invoices**;
- view **reports** on revenue and customers.

A monolithic approach couples all of these concerns. BuildHub separates them into independently deployable Spring Boot services coordinated by an API Gateway and event bus.

## ✅ Solution

```text
Browser (React)
    │ Google OAuth → Auth Server (:9000) → BuildHub JWT
    │ Bearer JWT → API Gateway (:28080)
    │                       │ injects X-API-KEY
    ├─ Supplier (:28084)  Material (:28085)  Payment (:28086)  Order (:28087)
    └─ MongoDB ×5 + RabbitMQ (buildmate.exchange)
```

Host ports above are for browser/`localhost` access. See [PORTS.md](../PORTS.md) and [02-System-Architecture](./02-System-Architecture.md) for Docker-internal vs host mappings.

## ✨ Main features (implemented)

| Area | Features present in code |
|------|--------------------------|
| Identity | Google OAuth login, RSA JWT, JWKS, `/api/auth/me`, `/api/auth/health` |
| Gateway | Path routing, JWT validation, CORS, rate limiting, API-key injection |
| Suppliers | Register/login, CRUD update DTO, status, rating, documents, top-rated, events |
| Materials | Materials/brands/categories CRUD, stock/price patch, search, low-stock |
| Orders | Create/list/get/status/delete, by user/status |
| Cart | Add item, get by user, clear cart |
| Inventory | Create/list, reserve/release, history |
| Payments | Create/list/history/status/refund/retry, invoices, revenue/monthly/top-customer reports |
| Messaging | Order↔Payment paid workflow; supplier domain publish; payment.created on payment.exchange |
| Frontend | Login, dashboard, suppliers, materials, orders, cart, inventory, payments, invoices, reports |
| Ops | Actuator health, Swagger on domain services, Docker Compose profiles |

## 📚 Related docs

| Doc | Description |
|-----|-------------|
| [02-System-Architecture](./02-System-Architecture.md) | Diagrams & topology |
| [03-Microservices](./03-Microservices.md) | Per-service responsibilities |
| [11-Installation-Guide](./11-Installation-Guide.md) | How to run |
| [16-System-Verification-Report](./16-System-Verification-Report.md) | Verified runtime evidence |
