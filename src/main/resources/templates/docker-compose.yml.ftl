# =============================================
# ${entityName} — Docker Compose
# Uses global scaffold containers started by
# the Scaffold Generator on boot.
# Run: docker-compose up -d
# =============================================
<#assign needsMySQL = (database == "mysql") />
<#assign needsKafka = (kafkaProducer?? && (kafkaProducer || kafkaConsumer)) />

services:
  ${entityNameLower}-app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: ${entityNameLower}-app
    ports:
      - "${assignedPort}:${assignedPort}"
    environment:
      - SPRING_PROFILES_ACTIVE=default
      - SERVER_PORT=${assignedPort}
<#if needsMySQL>
      - SPRING_DATASOURCE_URL=jdbc:mysql://scaffold-mysql:3306/${entityNameLower}db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      - SPRING_DATASOURCE_USERNAME=said
      # Password hidden: not stored in the repository. Export SPRING_DATASOURCE_PASSWORD before running docker compose.
      - SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD:?set SPRING_DATASOURCE_PASSWORD}
</#if>
<#if needsKafka>
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=scaffold-kafka:9092
      - SPRING_KAFKA_LISTENER_MISSING_TOPICS_FATAL=false
</#if>
    networks:
      - scaffold-net
    restart: on-failure
<#if needsMySQL || needsKafka>
    depends_on:
<#if needsMySQL>
      - scaffold-mysql-check
</#if>
<#if needsKafka>
      - scaffold-kafka-check
</#if>
</#if>

<#if needsMySQL>
  # Health check — verifies global MySQL is reachable
  scaffold-mysql-check:
    image: mysql:8.0
    container_name: ${entityNameLower}-mysql-check
    # Password hidden: taken from the environment variable SPRING_DATASOURCE_PASSWORD (never stored in the file).
    environment:
      - MYSQL_PWD=${SPRING_DATASOURCE_PASSWORD:?set SPRING_DATASOURCE_PASSWORD}
    command: >
      bash -c "i=0; until mysqladmin ping -h scaffold-mysql -u said --silent; do
        i=$$((i+1)); if [ $$i -ge 15 ]; then echo 'MySQL timeout — continuing anyway'; exit 0; fi;
        echo 'Waiting for MySQL... ($$i/15)'; sleep 3; done; echo 'MySQL is ready'"
    networks:
      - scaffold-net
    restart: "no"

</#if>
<#if needsKafka>
  # Health check — verifies global Kafka is reachable
  scaffold-kafka-check:
    image: confluentinc/cp-kafka:7.5.0
    container_name: ${entityNameLower}-kafka-check
    command: >
      bash -c "i=0; until kafka-topics --bootstrap-server scaffold-kafka:9092 --list > /dev/null 2>&1; do
        i=$$((i+1)); if [ $$i -ge 15 ]; then echo 'Kafka timeout — continuing anyway'; exit 0; fi;
        echo 'Waiting for Kafka... ($$i/15)'; sleep 3; done; echo 'Kafka is ready'"
    networks:
      - scaffold-net
    restart: "no"

</#if>
networks:
  scaffold-net:
    external: true
    name: scaffold-net
