# ============================================================
# Master Computer Academy – Certificate Verification System
# Multi-stage Dockerfile
#
# Stage 1 (builder) – compiles the project and extracts the
#                     Spring Boot layered JAR for optimal caching.
# Stage 2 (runtime) – minimal JRE image; only the app layers
#                     are copied in, keeping the final image small.
#
# Build :  docker build -t mca-cert-api .
# Run   :  docker run --rm -p 8080:8080 \
#            -e SPRING_PROFILES_ACTIVE=prod \
#            -e DB_URL=... \
#            -e DB_USERNAME=... \
#            -e DB_PASSWORD=... \
#            -e JWT_SECRET=... \
#            -e FRONTEND_URL=https://mastercomputeracademy.org \
#            mca-cert-api
#
# On Render the PORT variable is injected automatically.
# All secrets must be supplied as environment variables –
# NEVER hardcode credentials in this file.
# ============================================================


# ──────────────────────────────────────────────────────────
# Stage 1 – build
# eclipse-temurin is the canonical, actively maintained
# OpenJDK distribution recommended for production containers.
# We use the JDK image here (needed to run Maven + javac).
# ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

# ── Dependency layer (cached unless pom.xml changes) ──────
# Copy only the POM first so that Maven dependency resolution
# is cached as a separate Docker layer. Source files changing
# will NOT bust this layer.
COPY pom.xml .

# Download all dependencies into the local Maven repository.
# The -B flag runs in non-interactive (batch) mode.
# The -q flag suppresses download progress noise in build logs.
RUN mvn -B -q dependency:go-offline -f pom.xml

# ── Application source ────────────────────────────────────
COPY src ./src

# ── Build & test ──────────────────────────────────────────
# Runs the full test suite (H2 in-memory – no external DB needed).
# Produces: target/certificate-verification-1.0.0.jar
RUN mvn -B package -Dspring.profiles.active=test \
        -DADMIN_PASSWORD=docker-build-placeholder \
        -DJWT_SECRET=docker-build-placeholder-secret-that-is-long-enough

# ── Extract Spring Boot layered JAR ───────────────────────
# Spring Boot 3.x packages the fat JAR in layers:
#   dependencies        – third-party libs (rarely change)
#   spring-boot-loader  – Spring Boot launcher (rarely changes)
#   snapshot-dependencies – SNAPSHOT libs
#   application         – your compiled classes (changes every build)
#
# Extracting them separately means Docker only re-pushes the
# "application" layer on every code change – the large lib
# layers are served from cache.
RUN java -Djarmode=layertools \
         -jar target/certificate-verification-1.0.0.jar \
         extract --destination target/extracted


# ──────────────────────────────────────────────────────────
# Stage 2 – runtime
# JRE-only image: no compiler, no javadoc, no Maven.
# eclipse-temurin:17-jre-jammy ≈ 250 MB vs the JDK at ≈ 400 MB.
# ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy AS runtime

# Run as a non-root user – defence-in-depth for container security.
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

WORKDIR /app

# Copy the four extracted layers in dependency-stability order
# (least likely to change first → better layer caching on Render).
COPY --from=builder --chown=appuser:appgroup \
     /workspace/target/extracted/dependencies/ ./

COPY --from=builder --chown=appuser:appgroup \
     /workspace/target/extracted/spring-boot-loader/ ./

COPY --from=builder --chown=appuser:appgroup \
     /workspace/target/extracted/snapshot-dependencies/ ./

COPY --from=builder --chown=appuser:appgroup \
     /workspace/target/extracted/application/ ./

USER appuser

# ── Port ──────────────────────────────────────────────────
# Render injects $PORT at container startup.
# The EXPOSE directive is documentation only; the actual port
# is controlled by the -Dserver.port JVM argument at runtime.
EXPOSE 8080

# ── JVM runtime flags ─────────────────────────────────────
# -XX:+UseContainerSupport      honours cgroup CPU/memory limits
#                                (default ON in Java 11+, explicit for clarity)
# -XX:MaxRAMPercentage=75.0     use up to 75 % of the container's RAM for
#                                the heap; leave headroom for Metaspace,
#                                thread stacks, and the OS.
# -Djava.security.egd=...       faster SecureRandom on Linux containers
#                                (avoids /dev/random blocking on entropy).
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom"

# ── Startup ───────────────────────────────────────────────
# server.port: reads $PORT (injected by Render) with 8080 as fallback.
# spring.profiles.active: always "prod" inside a container.
#   Override at runtime with -e SPRING_PROFILES_ACTIVE=staging if needed.
#
# Uses the Spring Boot JarLauncher directly (not `java -jar`) so the
# layered class-path is resolved correctly.
ENTRYPOINT ["sh", "-c", \
  "exec java $JAVA_OPTS \
     -Dserver.port=${PORT:-8080} \
     -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} \
     org.springframework.boot.loader.launch.JarLauncher"]
