# =============================================================================
# BuildHub — Auth Server
# docker build -t buildmate-auth .
# Runtime env (never bake secrets into the image):
#   MONGODB_URI / SPRING_DATA_MONGODB_URI
#   GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET
#   JWT / OAuth settings via Spring config only
# Listens on 9000.
# =============================================================================

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package \
    && cp target/auth-server-0.0.1-SNAPSHOT.jar /workspace/app.jar

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

LABEL org.opencontainers.image.title="buildmate-auth" \
      org.opencontainers.image.description="BuildHub Auth Server (OAuth2 + JWT)" \
      org.opencontainers.image.vendor="BuildHub"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system buildmate \
    && useradd --system --gid buildmate --home-dir /app --shell /usr/sbin/nologin buildmate \
    && mkdir -p /app/.buildmate \
    && chown -R buildmate:buildmate /app

COPY --from=build --chown=buildmate:buildmate /workspace/app.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

USER buildmate
EXPOSE 9000

HEALTHCHECK --interval=20s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fsS http://127.0.0.1:9000/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
