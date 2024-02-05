export INFLUX_TOKEN="RQWlUg-S5QtgcPieJHU_WgWTDtDbqobm5nHF9txTxcpI9stRpg4yZCFp3hZmmna9Lnau-_Fn8haHO779iuujiw=="
export INFLUX_HOST="http://localhost:8086"
export INFLUX_ORG="test-org"
export INFLUX_BUCKET="performance-bucket"

telegraf --config telegraf.conf
