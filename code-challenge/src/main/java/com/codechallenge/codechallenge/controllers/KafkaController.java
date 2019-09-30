package com.codechallenge.codechallenge.controllers;

import com.codechallenge.codechallenge.SqliteDB;
import com.codechallenge.codechallenge.services.Producer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/kafka")
public class KafkaController {
    private static final Logger logger = LoggerFactory.getLogger(KafkaController.class);
    private final Producer producer;

    @Autowired
    public KafkaController(Producer producer) {
        this.producer = producer;
    }

    private Connection conn = SqliteDB.connect();

    /**
     * Check if input string is of json format.
     *
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

    @PostMapping(value = "/writeApi")
    public void sendMessageToKafkaTopic(@RequestParam("message") String message) {
        if (isJSONValid(message)) {
            this.producer.sendMessage(message);
        } else {
            logger.info(String.format("Got message which is not json: --> %s", message));
        }
    }

    @GetMapping(value = "/productApi")
    public List<String> getAllProducts() throws SQLException {
        String sql = "SELECT * FROM adidas;";
        List<String> ret = new ArrayList<>();
        Connection conn = SqliteDB.connect();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        // loop through the result set
        while (rs.next()) {
            ret.add(rs.getString("id"));
        }
        return ret;
    }

    @GetMapping("/productApi/{id}")
    public Map<String,String> getUsersById(@PathVariable(value = "id") String id) throws SQLException {
        String sql = String.format("SELECT * FROM adidas WHERE id='%s';", id);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        ResultSetMetaData rsmd = rs.getMetaData();

        Map<String,String> ret = new HashMap<>();
        while (rs.next()) {
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                ret.put(rsmd.getColumnName(i),rs.getString(i));
            }
        }
        return ret;
    }

}