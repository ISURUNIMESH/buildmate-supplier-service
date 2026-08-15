import net from 'net'
import { spawn } from 'child_process'

const port = Number(process.env.PROXY_PORT || 5050)

function waitForPort(p, attempts = 50) {
  return new Promise((resolve, reject) => {
    let left = attempts
    const tryOnce = () => {
      const socket = net.connect({ host: '127.0.0.1', port: p }, () => {
        socket.end()
        resolve()
      })
      socket.on('error', () => {
        left -= 1
        if (left <= 0) reject(new Error(`Proxy did not start on port ${p}`))
        else setTimeout(tryOnce, 200)
      })
    }
    tryOnce()
  })
}

await waitForPort(port)
const child = spawn('npx', ['vite'], { stdio: 'inherit', shell: true })
child.on('exit', (code) => process.exit(code ?? 0))
