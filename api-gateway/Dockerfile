# =============================================================================
# BuildHub — API Gateway
# docker build -t buildmate-gateway .
# Runtime env: JWT_ISSUER_URI, JWT_JWK_SET_URI, downstream service URLs, API keys
# Do not bake secrets into the image.
# Listens on 28080 (host and container).
# =============================================================================

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package \
    && cp target/api-gateway-0.0.1-SNAPSHOT.jar /workspace/app.jar

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

LABEL org.opencontainers.image.title="buildmate-gateway" \
      org.opencontainers.image.description="BuildHub API Gateway (JWT reverse proxy)" \
      org.opencontainers.image.vendor="BuildHub"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system buildmate \
    && useradd --system --gid buildmate --home-dir /app --shell /usr/sbin/nologin buildmate \
    && chown -R buildmate:buildmate /app

COPY --from=build --chown=buildmate:buildmate /workspace/app.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0" \
    SERVER_PORT=28080

USER buildmate
EXPOSE 28080

HEALTHCHECK --interval=20s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fsS http://127.0.0.1:28080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
