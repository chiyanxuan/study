package com.nil.starterdemo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

//用户配置在yaml文件里的配置以nil.redisson开头的就会读到这里来加载
@ConfigurationProperties(prefix = "nil.redisson")
@Data
public class RedissonProperties {
    private String host = "locahost";
    private int port = 6379;
    private int timeout;
    private boolean ssl;

}
