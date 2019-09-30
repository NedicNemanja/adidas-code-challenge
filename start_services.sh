#!/bin/bash

./bin/zookeeper-server-start ./etc/kafka/zookeeper.properties &
zoo_pid=$!
status=$?
if [ $status -ne 0 ]; then
  echo "Failed to start zookeeper: $status"
  exit $status
else
  echo "zoo_pid: $zoo_pid"
fi

./bin/kafka-server-start ./etc/kafka/server.properties &
kafka_pid=$!
status=$?
if [ $status -ne 0 ]; then
  echo "Failed to start kafka: $status"
  exit $status
else
  echo "kafka_pid: $kafka_pid"
fi

./bin/schema-registry-start ./etc/schema-registry/schema-registry.properties &
schreg_pid=$!
status=$?
if [ $status -ne 0 ]; then
  echo "Failed to start schema-registry: $status"
  exit $status
else
  echo "schema_reg_pid: $schreg_pid"
fi

./bin/kafka-rest-start ./etc/kafka-rest/kafka-rest.properties &
krest_pid=$!
status=$?
if [ $status -ne 0 ]; then
  echo "Failed to start kafka-rest: $status"
  exit $status
else
  echo "kafka_rest_pid: $krest_pid"
fi


./bin/connect-standalone ./etc/schema-registry/connect-json-standalone.properties ./etc/kafka-connect-jdbc/mysink.properties &
connect_pid=$!
status=$?
if [ $status -ne 0 ]; then
  echo "Failed to start kafka-connect-jdbc-sink: $status"
  exit $status
else
  echo "connect_pid: $connect_pid"
fi

java -jar ./app.jar
app_pid=$!
status=$?
if [ $status -ne 0 ]; then
  echo "Failed to start spring app: $status"
  exit $status
else
  echo "app_pid: $app_pid"
fi
