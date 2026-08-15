// Seed API key for Material Service (runs only on first empty volume)
db = db.getSiblingDB('material_db');
db.api_keys.updateOne(
  { keyValue: 'buildmate-material-key' },
  {
    $set: {
      keyValue: 'buildmate-material-key',
      clientName: 'BuildMateClient',
      active: true,
      createdAt: new Date()
    }
  },
  { upsert: true }
);
