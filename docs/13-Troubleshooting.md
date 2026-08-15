# 13 — Troubleshooting

## MongoDB

| Symptom | Likely cause | Action |
|---------|--------------|--------|
| Connection refused | Mongo not listening on expected port | Check host ports **27017** / **27020–27023** (Auth on **27017**); Compose health. Inside Docker use `*-mongo:27017` |
| Empty `api_keys` → 401 on Payment/Order | Init script only runs on first volume | Reseed from `docker/mongo-init/**` or insert key manually |
| Wrong DB name | URI mismatch | Confirm `*_db` names in URI |
| Data “lost” after `down -v` | Volumes removed | Recreate stack; accept re-seed |

---

## RabbitMQ

| Symptom | Likely cause | Action |
|---------|--------------|--------|
| App fails startup AMQP | Wrong host/user/pass | Compose apps use host `rabbitmq` and port **5672** inside the network; host UI/AMQP are **25673** / **25672**. Credentials come from `.env` |
| No paid transition | Event not published / wrong status | Complete payment so `PaymentCompletedEvent` fires; check listeners |
| Queues missing | Services not started | Start Order + Payment so declarations run |
| UI login fails | Credential mismatch | Match Compose `RABBITMQ_DEFAULT_*` from `.env` |

---

## Docker

| Symptom | Likely cause | Action |
|---------|--------------|--------|
| Client missing | Used `api` profile only | Use `--profile full` |
| Gateway unhealthy | Upstream still starting | Wait for dependencies `service_healthy`; check logs |
| Auth OAuth fails | Missing Google config mount | Provide `auth-server/config` local secrets from `.env` / local yaml |
| Port already allocated | Host conflict | Stop conflicting process or change host binds (config change not covered here) |

```bash
docker compose --profile api logs -f <service>
docker compose --profile api ps
```

---

## Gateway

| Symptom | Likely cause | Action |
|---------|--------------|--------|
| 401 on all `/api/*` | Missing/expired JWT | Re-login; check Bearer header |
| 401 on health mistakenly protected | Wrong path | Use `/actuator/health` or `/api/auth/health` |
| 502 / connection refused upstream | Service URL/DNS | In Docker use container names; verify `*_SERVICE_URL` |
| JWKS fetch errors | `JWT_JWK_SET_URI` unreachable | Must reach `auth-server:9000/oauth2/jwks` inside network |

---

## OAuth

| Symptom | Likely cause | Action |
|---------|--------------|--------|
| redirect_uri mismatch | Google console vs Auth config | Align Google client redirect URIs with Auth Server |
| Stuck on login | Missing client id/secret | Set env or local yaml (from `.env`) |
| Callback without token | Auth failure path | Check Auth logs; failure URL `/login` on frontend host **25173** |

---

## JWT

| Symptom | Likely cause | Action |
|---------|--------------|--------|
| Gateway 401, Auth `/me` 200 | Clock/issuer mismatch | Issuer `http://localhost:9000`; verify token `iss` |
| Signature invalid after rebuild | New RSA key volume | Expected if `auth-rsa-data` recreated — re-login |
| SPA “logged in” but API 401 | Token not attached | Check Axios interceptor / localStorage |

---

## Frontend

| Symptom | Likely cause | Action |
|---------|--------------|--------|
| API calls fail CORS | Hitting service port directly | Use `/api` via Gateway |
| Proxy errors in dev | Gateway down | Start Gateway on host `:28080` |
| Blank after login | Callback parse issue | Inspect `/oauth/callback` query params |
| Theme resets | Cleared storage | Re-toggle; key `buildmate_theme` |

---

## API keys

| Symptom | Likely cause | Action |
|---------|--------------|--------|
| Direct call 401 | Wrong header name/value | Header must be `X-API-KEY` |
| Gateway OK, direct Payment 401 | Key not in Mongo | Seed payment `api_keys` |
| Supplier works with env only | Env fallback present | Payment/Order lack env fallback |

---

## Related

- [11-Installation-Guide](./11-Installation-Guide.md)
- [17-Known-Issues](./17-Known-Issues.md)
