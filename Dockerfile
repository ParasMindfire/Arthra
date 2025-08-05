# STAGE 1: Build the JAR inside Docker
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN ./mvnw dependency:resolve                    
COPY src ./src  
RUN ./mvnw clean package -DskipTests          

# STAGE 2: Copy JAR from Stage 1 to runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/Arthra-0.0.1-SNAPSHOT.jar moneymanager-v1.0.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","moneymanager-v1.0.jar"]
