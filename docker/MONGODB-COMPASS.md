# MongoDB Compass — BuildHub connections

Import file:

```text
docker/mongodb-compass-connections.json
```

## Import into Compass

1. Start BuildHub Mongo containers:

```powershell
docker compose --profile api up -d auth-mongo material-mongo supplier-mongo payment-mongo order-mongo
```

2. Open **MongoDB Compass**
3. Click **…** (or **Favorites**) → **Import saved connections**
4. Select `docker/mongodb-compass-connections.json`
5. Connect to each favorite

## Manual connection strings

| Name | URI (from Windows host) |
|------|-------------------------|
| Auth | `mongodb://localhost:27017/buildmate_auth_db` |
| Material | `mongodb://localhost:27020/material_db` |
| Supplier | `mongodb://localhost:27021/supplier_db` |
| Payment | `mongodb://localhost:27022/payment_db` |
| Order/Inventory | `mongodb://localhost:27023/order_inventory_db` |

### Inside Docker vs host

| Context | URI form |
|---------|----------|
| Container → Mongo | `mongodb://<service-name>:27017/<db>` |
| Windows host / Compass | `mongodb://localhost:<host-port>/<db>` |

Auth Mongo host port is **27017**. Material/Supplier/Payment/Order use **27020–27023**.
