# syntax=docker/dockerfile:latest

FROM alpine:latest
RUN apk add telegraf gcompat jq

COPY target/influxdb-exploration /app/app
COPY docker/telegraf/telegraf.conf /app/

CMD ["sleep", "infinity"]
