export INFLUX_TOKEN=""
export INFLUX_HOST="http://localhost:8086"
export INFLUX_ORG="test-org"
export INFLUX_BUCKET="performance-bucket"

telegraf --config telegraf.conf
