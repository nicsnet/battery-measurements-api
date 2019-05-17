FROM clojure

WORKDIR /build
COPY ./project.clj /build
RUN lein with-profile uberjar deps


COPY . /build
RUN lein uberjar

FROM java:8-alpine
RUN apk add --no-cache curl

COPY --from=0 /build/target/uberjar/battery-measurements-api.jar /battery-measurements-api/app.jar
COPY --from=0 /build/docker-start.sh /battery-measurements-api/docker-start.sh

EXPOSE 3000

WORKDIR /battery-measurements-api
CMD ["./docker-start.sh"]
