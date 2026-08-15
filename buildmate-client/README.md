# BuildHub Client

React admin UI for **BuildHub** with Google OAuth → JWT → API Gateway.

Technical folder name remains `buildmate-client`.

## Auth flow

1. Open http://localhost:25173/login
2. Continue with Google → Auth Server (`:9000`)
3. Auth Server upserts Mongo user and issues RSA JWT
4. Redirect to `/oauth/callback` → store token
5. All `/api/*` calls go through Vite/nginx → Gateway `:28080` with `Authorization: Bearer <token>`
6. Gateway validates JWT and injects microservice `X-API-KEY`

## Run (Docker full stack)

```bash
docker compose --profile full up -d --build
```

Open http://localhost:25173

## Run (native Vite)

1. Stack/Gateway available on `http://localhost:28080`
2. Auth on `http://localhost:9000`
3. `cd buildmate-client && npm run dev:web` (port **25173**)

Google redirect URI must be:

`http://localhost:9000/login/oauth2/code/google`

Frontend must use Gateway base `/api` (proxied to `http://localhost:28080`). Do **not** call `:28084–28087` directly from the browser.

## Scripts

- `npm run dev:web` — Vite only (recommended with Gateway)
- `npm run build` — production build
- `npm run dev` — legacy Express API-key proxy (optional fallback)
