

# Spring bean的声明

- 最初是配置在配置文件里面的，配置bean的name，实现的class，属性
- 然后可以使用注解，创建配置类，在上面加上@Configuration，里面用@Bean注解，再把配置类的路径用下面这两个注解告知spring
- 直接使用@Controller、@RestController、@Service、@Companent等注解放在类上就表明是bean需要加载



# Spring静态Bean的加载方式

- springBoot自动能加载启动类同目录的bean

- 创建配置类，在上面加上@Configuration，里面用@Bean注解，再把配置类的路径用下面这两个注解告知spring

- 不同目录的bean可以通过

  在SpringBoot工程在启动类上加上

  ```java
  @ComponentScan(basePackages= {"com.xxxx"})
  ```

  或者

  ```java
  @SpringBootApplication(scanBasePackages = {"com.tencent"})
  ```

  两个注解来告诉spring去这些目录扫描带有@Controller、@RestController、@Service、@Companent等注解的类并加载



# Spring 的动态Bean的装载

但是有的bean我们是不知道路径的，就没办法使用静态类的方式来进行配置，就需要动态加载方式

- importSelector:DeferredImportSelector
- Registator:ImportBeanDefinitionRegistar



# SpringFactoriesLoader

springboot在依赖里面加上starter，这些bean就会被加载到spring容器里面，如何实现的呢？

即starter的机制

例如

- redisstarter，里面有@Configration注解表明的配置类，配置类里面@Bean实现bean注入

- 在classpath：META-INF/spring-factories文件里面的表明配置类的地址

  ```
  org.springframework.boot.autoconfigure.EnableAutoConfiguration=xx.xx.xx.RedisXXConfiguration
  ```

  

- springBoot会扫描所有的META-INF/spring-factories，找到这些配置类，加载里面的bean





# SPI

Service provider interface

## 基本概念

服务提供接口，这个机制是Springboot自动装配的核心理念

springboot提供一个规范（接口），第三方去进行具体的实现（starter），然后我们就可以使用了



## demo实现

1、需要有个接口，给出规范

​      例如数据库连接驱动，例如demo里面的database-driver，里面写了个接口DataBaseDriver，这就是规范



2、实现规范

​     例如mysql-driver假设是mysql驱动的实现，

    - 首先需要在pom.xml里面添加database-driver依赖
    - 实现规范，即实现DataBaseDriver这个接口（MsqlDriver），并实现里面的方法，即mysql驱动的实际内容
    - 在resources目录下创建META_INF.services目录
    - 在META_INF.services创建一个文件，这个文件名是database-driver中DataBaseDriver接口的全路径，包名+接口名，demo里就是com.nil.spi.DataBaseDriver,注意一点也不能错，编码格式需要是UTF-8
    - 编辑文件，里面能够联想出这个接口的实现MsqlDriver类，所以这个文件的内容就是MsqlDriver的包名+MsqlDriver，demo里就是com.nil.spi.MsqlDriver，可以写多个实现，换行即可

3、使用

​      有了规范，有个实现，接下来就可以使用了

​      例如spi-test

   - 首选需要在pom.xml里面引入database-driver和mysql-driver两个依赖

   - ServiceLoader获取实现

     ```java
     import com.nil.spi.DataBaseDriver;
     import java.util.ServiceLoader;
     
     public class Test {
     	public static void main(String[] args) {
     		//可以获取到DataBaseDriver接口的所有实现类
     		ServiceLoader<DataBaseDriver> serviceLoader = ServiceLoader.load(DataBaseDriver.class);
     		//打印出所有的实现类
     		for(DataBaseDriver dataBaseDriver : serviceLoader){
     			System.out.println(dataBaseDriver.connect("test"));
     		}
     	}
     }
     ```

     

# 条件控制

官方的starter里面其实没有META-INF/spring-factories

META-INF/spring-factories而是在spring-boot-autoconfigure的jar包里面

然后官方starter里面用到了@ConditionalOnClass(XX.class)之类的注解进行条件控制，也就是你引入了官方starter后，这些条件注解满足了条件就会进行bean的加载



但是非官方的starter都是需要META-INF/spring-factories的



## 实现条件控制的两种方式：

- @ConditionalOnClass({XXX.class})等条件注解，加在配置类上

- META-INF/spring-autoconfigure-metadata.properties文件

  ```
  com.XX.XX.XXConfiguration.ConditionalOnClass=com.XX.XX.XXClass
  ```

  表示存在XXClass类才会加载ConditionalOnClass这个配置类里面的bean



### 额外说明：maven的optional

说明starter里面使用@ConditionalOnClass({XXX.class})的是要需要引入XXX.class所在的jar，需要注明optional为true，表示包不传递

例如前文spi那个demo里面，在mysql-driver子model里面引入database-driver时如下：

```
<dependency>
    <groupId>org.example</groupId>
    <artifactId>database-driver</artifactId>
    <version>1.0-SNAPSHOT</version>
    <optional>true</optional>
</dependency>
```

那么spi-test里面引入mysql-driver也得不到database-driver依赖，因为没传递下来