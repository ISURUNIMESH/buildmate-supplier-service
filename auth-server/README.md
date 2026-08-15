# BuildHub Auth Server — Google OAuth2 + JWT

Technical module name remains `auth-server` / `buildmate` packages.

## Google Cloud Console setup

1. Create OAuth 2.0 Client ID (Web application)
2. Authorized redirect URI:
   `http://localhost:9000/login/oauth2/code/google`
3. Authorized JavaScript origins (optional):
   `http://localhost:25173`
4. Export credentials into local config / `.env` (never commit secrets):

```bash
set GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
set GOOGLE_CLIENT_SECRET=your-client-secret
```

## Run

- MongoDB: `mongodb://localhost:27017/buildmate_auth_db`
- `mvnw spring-boot:run`

Auth server: http://localhost:9000  
Google start URL: http://localhost:9000/oauth2/authorization/google

## Flow

1. React Login → `/oauth2/authorization/google`
2. Google authenticates user
3. `OAuth2LoginSuccessHandler` upserts Mongo user and issues **BuildHub JWT** (RSA)
4. Redirect → `http://localhost:25173/oauth/callback?token=...`
5. React stores JWT and calls Gateway with `Authorization: Bearer ...`

## API (authenticated)

| Method | Path | Notes |
|--------|------|--------|
| GET | `/api/auth/health` | Public |
| GET | `/api/auth/me` | Current user from JWT |
| GET | `/api/auth/users` | **Admin only** — list users |

Admin check: JWT `roles` claim contains `ADMIN` / `ROLE_ADMIN`.

## Promote a local user to admin

```powershell
# From repo root (auth Mongo on localhost:27017)
.\scripts\promote-admin.ps1 -Email "you@example.com"
```

Then sign out and sign in again so the new JWT includes `ROLE_ADMIN`.
