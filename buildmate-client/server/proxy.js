import express from 'express'
import cors from 'cors'
import dotenv from 'dotenv'
import http from 'http'
import https from 'https'
import path from 'path'
import { fileURLToPath } from 'url'
import { URL } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
dotenv.config({ path: path.join(__dirname, '..', '.env') })

const PORT = Number(process.env.PROXY_PORT || 5050)

const services = {
  'supplier-service': {
    target: process.env.SUPPLIER_SERVICE_URL || 'http://localhost:28084',
    apiKey: process.env.SUPPLIER_API_KEY || '',
  },
  'material-service': {
    target: process.env.MATERIAL_SERVICE_URL || 'http://localhost:28085',
    apiKey: process.env.MATERIAL_API_KEY || '',
  },
  'payment-service': {
    target: process.env.PAYMENT_SERVICE_URL || 'http://localhost:28086',
    apiKey: process.env.PAYMENT_API_KEY || '',
  },
  'order-inventory-service': {
    target: process.env.ORDER_INVENTORY_SERVICE_URL || 'http://localhost:28087',
    apiKey: process.env.ORDER_API_KEY || '',
  },
}

const app = express()
app.use(cors())
app.use(express.raw({ type: '*/*', limit: '10mb' }))

app.get('/api/health', (_req, res) => {
  res.json({ status: 'UP', proxy: true, services: Object.keys(services) })
})

Object.entries(services).forEach(([name, cfg]) => {
  const prefix = `/api/${name}`
  app.use(prefix, (req, res) => {
    const suffix = req.originalUrl.slice(prefix.length) || '/'
    const targetUrl = new URL(suffix, cfg.target.endsWith('/') ? cfg.target : `${cfg.target}/`)

    const headers = { ...req.headers, host: targetUrl.host }
    delete headers['content-length']
    if (cfg.apiKey) headers['x-api-key'] = cfg.apiKey

    const lib = targetUrl.protocol === 'https:' ? https : http
    const upstream = lib.request(
      {
        protocol: targetUrl.protocol,
        hostname: targetUrl.hostname,
        port: targetUrl.port || (targetUrl.protocol === 'https:' ? 443 : 80),
        path: `${targetUrl.pathname}${targetUrl.search}`,
        method: req.method,
        headers,
        timeout: 15000,
      },
      (upRes) => {
        res.status(upRes.statusCode || 502)
        Object.entries(upRes.headers).forEach(([k, v]) => {
          if (k.toLowerCase() === 'transfer-encoding') return
          if (v !== undefined) res.setHeader(k, v)
        })
        upRes.pipe(res)
      },
    )

    upstream.on('error', (err) => {
      if (!res.headersSent) {
        res.status(502).json({
          error: 'Upstream unavailable',
          service: name,
          message: err.message,
        })
      }
    })

    upstream.on('timeout', () => {
      upstream.destroy()
      if (!res.headersSent) {
        res.status(504).json({ error: 'Upstream timeout', service: name })
      }
    })

    if (req.method === 'GET' || req.method === 'HEAD' || req.method === 'DELETE') {
      upstream.end()
    } else if (Buffer.isBuffer(req.body) && req.body.length) {
      upstream.end(req.body)
    } else {
      req.pipe(upstream)
    }
  })
})

app.listen(PORT, () => {
  console.log(`[buildmate-proxy] listening on http://localhost:${PORT}`)
  Object.entries(services).forEach(([name, cfg]) => {
    console.log(`  /api/${name} → ${cfg.target} (X-API-KEY ${cfg.apiKey ? 'set' : 'MISSING'})`)
  })
})
