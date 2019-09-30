FROM openjdk:8

RUN apt-get update && apt-get install -y sqlite

EXPOSE 9000

COPY ./confluent-community-5.3.1-2.12/confluent-5.3.1 /usr/src/app/

COPY ./code-challenge/build/libs/code-challenge-0.0.1-SNAPSHOT.jar /usr/src/app/app.jar
COPY ./code-challenge/src/main/resources/schema.json /usr/src/app/src/main/resources/schema.json

COPY ./start_services.sh /usr/src/app/start_services.sh
ENTRYPOINT ["bash","-c", "chmod +x ./start_services.sh"]

WORKDIR /usr/src/app

ENTRYPOINT ["bash","-c", "./start_services.sh"]
