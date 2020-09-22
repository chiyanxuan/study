# 自己写一个start

- 1、创建一个maven工程

例如demo中的

Redissonnil-spring-boot-start

- 2、引入依赖

  ```xml
  <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter</artifactId>
              <version>2.1.2.RELEASE</version>
              <optional>true</optional>
          </dependency>
  
          <dependency>
              <groupId>org.redisson</groupId>
              <artifactId>redisson</artifactId>
              <version>3.13.1</version>
              <optional>true</optional>
          </dependency>
  ```

optional设置为true，依赖不会向下传递，这样我们的条件加载才有意义

- 3、读取用户的配置

  ```java
  @ConfigurationProperties(prefix = "nil.redisson")
  @Data
  public class RedissonProperties {
      private String host = "locahost";
      private int port = 6379;
      private int timeout;
      private boolean ssl;
  
  }
  ```

  这里的ConfigurationProperties注解就是读取用户的配置，prefix表明读取那些配置

- 4、写configuration类

  ```java
  @Configuration
  @EnableConfigurationProperties(RedissonProperties.class)  //将RedissonProperties引入
  @ConditionalOnClass(Redisson.class)  // 有Redisson这个类的时候才会装配
  public class RedissionAutoConfiguration {
  
  	@Bean
  	RedissonClient redissonClient(RedissonProperties redissonProperties){
  		Config config = new Config();
  		String prefix = "redis://";
  		if(redissonProperties.isSsl()){
  			prefix = "rediss://";
  		}
  		SingleServerConfig singleServerConfig = config.useSingleServer()
  				.setAddress(prefix+redissonProperties.getHost()+":"+redissonProperties.getPort())
  				.setConnectTimeout(redissonProperties.getTimeout());
  		return Redisson.create(config);
  
  	}
  }
  ```

  这里面EnableConfigurationProperties注解将刚才读取的配置注入过来

  ConditionalOnClass注解表示条件装配，只有用户加载了Redisson这个类才会装配此configuration里面的bean

  类里面是需要装配的bean

- 5、配置spring.factories

  ```properties
  org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
    com.nil.starterdemo.RedissionAutoConfiguration
  ```

  这样springboot才能找到我们的RedissionAutoConfiguration配置类并加载里面的bean

- 6、maven install打jar包





# 使用自己写的starter

- 1、创建一个springboot工程

  如demo

- 2、添加依赖

  ```xml
  <dependency>
              <artifactId>redissonnil-spring-boot-start</artifactId>
              <groupId>org.example</groupId>
              <version>1.0-SNAPSHOT</version>
          </dependency>
  
          <dependency>
              <groupId>org.redisson</groupId>
              <artifactId>redisson</artifactId>
              <version>3.13.1</version>
          </dependency>
  ```

  要将自己刚刚写的starter引入

  并将条件加载里面的类所在的jar引入，否则条件不满足不会加载

- 3、写配置文件

  ```properties
  nil.redisson.host=127.0.0.1
  nil.redisson.port=6379
  ```

  这里只是例子，写的比较少，正式的starter肯定有很多配置

- 4、使用starter

  ```java
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
  ```

  使用我们starter里面的bean连接redis，并获取一个值

- 5、启动springboot工程

- 6、访问这个接口http://127.0.0.1:8080/getName进行验证

  

