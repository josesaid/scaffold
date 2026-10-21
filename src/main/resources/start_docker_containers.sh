#!/bin/bash
# =============================================
# Spring Boot Scaffold Generator
# Global Docker Containers Startup Script
# =============================================

echo "============================================="
echo " Starting global Docker containers..."
echo "============================================="
echo ""

# ── Create shared network if not exists ──────
docker network inspect scaffold-net >/dev/null 2>&1 || \
  docker network create scaffold-net && echo "[INFO] Network scaffold-net ready"

# ── Helper function ───────────────────────────
start_container() {
  local name=$1
  local run_cmd=$2

  if docker ps --format '{{.Names}}' | grep -q "^${name}$"; then
    echo "[INFO] ${name} already running — skipping"
  elif docker ps -a --format '{{.Names}}' | grep -q "^${name}$"; then
    echo "[INFO] ${name} exists but stopped — starting..."
    docker start "${name}" && echo "[OK] ${name} started" || echo "[ERROR] ${name} failed to start"
  else
    echo "[INFO] Creating and starting ${name}..."
    eval "${run_cmd}" && echo "[OK] ${name} started" || echo "[ERROR] ${name} failed to start"
  fi
}

# ── Wait for a container port to be ready ─────
wait_for_port() {
  local name=$1
  local host=$2
  local port=$3
  local timeout=${4:-30}
  local elapsed=0

  echo "[INFO] Waiting for ${name} on port ${port}..."
  until docker exec "${name}" bash -c "echo > /dev/tcp/${host}/${port}" 2>/dev/null; do
    sleep 2
    elapsed=$((elapsed + 2))
    if [ "${elapsed}" -ge "${timeout}" ]; then
      echo "[WARN] Timeout waiting for ${name} — continuing anyway"
      return 1
    fi
  done
  echo "[OK] ${name} is ready"
}

# ── MySQL ──────────────────────────────────────
start_container "scaffold-mysql" "docker run -d \
  --name scaffold-mysql \
  --network scaffold-net \
  -e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:?set MYSQL_ROOT_PASSWORD} \
  -e MYSQL_USER=said \
  -e MYSQL_PASSWORD=${MYSQL_PASSWORD:?set MYSQL_PASSWORD} \
  -p 3306:3306 \
  -v scaffold_mysql_data:/var/lib/mysql \
  mysql:8.0"

# ── PostgreSQL ─────────────────────────────────
start_container "scaffold-postgres" "docker run -d \
  --name scaffold-postgres \
  --network scaffold-net \
  -e POSTGRES_USER=said \
  -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD:?set POSTGRES_PASSWORD} \
  -p 5432:5432 \
  -v scaffold_postgres_data:/var/lib/postgresql/data \
  postgres:15-alpine"

# ── Zookeeper ──────────────────────────────────
start_container "scaffold-zookeeper" "docker run -d \
  --name scaffold-zookeeper \
  --network scaffold-net \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  -e ZOOKEEPER_TICK_TIME=2000 \
  -p 2181:2181 \
  confluentinc/cp-zookeeper:7.5.0"

# ── Wait for Zookeeper before starting Kafka ──
if docker ps --format '{{.Names}}' | grep -q "^scaffold-zookeeper$"; then
  wait_for_port "scaffold-zookeeper" "localhost" "2181" 30
fi

# ── Kafka ──────────────────────────────────────
start_container "scaffold-kafka" "docker run -d \
  --name scaffold-kafka \
  --network scaffold-net \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=scaffold-zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://scaffold-kafka:9092,PLAINTEXT_HOST://localhost:29092 \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -e KAFKA_AUTO_CREATE_TOPICS_ENABLE=true \
  -p 9092:9092 \
  -p 29092:29092 \
  confluentinc/cp-kafka:7.5.0"

echo ""
echo "============================================="
echo " Done. Global containers status:"
echo "============================================="
docker ps --filter "name=scaffold-" --format "  {{.Names}}\t{{.Status}}"
echo ""
