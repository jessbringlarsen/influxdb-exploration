FROM ghcr.io/graalvm/graalvm-community:21 as builder

WORKDIR /tmp
COPY target/influxdb-exploration.jar /tmp/influxdb-exploration.jar
RUN jar xvf influxdb-exploration.jar
RUN native-image -H:+UnlockExperimentalVMOptions dk.bringlarsen.influxdbexploration.SpringShellApplication -o app -cp .:BOOT-INF/classes:`find BOOT-INF/lib | tr '\n' ':'`

FROM alpine:latest
RUN apk add telegraf gcompat

COPY  --from=builder /tmp/app /app/
COPY environments/local/telegraf/telegraf.conf /app/
COPY environments/local/telegraf/telegraf-start.sh /app/

CMD sh -c /app/telegraf-start.sh