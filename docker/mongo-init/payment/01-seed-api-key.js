// Seed API key for Payment Service (runs only on first empty volume)
db = db.getSiblingDB('payment_db');
db.api_keys.updateOne(
  { keyValue: 'buildmate-payment-key' },
  {
    $set: {
      keyValue: 'buildmate-payment-key',
      active: true,
      createdAt: new Date()
    }
  },
  { upsert: true }
);
