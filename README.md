# adidas-code-challenge

![alt text](https://github.com/NedicNemanja/adidas-code-challenge/blob/master/adidas-code-challenge.png "the stack")

## Run

### Run docker image

Find the docker image at https://cloud.docker.com/u/nedicnem/repository/docker/nedicnem/adidas-code-challenge

Please expose port 9000 when running the image (```docker run --name youcontainername nedicnem/adidas-code-challenge -p 9000:9000```).

Give the container some time (~10sec) to start all services.

### Create an entry in the database

To create an entry in the database you need a POST request to localhost:9000/kafka/writeApi with a request parameter "message" consisting of json data.

minimal json data example: {"id":"Bk1250"}

You can use the following command from your terminal:

```
curl --data "message={   \"id\": \"CG7088\",   \"name\": \"Nite Jogger Shoes\",   \"model_number\": \"BTO93\",   \"product_type\": \"inline\",   \"meta_data\": {     \"page_title\": \"adidas Nite Jogger Shoes -Black | adidas UK\",     \"site_name\": \"adidas United Kingdom\",     \"description\": \"Shop for Nite\",     \"keywords\": \"Nite Jogger Shoes\",     \"canonical\": \"//www.adidas.co.uk/nite-jogger-shoes/CG7088.html\"   },   \"pricing_information\": {     \"standard_price\": 119.95,     \"standard_price_no_vat\": 99.96,     \"currentPrice\": 119.95   },   \"product_description\": {     \"title\": \"Nite Jogger Shoes\",     \"subtitle\": \"Modern cushioning updates this flashy '80s standout.\",     \"text\": \"Inspired\"   } }" localhost:9000/kafka/writeApi
```

### Query the database

The Spring Boot app provides the ProductApi which returns all the items in the database at ip:9000/kafka/productApi

In order to find the ip of your container run:

```
docker inspect youcontainername
```

Find the ip under NetworkSettings.IPAddress.

Once you have the ip (ie. 172.17.0.2) just visit http://172.17.0.2:9000/kafka/productApi from your browser.
 
### Query a specific entry in the database by id

In a similar fashion if you visit http://172.17.0.2:9000/kafka/productApi/BK1250, where BK1250 is the id of an existing record. You will get a respone with all the columns of that record.

### Connect to the container and inspect

If you wish to connect to the container while it running and inspect the database or the services you can use:

```docker exec -it youcontainername /bin/bash```

# Implementation

Commentary about implementation descisions and their flaws which (I promise) I would not replicate in a prod environment :)

## writeApi -> Kafka
Pretty simple. One producer litening and publishing to topic "adidas". If the message received is not parasble but json then ignore it.

For simplicity's sake I decided to use JsonDeserializer for the producer.


Flaw: doesn't validate the schema of the message. Allowing for all kinds of entries to the database.

## Kafka -> database

For this purpose I used Kafka-Connect-JDBC as a sink for my SQLite database.

The reason I selected SQLite is because connect-jdbc requires less configuration than other databases, plus its pretty lightweight for demo purposes like in our case.

connect-standalone configuration:
```
bootstrap.servers=localhost:9092

key.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schema.registry.url=http://localhost:8081
value.converter=org.apache.kafka.connect.json.JsonConverter
value.converter.schema.registry.url=http://localhost:8081

internal.key.converter=org.apache.kafka.connect.json.JsonConverter
internal.value.converter=org.apache.kafka.connect.json.JsonConverter
internal.key.converter.schemas.enable=true
internal.value.converter.schemas.enable=true

offset.storage.file.filename=/tmp/connect.offsets

plugin.path=share/java
```

sink configuration:
```
name=test-sink
connector.class=io.confluent.connect.jdbc.JdbcSinkConnector
tasks.max=1

topics=adidas

connection.url=jdbc:sqlite:test.db
auto.create=true
#pk.fields=id
#insert.mode=upsert
```

The connector polls data from Kafka to write to the database based on the topics subscription. Data from the topic is converted using JsonConverter with the schema supplied by the producer in a JSON envelope along with the payload.

JsonEnvelope example (here you can see the schema as well):
```
{"schema": {
  "type": "struct",
  "fields": [
    {
      "type": "string",
      "field": "id",
      "optional": false
    },
    {
      "type": "string",
      "field": "name",
      "optional": true
    },
    {
      "type": "string",
      "field": "model_number",
      "optional": true
    },
    {
      "type": "string",
      "field": "product_type",
      "optional": true
    },
    {
      "type": "string",
      "field": "meta_data",
      "optional": true
    },
    {
      "type": "string",
      "field": "pricing_information",
      "optional": true
    },
    {
      "type": "string",
      "field": "product_description",
      "optional": true
    }
  ]
}, "payload": {...}}
```

So when a message is written by the producer into the topic, the connect-jdbc driver picks it up and tries to use the schema to populate the SQLite database. Note that if no table exists this will create a new table based on the schema, and since there is no name provided it will by default take the topic name "adidas".

Simple right?
Thats what I thought when I read the confluent documentation about connect-jdbc sink. But as it seems it does NOT support nesting of json fields. I'm pretty sure it can be done, either by message transform, flattening or a different schema.
Same goes for the insert.mode=upsert. Ideally  I would want to have the product.id as the primary key and whenever I get a duplicate the do an update of that record in the database. But honestly after trying many different configuration combinations, docs and tutorials I was running out of time and i decided to bail and revisit when I have time, I need to read more about sink schemas.

## DB -> productApi

SpringBoot connects to SQLite using JDBC. A GET request arrives, then query is performed and results returned.

JDBC suitable for low-latency and high-load applications? Nope but it was very simple to write. If we acutally need super low latency then we can use Speedment in-JVM-memory acceleration. 
(https://dzone.com/articles/java-shortest-code-and-lowest-latency)
(https://dzone.com/articles/ultra-low-latency-querying-with-java-streams-and-i)

## Docker

I used one container for all services. This is not the best practice but is easy for demonstration purposes. In order to make this scalable I suggest one container per service. I managed to run all of them in a single container using a bash script.
