#!/bin/bash

set -e

# Responsible for creating influxdb tokens and the Grafana Influxdb Datasource
while true; do
  RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "http://grafana:3000/api/orgs/1" -H "Authorization: Basic YWRtaW46YWRtaW4tcGFzc3dvcmQ=" || true)

  if [ "$RESPONSE" -eq 200 ]; then
    echo "Grafana is up!"
    break
  else
    echo "Waiting for Grafana..."
    sleep 1
  fi
done

# Create Influxdb token and Grafana dashboard
export INFLUXDB_TOKEN=$(influx auth create --read-buckets -d grafana | grep grafana | awk {'print $3'})
curl -X POST -s -o response.txt --location "http://grafana:3000/api/datasources" -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4tcGFzc3dvcmQ=" \
    -d "{
          \"uid\": \"b8cb4326-8b98-49d6-8a9c-839e5c32a6a3\",
          \"orgId\": 1,
          \"name\": \"InfluxDB\",
          \"type\": \"influxdb\",
          \"typeLogoUrl\": \"/public/app/plugins/datasource/influxdb/img/influxdb_logo.svg\",
          \"access\": \"proxy\",
          \"url\": \"http://influxdb:8086\",
          \"user\": \"\",
          \"database\": \"\",
          \"basicAuth\": false,
          \"basicAuthUser\": \"\",
          \"withCredentials\": false,
          \"isDefault\": true,
          \"jsonData\": {
            \"defaultBucket\": \"performance-bucket\",
            \"httpMode\": \"POST\",
            \"organization\": \"test-org\",
            \"version\": \"Flux\"
          },
          \"secureJsonData\": {
            \"token\": \"$INFLUXDB_TOKEN\"
          },
          \"version\": 1,
          \"readOnly\": false
        }"

# Create Telegraf API token
influx auth create --write-buckets -d telegraf
