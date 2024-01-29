# Personal InfluxDB exploration project

## Running InfluxDB

Do a local install or use [Docker](https://docs.influxdata.com/influxdb/v2/install/?t=Docker) using the following command to set up a default user and organization: 

    docker run --env=DOCKER_INFLUXDB_INIT_USERNAME=test-user --env=DOCKER_INFLUXDB_INIT_PASSWORD=test-password --env=DOCKER_INFLUXDB_INIT_MODE=setup --env=DOCKER_INFLUXDB_INIT_BUCKET=test-bucket --env=DOCKER_INFLUXDB_INIT_ORG=test-org --env=DOCKER_INFLUXDB_INIT_CLI_CONFIG_NAME=default --volume c:/temp/influxdb2-data:/var/lib/influxdb2 -p 8086 --name InfluxDB -d influxdb:2.7.5 

Note: if you do not want to persist your data outside the container, omit the `--volume` flag. 

## Running Telegraf

[Telegraf](https://www.influxdata.com/time-series-platform/telegraf/) is the agent that collect and send metrics to InfluxDB.

Install options: 

    nix-env --install telegraf
    snap install telegraf --classic

or [download and install](https://www.influxdata.com/time-series-platform/telegraf/#) locally.

### Configuring Telegraf

Basically you configure a number of input, aggregator, processor and output [plugins](https://docs.influxdata.com/telegraf/v1/plugins/).
There are many plugins for example input plugins that can read output from a script `ExecD` or `http` that can periodically  pull a http address.

In this project there is an example of using the [Grok](https://docs.influxdata.com/telegraf/v1/data_formats/input/grok/) input plugin
for sending the content of a log file to InfluxDB. See the file [telegraf.conf](telegraf.conf).

One very important bit is to configure the timestamp correctly as shown. Note that throughout this project the InfluxDB uses UTC time zone 
as that is the default configured for the docker image.

When starting out configuring Telegraf you can specify only one output plugin as shown below such that all output is sent
to stdout: 

    [[outputs.file]]
        files = ["stdout"]

### Deploying Telegraf

A common scenario and best practise is to have multiple Telegraf instances output to a message queue and have one Telegraf instance
read from that message queue and output the content to InfluxDB. In this way the architecture is more resilient to outages and 
buffer overflow if data cannot be written to InfluxDB.

## Applying templates

Templates contain everything from dashboards and Telegraf configurations to notifications and alerts in a single manifest file.
See the github [community-templates](https://github.com/influxdata/community-templates/tree/master) repository for prepackaged InfluxDB configurations. 

### Example

In the InfluxDB UI go to `Settings` -> `Templates` and paste the url: `https://github.com/influxdata/community-templates/tree/master/linux_system` in the URL field and press `lookup template`.

Go to `Load Data` -> `Telegraf` -> `Linux System Monitoring` -> `Setup Instructions` -> `Generate New API Token` and copy the API Token and 
the Telegraf start command and paste it to a `telegraf.sh` file.

See the [template readme](https://github.com/influxdata/community-templates/blob/master/linux_system/readme.md) for additional setup instructions, typically
the `INFLUX_HOST` and `INFLUX_ORG` environment variables needs to be specified as in this case. Add these two lines to `telegraf.sh` so the complete file will be similar to this:

    export INLFUX_ORG=TEST_ORG
    export INFLUX_HOST=http://localhost:8086
    export INFLUX_TOKEN=YUxTSd72wHJEGTDQ_tRyaVn1XDFqXY96uMRrRQdN6YVgqRZlDs8VX77KvjF8MXT3dlx6sVMf6ZfBORXJmAwe1Q==  
    telegraf --config http://localhost:8086/api/v2/telegrafs/0c812ba1299bc000

Execute the file and watch the `Linux System` dashboard light up.
