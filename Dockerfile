# ═══════════════════════════════════════════════════════════
# STAGE 1 — BUILD (Maven + Node.js per Angular)
# ═══════════════════════════════════════════════════════════
FROM eclipse-temurin:17-jdk-jammy AS builder

# Installa Node.js 22 e npm
RUN apt-get update && apt-get install -y curl gnupg2 && \
    curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia i descrittori di dipendenza prima del codice
# (ottimizza la cache Docker: se pom.xml non cambia, Maven non riscaricare tutto)
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./
RUN chmod +x mvnw

COPY package.json package-lock.json ./

# Installa dipendenze npm
RUN npm ci

# Copia il codice sorgente completo
COPY src/ src/
COPY angular.json tsconfig.json tsconfig.app.json tsconfig.spec.json ngsw-config.json ./
COPY webpack/ webpack/
COPY sonar-project.properties ./

# Build completo: Angular embedded + Spring Boot JAR con profilo prod
# -Dmodernizer.skip=true → disabilita il controllo stile Java (non rilevante per il deploy)
RUN ./mvnw -Pprod clean package -DskipTests -Dmodernizer.skip=true -B

# ═══════════════════════════════════════════════════════════
# STAGE 2 — RUNTIME (solo JRE, immagine leggera ~250MB)
# ═══════════════════════════════════════════════════════════
FROM eclipse-temurin:17-jre-jammy

# Installa curl per l'healthcheck
RUN apt-get update && apt-get install -y curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Utente non-root per sicurezza
RUN groupadd --gid 1001 boardroom && \
    useradd --uid 1001 --gid boardroom --shell /bin/bash --create-home boardroom

WORKDIR /app

# Copia il JAR dallo stage di build
COPY --from=builder --chown=boardroom:boardroom \
     /app/target/*.jar app.jar

USER boardroom

EXPOSE 8080

# Healthcheck interno al container
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
  CMD curl -f http://localhost:8080/management/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]