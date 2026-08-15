# =============================================================================
# BuildHub — Supplier Service
# docker build -t buildmate-supplier .
# Runtime env:
#   MONGODB_URI, RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_USERNAME,
#   RABBITMQ_PASSWORD, RABBITMQ_VHOST, SUPPLIER_API_KEY, MATERIAL_SERVICE_URL, MATERIAL_API_KEY
# Listens on 28084.
# =============================================================================

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package \
    && cp target/supplier-service-0.0.1-SNAPSHOT.jar /workspace/app.jar

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

LABEL org.opencontainers.image.title="buildmate-supplier" \
      org.opencontainers.image.description="BuildHub Supplier Service" \
      org.opencontainers.image.vendor="BuildHub"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system buildmate \
    && useradd --system --gid buildmate --home-dir /app --shell /usr/sbin/nologin buildmate \
    && chown -R buildmate:buildmate /app

COPY --from=build --chown=buildmate:buildmate /workspace/app.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0" \
    SERVER_PORT=28084

USER buildmate
EXPOSE 28084

HEALTHCHECK --interval=20s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fsS http://127.0.0.1:28084/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
