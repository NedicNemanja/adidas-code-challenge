package com.codechallenge.codechallenge.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.json.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class Producer {

    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    private static final String TOPIC = "adidas";
    private static String SCHEMA = null;

    static {
        try {
            //read schema from file
            SCHEMA = new String(Files.readAllBytes(Paths.get("src/main/resources/schema.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Check if input string is of json format.
     * @param test
     * @return true if string is of json format, else false
     */
    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create an json envelope with the schema and the payload.
     * @param payload message
     * @return json envelope of format {"schema": {...}, "payload": {...}}
     */
    private String jsonEnvelope(String payload) {
        String envelope = "{ \"schema\": " + SCHEMA + ",\"payload\":" + payload + "}";
        logger.info("ENVELOPE: \n"+envelope);
        return envelope;
    }

    /**
     * Publish a message to the topic only if its json
     * @param message json format payload
     */
    public void sendMessage(String message) {
        if (isJSONValid(message)) {
            logger.info(String.format("$$ -> Producing message --> %s", message));
            this.kafkaTemplate.send(TOPIC, jsonEnvelope(message));
        }
        else {
            logger.info(String.format("Got message which is not json: --> %s", message));
        }
    }
}