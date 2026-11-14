# Etapa 0: compila Tailwind CSS
FROM node:22-alpine AS css
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY src ./src
RUN npm run build:css

# Etapa 1: compila el WAR con Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
COPY --from=css /app/src/main/webapp/assets/css/tailwind.css ./src/main/webapp/assets/css/tailwind.css
COPY database ./database
RUN mvn -q clean package -DskipTests

# Etapa 2: Tomcat 10.1 (jakarta.* / Servlet 6.0)
FROM tomcat:10.1-jdk21-temurin
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
