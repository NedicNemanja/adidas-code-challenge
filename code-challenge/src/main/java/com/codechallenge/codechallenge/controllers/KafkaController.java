package com.codechallenge.codechallenge.controllers;

import com.codechallenge.codechallenge.services.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/kafka")
public class KafkaController {

    private final Producer producer;

    @Autowired
    public KafkaController(Producer producer) {
        this.producer = producer;
    }

    @PostMapping(value = "/writeApi")
    public void sendMessageToKafkaTopic(@RequestParam("message") String message) {
        //TODO: validate message (json parse,)
        this.producer.sendMessage(message);
    }

}