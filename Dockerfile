# Stage 1: Build JVM uberjar
FROM clojure:temurin-17-tools-deps AS jvm-builder
WORKDIR /app
COPY deps.edn build.clj ./
RUN clojure -P
RUN clojure -P -T:build

COPY src src
COPY resources resources
# The pre-compiled shadow-cljs output (resources/public/js) is already in the host filesystem 
# from the previous step, so COPY resources grabs it.

RUN clojure -T:build uber

# Stage 2: Runtime (Distroless)
FROM gcr.io/distroless/java17-debian12:nonroot
WORKDIR /app
COPY --from=jvm-builder /app/target/argus.jar /app/argus.jar
EXPOSE 8080 8081
CMD ["/app/argus.jar"]
