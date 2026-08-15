// Seed API key for Order & Inventory Service (runs only on first empty volume)
db = db.getSiblingDB('order_inventory_db');
db.api_keys.updateOne(
  { keyValue: 'buildmate-order-key' },
  {
    $set: {
      keyValue: 'buildmate-order-key',
      active: true,
      createdAt: new Date()
    }
  },
  { upsert: true }
);
