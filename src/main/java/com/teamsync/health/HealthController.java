package com.teamsync.health;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController{
    private final MongoTemplate mongoTemplate;

    public HealthController(MongoTemplate mongoTemplate){
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/api/health")
    public Map<String, String> health(){
        try{
            mongoTemplate.getDb().runCommand(new Document("ping", 1));
            return Map.of("status","UP","database","CONNECTED");
        } catch(Exception e){
            return Map.of("status","UP","database","DISCONNECTED");
        }
    }
}