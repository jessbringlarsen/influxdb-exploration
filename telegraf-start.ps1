$env:INFLUX_TOKEN=""
$env:INFLUX_HOST="http://localhost:8086"
$env:INFLUX_ORG="Consid"
$env:INFLUX_BUCKET="performance-bucket"

telegraf.exe --config http://localhost:8086/api/v2/telegrafs/0c6be43812873000