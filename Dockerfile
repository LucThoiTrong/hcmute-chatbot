FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy file pom để tải thư viện.
COPY pom.xml .

# Lệnh này tải các thư viện về máy ảo, lần sau build sẽ nhanh hơn.
# Tải toàn bộ thư viện về trước.
# -B = batch note (Không hỏi).
RUN mvn dependency:go-offline -B

# Copy toàn bộ src code vào.
COPY src ./src

# Dùng lệnh build => tạo ra file .jar và lưu trong target.
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Lệnh chạy app
ENTRYPOINT ["java", "-jar", "app.jar"]

