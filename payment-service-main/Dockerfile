# =============================================================================
# BuildHub — Payment Service
# docker build -t buildmate-payment .
# Runtime env:
#   MONGODB_URI, RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_USERNAME,
#   RABBITMQ_PASSWORD, RABBITMQ_VHOST, PAYMENT_API_KEY
# Listens on 28086.
# =============================================================================

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package \
    && cp target/payment-service-0.0.1-SNAPSHOT.jar /workspace/app.jar

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

LABEL org.opencontainers.image.title="buildmate-payment" \
      org.opencontainers.image.description="BuildHub Payment Service" \
      org.opencontainers.image.vendor="BuildHub"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system buildmate \
    && useradd --system --gid buildmate --home-dir /app --shell /usr/sbin/nologin buildmate \
    && chown -R buildmate:buildmate /app

COPY --from=build --chown=buildmate:buildmate /workspace/app.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0" \
    SERVER_PORT=28086

USER buildmate
EXPOSE 28086

HEALTHCHECK --interval=20s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fsS http://127.0.0.1:28086/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
