FROM alpine:latest
RUN apk add telegraf gcompat

COPY target/influxdb-exploration /app/app
COPY docker/telegraf/telegraf.conf /app/

CMD ["sleep", "infinity"]
