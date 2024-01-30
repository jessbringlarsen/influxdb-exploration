$env:INFLUX_TOKEN=""
$env:INFLUX_HOST="http://localhost:8086"
$env:INFLUX_ORG="test-org"
$env:INFLUX_BUCKET="performance-bucket"

telegraf.exe --config telegraf.conf