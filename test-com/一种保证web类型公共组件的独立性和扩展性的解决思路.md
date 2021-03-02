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



 # 插件式扩展

其实大部分扩展方式都是使用插件化扩展。就是主程序中加载插件，扩展方以插件的方式进行扩展

## 一些插件化拓展的方案

1. IoC方式：母体应用声明接口，外部插件实现接口并且通过 @Component @Service 或其他注解让Spring 容器管理， 母体应用通过 @Resource @Autowired 来注入。
2. SPI方式：母体应用声明接口，外部插件实现接口并且配置于 META-INF/services/ 下，母体应用通过 ServiceLoader 加载接口的实现类。
3. AOP方式：外部插件通过 Spring Aspect 技术实现对母体应用的切面拦截。



## slot-maven-plugin

slot：Spring Boot 可插件化拓展改造器，让 Spring-Boot 应用支持加载外部 jar 包，实现插件化拓展。

GitHub: https://github.com/core-lib/slot-maven-plugin

Slot: 在计算机行业指的就是周边元件扩展插槽。

### 问题描述

Spring-Boot 项目打包后是一个FatJar 即把所有依赖的第三方jar也打包进自身的jar中，运行时 classpath 包括 FatJar 中的 BOOT-INF/classes 目录和 BOOT-INF/lib 目录下的所有jar。

那么问题是要想加载外部化 jar 就只能打包期间把 jar 依赖进去，无法实现可插拔式插件化拓展。

[Slot](https://github.com/core-lib/slot-maven-plugin) 就是一个可以将 Spring-Boot 项目升级为可支持加载外部 jar 的 Maven 插件。

### 原理说明

一个 Spring-Boot JAR 启动的流程可以分为以下几步：

1. 通过 java -jar spring-boot-app.jar args... 命令启动
2. JVM 读取该 jar 的 META-INF/MANIFEST.MF 文件中的 Main-Class，在 Spring-Boot JAR 中这个值通常为 org.springframework.boot.loader.JarLauncher
3. JVM 调用该类的 main 方法，传入参数即上述命令中参数
4. JarLauncher 构建 ClassLoader 并反射调用 META-INF/MANIFEST.MF 中的 Start-Class 类的 main 方法，通常为项目中的 Application 类
5. Application 类的 main 方法调用 SpringApplication.run(Application.class, args); 以最终启动应用

[Slot](https://github.com/core-lib/slot-maven-plugin) 的核心原理是：

1. 拓展 org.springframework.boot.loader.JarLauncher 实现根据启动命令参数读取外部 jar 包并且加入至 classpath 中
2. 修改 META-INF/MANIFEST.MF 中的 Main-Class 为拓展的 JarLauncher

### 环境依赖

1. JDK 1.7 +
2. Spring-Boot

### 使用说明

```
<project>
    <!-- 设置 jitpack.io 插件仓库 -->
    <pluginRepositories>
        <pluginRepository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </pluginRepository>
    </pluginRepositories>
    <!-- 添加 Slot Maven 插件 -->
    <build>
        <plugins>
            <plugin>
                <groupId>com.github.core-lib</groupId>
                <artifactId>slot-maven-plugin</artifactId>
                <version>1.0.2</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>transform</goal>
                        </goals>
                        <phase>package</phase>
                        <configuration>
                            <!-- optional
                            <sourceDir/>
                            <sourceJar/>
                            <targetDir/>
                            <targetJar/>
                            -->
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### 参数说明

| 参数名称  | 命令参数名称     | 参数说明        | 参数类型 | 缺省值                          | 示例值   |
| --------- | ---------------- | --------------- | -------- | ------------------------------- | -------- |
| sourceDir | -Dslot.sourceDir | 源jar所在目录   | File     | ${project.build.directory}      | 文件目录 |
| sourceJar | -Dslot.sourceJar | 源jar名称       | String   | ${project.build.finalName}.jar  | 文件名称 |
| targetDir | -Dslot.targetDir | 目标jar存放目录 | File     | ${project.build.directory}      | 文件目录 |
| targetJar | -Dslot.targetJar | 目标jar名称     | String   | ${project.build.finalName}.slot | 文件名称 |

插件的默认执行阶段是 package ， 当然也可以通过使用以下命令来单独执行。

```
mvn slot:transform

mvn slot:transform -Dslot.targetJar=your-spring-boot-app-slot.jar
```

默认情况下，通过 slot 升级后的 jar 名称为 ${project.build.finalName}-slot.jar ，可以通过插件配置或命令参数修改。

### 注意事项

```
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <!-- 需要将executable和embeddedLaunchScript参数删除，目前还不能支持对该模式Jar的升级！
    <configuration>
        <executable>true</executable>
        <embeddedLaunchScript>...</embeddedLaunchScript>
    </configuration>
    -->
</plugin>
```

### 启动应用

Slot 支持使用两个参数来指定要加载的外部 jar 包：

1. --slot.root 即外部 jar 的根路径，缺省情况下为 Spring-Boot JAR 包的目录。
2. --slot.path 即外部 jar 的路径，支持设置多个，支持 ANT 表达式风格。

```
java -jar spring-boot-app-slot.jar --slot.root=/absolute/root/ --slot.path=foo.jar  --slot.path=bar.jar

java -jar spring-boot-app-slot.jar --slot.path=/relative/path/to/plugin.jar

java -jar spring-boot-app-slot.jar --slot.path=/relative/path/to/**.jar
```

ANT 表达式通配符说明

| 通配符 | 含义             | 示例                                                         |
| ------ | ---------------- | ------------------------------------------------------------ |
| **     | 任意个字符及目录 | /plugins/**.jar 即 /plugins 目录及子目录的所有 .jar 后缀的文件 |
| *      | 任意个字符       | /plugins/*.jar 即 /plugins 目录的所有 .jar 后缀的文件        |
| ?      | 单个字符         | ???.jar 即当前目录所有名称为三个任意字符及以 .jar 为后缀的文件 |

通配符可以随意组合使用！ 例如 /plugins/**/plugin-*-v???.jar

### 使用技巧

由于通过 Slot 加载后的外部 jar 实际上和 Spring-Boot JAR 中的 jar 处于同一个 ClassLoader 所以外部插件和母体应用之间是一个平级的关系， 外部插件可以引用母体应用中的 class 同样母体应用也可以引用外部插件的 class。

由于外部插件项目或模块通常也会依赖另外的第三方jar，所以外部插件与母体应用集成运行时也需要把另外的第三方jar通过--slot.path参数加载进来。 推荐使用 maven-dependency-plugin 在打包时将需要用到的第三方jar拷贝到指定目录，最后通过ANT表达式方式一起加载运行。

```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>copy-dependencies</goal>
            </goals>
            <configuration>
                <includeScope>runtime</includeScope>
                <outputDirectory>${project.build.directory}/dependencies</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

或者使用 maven-shade-plugin 插件把相关的第三方jar资源通通打包进一个。

```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.1.1</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                ...
            </configuration>
        </execution>
    </executions>
</plugin>
```

另外需要注意的是，当母体应用和外部插件有相同的第三方依赖时，推荐让外部插件模块以 <scope>provided</scope> 的方式依赖之。

## PF4J

PF4J是一个Java轻量级的插件框架，可以实现动态加载，执行，卸载外部插件(支持`jar`以及`zip`)，具体可以看官网：https://pf4j.org/。

Github地址：https://github.com/pf4j/pf4j

```
<dependency>
  <groupId>org.pf4j</groupId>
  <artifactId>pf4j</artifactId>
  <version>3.0.1</version>
</dependency>
```

### 插件项目工程结构

- plugin-api：定义可扩展接口
- plugins：插件项目，可以包含多个插件，需要实现`plugin-api`中定义的接口
- plugin-app：主程序，需要依赖`plugin-api`，加载并执行`plugins`

#### 定义可扩展接口(plugin-api)

简单定义一个接口，需继承`ExtensionPoint`：

```
package plugin.api;

import org.pf4j.ExtensionPoint;

public interface Greeting extends ExtensionPoint {

    String getGreeting();
}
```

#### 实现插件(plugins)

插件需要实现`plugin-api`定义的接口，并且使用`@Extension`标记：

```
package plugins;

import org.pf4j.Extension;
import plugin.api.Greeting;

@Extension
public class WelcomeGreeting implements Greeting {

    public String getGreeting() {
        return "Welcome";
    }
}
```

#### 插件打包(plugins)

插件打包时，需要往`MANIFEST.MF`写入插件信息，此处使用`maven`插件(打包命令为`package`)：

```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-jar-plugin</artifactId>
  <version>2.3.1</version>
  <configuration>
    <archive>
      <manifestEntries>
        <Plugin-Id>welcome-plugin</Plugin-Id>
        <Plugin-Version>0.0.1</Plugin-Version>
      </manifestEntries>
    </archive>
  </configuration>
</plugin>
```

 

根据Github上介绍，`MANIFEST.MF`中`Plugin-Id`以及`Plugin-Version`是必须信息：

> In above manifest I described a plugin with id `welcome-plugin` (mandatory attribute), with class `org.pf4j.demo.welcome.WelcomePlugin` (optional attribute), with version `0.0.1` (mandatory attribute) and with dependencies to plugins `x, y, z` (optional attribute).

此处定义插件ID为`welcome-plugin`，版本为`0.0.1`

#### 加载执行插件(plugin-app)

```
package plugin.app;

import java.nio.file.Paths;
import java.util.List;

import org.pf4j.JarPluginManager;
import org.pf4j.PluginManager;

import plugin.api.Greeting;

public class Main {

    public static void main(String[] args) {
        // jar插件管理器
        PluginManager pluginManager = new JarPluginManager();

        // 加载指定路径插件
        pluginManager.loadPlugin(Paths.get("plugins-0.0.1-SNAPSHOT.jar"));

        // 启动指定插件(也可以加载所有插件)
        pluginManager.startPlugin("welcome-plugin");

        // 执行插件
        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
        for (Greeting greeting : greetings) {
            System.out.println(">>> " + greeting.getGreeting());
        }

        // 停止并卸载指定插件
        pluginManager.stopPlugin("welcome-plugin");
        pluginManager.unloadPlugin("welcome-plugin");

    }
}
```

运行输出：

```
>>> Welcome
```

### 其他

#### 插件周期

如果对插件生命周期(如加载，执行，停止等)有兴趣的话，可以实现插件类继承`Plugin`：

```
package plugins;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

public class WelcomePlugin extends Plugin {

    public WelcomePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println("WelcomePlugin.start()");
    }

    @Override
    public void stop() {
        System.out.println("WelcomePlugin.stop()");
    }

    @Override
    public void delete() {
        System.out.println("WelcomePlugin.delete()");
    }
}
```

同时往`MANIFEST.MF`写入插件信息：

```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-jar-plugin</artifactId>
  <version>2.3.1</version>
  <configuration>
    <archive>
      <manifestEntries>
        <Plugin-Id>welcome-plugin</Plugin-Id>
        <Plugin-Version>0.0.1</Plugin-Version>
        <!-- 新增 -->
        <Plugin-Class>plugins.WelcomePlugin</Plugin-Class>
      </manifestEntries>
    </archive>
  </configuration>
</plugin>
```

打包后运行输出：

```
WelcomePlugin.start()
>>> Welcome
WelcomePlugin.stop()
```

