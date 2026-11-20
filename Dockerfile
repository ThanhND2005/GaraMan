FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src/ src/
RUN mvn -B package

FROM tomcat:9.0-jdk21
COPY --from=build /build/target/garaman-1.0.0.war /usr/local/tomcat/webapps/ROOT.war
