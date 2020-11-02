# Springboot相互依赖

- 暴力整体依赖

可以明确的是一个springBoot项目是完全可以依赖另一个springBoot项目的

类似demo中的demo1和demo2，demo2依赖demo1

```xml
<dependency>
  <groupId>com.nil</groupId>
  <artifactId>demo1</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```



启动的时候，demo1可以独立启动，

如果启动demo2，则demo1的启动类和配置无效，demo1会读取demo2中的配置，如果没有配置则报错

- 放在特定目录下，读取加载

这种方式，例如demo1和demo3，demo1是独立的一个springBoot项目，demo3其实也是，没有显示的依赖关系，甚至可以完全不依赖，可认为对于demo1而言，demo3可有可无，如果有就加载，相当于是demo1的一个可有可无的组件。本例中，demo3只要打包成jar放在demo1的extlib目录下，demo1就会读取加载

demo1的打包方式比较特殊，如代码所示，使用了assembly，打包成zip，里面包含一个demo1的jar包，config目录和extlib目录：

config目录放置配置文件，

extlib目录就是用来放置demo3的，将demo3打包成jar，放置在本目录下，demo1会自动读取加载

下面详细讲解此方法的实现步骤



# 读取并加载特定目录下的jar包

这里开始前文中第二种的实现方式

## 主体jar

本例中主体就是demo1

### 读取并加载外部扩展jar

需要代码实现读取扩展jar包，加载里面的类，对于controller里面的接口要用RequestMappingHandlerMapping处理

```java
package com.nil.demo1;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @description: 外部扩展jar插件load组件
 */
@Slf4j
@Component
public class PlugLoadComponent {

    @Autowired
    private AnnotationConfigServletWebServerApplicationContext annotationConfigEmbeddedWebApplicationContext;

    /**
     * 查找扩展版jar
     *
     * @return
     * @throws MalformedURLException
     */
    private static List<URL> findPlugJar(File path) throws MalformedURLException {
        List<URL> list = new ArrayList<>();
        Collection<File> files = FileUtils.listFiles(path, new String[]{"jar"}, false);
        for (File file : files) {
            list.add(file.toURI().toURL());
        }
        log.info("发现jar包数量：{}", list.size());
        return list;
    }

    /**
     * 注册bean
     */
    public void register() {

        try {
            // 判断扩展插件路径是否存在
            //File path = new File(
            //        "/data/test-com/demo1/output/demo1-0.0.1-SNAPSHOT-bin/extlib");
            //相对路径，此路径表示和本jar同目录下的extlib目录下的内容
            File path = new File("./extlib");
            if (!path.exists()) {
                log.error("./extlib不存在");
                return;
            }
            //查找扩展版jar
            List<URL> dependencyJar = findPlugJar(path);
            if (CollectionUtils.isEmpty(dependencyJar)) {
                log.warn("jar包数量为0，不需要加载类");
                return;
            }

            URL[] urls = dependencyJar.toArray(new URL[dependencyJar.size()]);
            //新建classloader 核心
            URLClassLoader urlClassLoader = new URLClassLoader(urls,
                    annotationConfigEmbeddedWebApplicationContext.getClassLoader());

            //获取导入的jar的controller  service  dao 等类，并且创建BeanDefinition
            Set<BeanDefinition> beanDefinitions = getBeanDefinitions(urlClassLoader);

            beanDefinitions.forEach(item -> {
                //根据beanDefinition通过BeanFactory注册bean
                annotationConfigEmbeddedWebApplicationContext.getDefaultListableBeanFactory()
                        .registerBeanDefinition(item.getBeanClassName(), item);
            });

            //修改BeanFactory的ClassLoader
            annotationConfigEmbeddedWebApplicationContext.getDefaultListableBeanFactory()
                    .setBeanClassLoader(urlClassLoader);
            // 注册controller
            registController(urlClassLoader, beanDefinitions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册controller
     *
     * @param urlClassLoader
     * @param beanDefinitions
     */
    private void registController(URLClassLoader urlClassLoader, Set<BeanDefinition> beanDefinitions) {
        //获取requestMappingHandlerMapping，用来注册HandlerMapping
        RequestMappingHandlerMapping requestMappingHandlerMapping = annotationConfigEmbeddedWebApplicationContext
                .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        beanDefinitions.forEach(item -> {

            String classname = item.getBeanClassName();
            try {
                Class<?> c = Class.forName(classname, false, urlClassLoader);
                RestController annotation = c.getAnnotation(RestController.class);
                //获取该bean 真正的创建
                Object proxy = annotationConfigEmbeddedWebApplicationContext.getBean(item.getBeanClassName());
                //如果此bean是Controller，则注册到RequestMappingHandlerMapping里面
                if (annotation != null) {

                    Method getMappingForMethod = ReflectionUtils
                            .findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod", Method.class,
                                    Class.class);
                    getMappingForMethod.setAccessible(true);
                    try {
                        Method[] method_arr = c.getMethods();
                        for (Method method : method_arr) {
                            if (method.getAnnotation(RequestMapping.class) != null) {
                                //创建RequestMappingInfo
                                RequestMappingInfo mapping_info = (RequestMappingInfo) getMappingForMethod
                                        .invoke(requestMappingHandlerMapping, method, c);
                                //注册
                                requestMappingHandlerMapping.registerMapping(mapping_info, proxy, method);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("LoadService.registController err:", e);
                    }
                }
            } catch (ClassNotFoundException e) {
                log.warn("LoadService.registController err:", e);
            }
        });
    }

    /**
     * 获取jar 中的 bean
     *
     * @param classLoader
     * @return
     * @throws Exception
     */
    public Set<BeanDefinition> getBeanDefinitions(ClassLoader classLoader) throws Exception {
        Set<BeanDefinition> candidates = new LinkedHashSet<>();

        // PathMatchingResourcePatternResolver是基于模式匹配的，默认使用AntPathMatcher进行路径匹配，它除了支持ResourceLoader支持的前缀外，还额外支持“classpath*:”用于加载所有匹配的类路径Resource，
        // ResourceLoader不支持前缀“classpath*:”
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(classLoader);
      //获取类路径为com/nil/ext下面的类，即包名需要是com.nil.ext开头
        Resource[] resources = resourcePatternResolver.getResources("classpath*:com/nil/ext/**/*.class");
        log.info("加载到类数量：{}", resources.length);
        MetadataReaderFactory metadata = new SimpleMetadataReaderFactory();
        for (Resource resource : resources) {
            MetadataReader metadataReader = metadata.getMetadataReader(resource);
            ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
            sbd.setResource(resource);
            sbd.setSource(resource);
            candidates.add(sbd);
        }
        for (BeanDefinition beanDefinition : candidates) {
            beanDefinition.setPrimary(true);
            beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
            String className = beanDefinition.getBeanClassName();
            RestController c = Class.forName(className, false, classLoader).getAnnotation(RestController.class);
            Service s = Class.forName(className, false, classLoader).getAnnotation(Service.class);
            Component component = Class.forName(className, false, classLoader).getAnnotation(Component.class);
            if (c != null || s != null || component != null) {
                log.warn("LoadService.getBeanDefinitions class type not support:{}", className);
            }
        }
        return candidates;
    }
}

```



### 启动类注册扩展jar

```java
@SpringBootApplication(scanBasePackages = "com.nil")
public class Demo1Application {

  //此处调用前文类的注册方法，加载扩展jar
    public Demo1Application(PlugLoadComponent plugLoadComponent) {
        plugLoadComponent.register();
    }

    public static void main(String[] args) {
        SpringApplication.run(Demo1Application.class, args);
    }

}
```

以上是demo1中的代码，这样，部署的时候，在demo1jar包的目录创建路径extlib，将扩展jar放在extlib目录下就会被demo1读取并加载



## 扩展jar

扩展jar就是要放在extlib目录下，要被主体jar读取的内容

### 依赖主体jar

扩展jar不能有自己的启动类，要依赖主体jar，设置主体jar的启动类为自己的启动类。打包方式也不能使用spring-boot的打包方式。

```xml
<dependency>
  <groupId>com.nil</groupId>
  <artifactId>demo1</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 设置主体jar的启动类为自己的启动类

本例中就是将demo3的启动类设置为demo1的启动类

![image-20201102222324367](/Users/beccaxi/Library/Application Support/typora-user-images/image-20201102222324367.png)





###  设置打包方式

```xml
<build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>assembly/assembly.xml</exclude>
                    </excludes>
                </configuration>
            </plugin>
          <!-- 下面这种方式是springboot的打包方式，idea中edit configurations设置Main class为com.nil.demo1.Demo1Application，也可以启动起来,但是经测试，这种打包方式打包出来的jar放在extlib目录下无法将类加载成功 -->
            <!--<plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
								<executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                            <goal>build-info</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <mainClass>com.nil.demo1.Demo1Application</mainClass>
                    <executable>true</executable>
                    <fork>true</fork>
                </configuration>
            </plugin>-->
        </plugins>
    </build>
```



## 扩展-自定义打包-maven-assembly-plugin

其实对于demo1可以自定义打包方式，例如打包成zip包，目录如下：

![image-20201101195948733](/Users/beccaxi/Library/Application Support/typora-user-images/image-20201101195948733.png)

zip包里面包含了demo1的jar包，然后包含一个config目录和一个extlib目录，config目录里面保存配置文件，extlib目录下保存扩展包

### 在pom中引入插件依赖



```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-jar-plugin</artifactId>
  <configuration>
    <excludes>
      <exclude>assembly/assembly.xml</exclude>
    </excludes>
  </configuration>
</plugin>
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-assembly-plugin</artifactId>
  <configuration>
    <finalName>${project.name}-${project.version}</finalName>
    <descriptors>
      <!--描述文件路径-->
      <descriptor>src/main/resources/assembly/assembly.xml</descriptor>
    </descriptors>
    <outputDirectory>output</outputDirectory>
  </configuration>
  <executions>
    <execution>
      <id>make-assembly</id>
      <phase>package</phase>
      <goals>
        <goal>single</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```



### assembly.xml

本例中assembly.xml在src/main/resources/assembly/目录下

```xml
<?xml version='1.0' encoding='UTF-8'?>
<assembly xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://maven.apache.org/ASSEMBLY/2.1.0"
        xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.1.0 http://maven.apache.org/xsd/assembly-2.1.0.xsd">
    <id>bin</id>
    <formats>
        <format>zip</format>
    </formats>
    <includeBaseDirectory>false</includeBaseDirectory>
    <fileSets>
        <!-- 把项目的配置文件，打包进zip包的config目录下    -->
        <fileSet>
            <!-- 配置项目中需要被打包的文件的存储路径 -->
            <directory>${project.basedir}/src/main/resources</directory>
            <outputDirectory>/config</outputDirectory>
            <includes>
                <include>*.properties</include>
                <include>*.yaml</include>
                <include>*.xml</include>
            </includes>
        </fileSet>
        <fileSet>
            <directory>${project.build.directory}/</directory>
            <outputDirectory>/</outputDirectory>
            <includes>
                <include>*.jar</include>
            </includes>
        </fileSet>
        <fileSet>
            <directory>${project.build.directory}/</directory>
            <outputDirectory>/extlib</outputDirectory>
            <excludes>
                <exclude>**/*</exclude>
            </excludes>
        </fileSet>
    </fileSets>
</assembly>
```





# maven的几种打包方式



## maven-compiler-plugin

编译Java源码，一般只需设置编译的jdk版本

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.6.0</version>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
    </configuration>
</plugin>
```



 或者在properties设置jdk版本

```xml
<properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
 </properties>
```

 



## maven-dependency-plugin

用于复制依赖的jar包到指定的文件夹里

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>2.10</version>
    <executions>
        <execution>
            <id>copy-dependencies</id>
            <phase>package</phase>
            <goals>
                <goal>copy-dependencies</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}/lib</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```



## maven-jar-plugin

打成jar时，设定manifest的参数，比如指定运行的Main class，还有依赖的jar包，加入classpath中

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>2.4</version>
    <configuration>
        <archive>
            <manifest>
                <addClasspath>true</addClasspath>
                <classpathPrefix>/data/lib</classpathPrefix>
                <mainClass>com.zhang.spring.App</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```





##  maven-antrun-plugin

在maven中运行Ant任务，比如在打包阶段，对文件进行复制

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-antrun-plugin</artifactId>
    <version>1.7</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>run</goal>
            </goals>
            <configuration>
                <target name="copy">
                    <delete>
                        <fileset dir="target" includes="*.properties"></fileset>
                    </delete>
                    <copy todir="target">
                        <fileset dir="files"></fileset>
                    </copy>
                </target>
            </configuration>
        </execution>
    </executions>
</plugin>
```





## wagon-maven-plugin

用于一键部署，把本地打包的jar文件，上传到远程服务器上，并执行服务器上的shell命令

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>wagon-maven-plugin</artifactId>
    <version>1.0</version>
    <configuration>
        <serverId>crawler</serverId>
        <fromDir>target</fromDir>
        <includes>*.jar,*.properties,*.sh</includes>
        <url>sftp://59.110.162.178/home/zhangxianhe</url>
        <commands>
            <command>chmod 755 /home/zhangxianhe/update.sh</command>
            <command>/home/zhangxianhe/update.sh</command>
        </commands>
        <displayCommandOutputs>true</displayCommandOutputs>
    </configuration>
</plugin>
```



## tomcat7-maven-plugin

用于远程部署Java Web项目

```xml
<plugin>
    <groupId>org.apache.tomcat.maven</groupId>
    <artifactId>tomcat7-maven-plugin</artifactId>
    <version>2.2</version>
    <configuration>
        <url>http://59.110.162.178:8080/manager/text</url>
        <username>XXXXX</username>
        <password>XXXXXX</password>
    </configuration>
</plugin>
```



##  maven-shade-plugin

用于把多个jar包，打成1个jar包

一般Java项目都会依赖其他第三方jar包，最终打包时，希望把其他jar包包含在一个jar包里

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>2.4.3</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <transformers>
                    <transformer
                        implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <manifestEntries>
                            <Main-Class>com.meiyou.topword.App</Main-Class>
                            <X-Compile-Source-JDK>${maven.compile.source}</X-Compile-Source-JDK>
                            <X-Compile-Target-JDK>${maven.compile.target}</X-Compile-Target-JDK>
                        </manifestEntries>
                    </transformer>
                </transformers>
            </configuration>
        </execution>
    </executions>
</plugin>
```



 