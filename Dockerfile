FROM clojure:lein-alpine AS builder
COPY . /app
WORKDIR /app
RUN lein ring uberjar


FROM openjdk:8-jre-alpine
COPY --from=builder /app/target/server.jar /app/server.jar
COPY ./.prod-config.edn /app/config.edn
COPY ./.prod-smtp.properties /app/smtp.properties

EXPOSE 3000

WORKDIR /app
ENTRYPOINT [ "java", \
             "-Dconf=config.edn", \
             "-jar", \
             "server.jar" ]

