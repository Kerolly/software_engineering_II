# Backend stand-in for the SCRUM-237 hosting test. Not the production backend image.
# It builds the backend from this branch and starts it without a database, so its cold start on
# Render can be measured next to the grader probe. Build context: the repository root.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY backend/ ./
RUN chmod +x mvnw && ./mvnw -q -B -DskipTests package \
 && cp target/*.jar /tmp/app.jar

FROM eclipse-temurin:25-jre
COPY --from=build /tmp/app.jar /opt/backend/app.jar
# No database: Flyway off, Hibernate starts without reading JDBC metadata, the database health
# check off. The pool connects lazily, so the dummy URL is never used. A real start with Neon
# adds the connection, Flyway and the schema validation on top of the time measured here.
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:5432/standin \
    SPRING_DATASOURCE_USERNAME=standin \
    SPRING_FLYWAY_ENABLED=false \
    SPRING_JPA_HIBERNATE_DDL_AUTO=none \
    SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect \
    SPRING_JPA_PROPERTIES_HIBERNATE_BOOT_ALLOW_JDBC_METADATA_ACCESS=false \
    MANAGEMENT_HEALTH_DB_ENABLED=false \
    JAVA_OPTS="-XX:+UseSerialGC -XX:MaxRAMPercentage=70 -Xss512k -XX:+ExitOnOutOfMemoryError"
USER 10001
EXPOSE 10000
CMD ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT:-10000} -jar /opt/backend/app.jar"]
