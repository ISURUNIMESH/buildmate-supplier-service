// Seed API key for Supplier Service (runs only on first empty volume)
db = db.getSiblingDB('supplier_db');
db.api_keys.updateOne(
  { keyValue: 'buildmate-supplier-key' },
  {
    $set: {
      keyValue: 'buildmate-supplier-key',
      clientName: 'BuildMateClient',
      active: true,
      createdAt: new Date()
    }
  },
  { upsert: true }
);
