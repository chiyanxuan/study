package com.nil.example.demo;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private RedissonClient redissonClient;

    @GetMapping("/getName")
    public String getName(){
        RBucket bucket = redissonClient.getBucket("name");
        if(bucket.get() == null){
            bucket.set("nil");
        }
        return bucket.get().toString();
    }


}
