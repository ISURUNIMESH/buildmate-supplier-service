# =============================================================================
# BuildHub Client (React/Vite → nginx)
# docker build -t buildmate-client .
#
# Production SPA serves on port 80 (host maps 25173:80). Vite /api base stays
# relative (/api) and is proxied by nginx to api-gateway:28080 (Docker DNS).
# Auth URLs are browser-facing host ports (see PORTS.md).
# =============================================================================

FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY index.html ./
COPY public ./public
COPY src ./src
COPY vite.config.js ./
ARG VITE_API_BASE=/api
ARG VITE_AUTH_SERVER_URL=http://localhost:9000
ARG VITE_GOOGLE_LOGIN_URL=http://localhost:9000/oauth2/authorization/google
ENV VITE_API_BASE=$VITE_API_BASE \
    VITE_AUTH_SERVER_URL=$VITE_AUTH_SERVER_URL \
    VITE_GOOGLE_LOGIN_URL=$VITE_GOOGLE_LOGIN_URL
RUN npm run build

FROM nginx:1.27-alpine
LABEL org.opencontainers.image.title="buildmate-client" \
      org.opencontainers.image.description="BuildHub React frontend (nginx)" \
      org.opencontainers.image.vendor="BuildHub"

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
HEALTHCHECK --interval=20s --timeout=5s --start-period=15s --retries=5 \
  CMD wget -qO- http://127.0.0.1/ | grep -qi html || exit 1

CMD ["nginx", "-g", "daemon off;"]
