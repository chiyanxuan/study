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
