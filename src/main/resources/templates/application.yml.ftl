spring:
  application:
    name: ${entityNameLower}
<#if database == "h2">
  datasource:
    url: jdbc:h2:mem:${entityNameLower}db;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true
<#elseif database == "mysql">
  datasource:
    url: jdbc:mysql://localhost:3306/${entityNameLower}db?useSSL=false&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: your_password_here
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
<#elseif database == "postgresql">
  datasource:
    url: jdbc:postgresql://localhost:5432/${entityNameLower}db
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: your_password_here
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
<#elseif database == "mongodb">
  data:
    mongodb:
      uri: mongodb://localhost:27017/${entityNameLower}db
</#if>
<#if kafkaProducer?? && (kafkaProducer || kafkaConsumer)>
  kafka:
    bootstrap-servers: localhost:29092
    # bootstrap-servers: scaffold-kafka:9092  # use inside Docker
    consumer:
      group-id: ${entityNameLower}-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
    listener:
      missing-topics-fatal: false
</#if>

server:
  port: ${assignedPort!"8080"}


<#if includeSwagger>
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    try-it-out-enabled: true
  packages-to-scan: ${basePackage}.${entityNameLower}.controller
</#if>
logging:
  level:
    ${basePackage}: DEBUG
    org.springframework.web: INFO
