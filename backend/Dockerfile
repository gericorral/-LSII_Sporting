FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY backend/pom.xml backend/pom.xml
RUN mvn -f backend/pom.xml dependency:go-offline -B

COPY backend backend
COPY frontend frontend

RUN mvn -f backend/pom.xml -Pprod -DskipTests package -B

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/backend/target/protube-back-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]