import { MongoClient } from 'mongodb'

const uri = process.env.MONGO_URI || 'mongodb://127.0.0.1:27017'
const client = new MongoClient(uri)

const keys = [
  { db: 'supplier_db', key: 'buildmate-supplier-key', clientName: 'Swagger' },
  { db: 'material_db', key: 'buildmate-material-key', clientName: 'BuildMateClient' },
  { db: 'payment_db', key: 'buildmate-payment-key', clientName: 'BuildMateClient' },
  { db: 'order_inventory_db', key: 'buildmate-order-key', clientName: 'BuildMateClient' },
]

await client.connect()
const now = new Date()

for (const k of keys) {
  const col = client.db(k.db).collection('api_keys')
  const existing = await col.findOne({ keyValue: k.key })
  if (existing) {
    await col.updateOne(
      { _id: existing._id },
      { $set: { active: true, clientName: k.clientName } },
    )
    console.log('updated', k.db)
  } else {
    await col.insertOne({
      keyValue: k.key,
      clientName: k.clientName,
      active: true,
      createdAt: now,
    })
    console.log('inserted', k.db)
  }
}

await client.close()
console.log('API keys seeded on', uri)
