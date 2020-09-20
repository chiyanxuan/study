package com.nil.pagehelp1.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = DataSourceNames.MAPPER1,sqlSessionFactoryRef = "master2SqlSessionFactory")
public class AppDataSource implements TransactionManagementConfigurer {
	private static Logger logger = LoggerFactory.getLogger(AppDataSource.class);

	 
	@Bean
	@ConfigurationProperties(prefix = "spring.datasource.hikari")
	public HikariConfig getHikariConfig() {
		return new HikariConfig();
	}

	@Bean
	@ConfigurationProperties(prefix = "mybatis.configuration")
	public org.apache.ibatis.session.Configuration mybatisConfiguration() {
		return new org.apache.ibatis.session.Configuration();
	}
	
	@Bean(name = "dataSource")
	@Qualifier("dataSource")
	@Primary
	public DataSource firstDataSource() {
		logger.info("第一个数据库连接池创建中......");
		return new HikariDataSource(getHikariConfig());
	}
	
	@Bean(name = "master2SqlSessionFactory")
    public SqlSessionFactory clusterSqlSessionFactory(@Qualifier("dataSource") DataSource dataSource
	,org.apache.ibatis.session.Configuration mybatisConfiguration) throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(DataSourceNames.MAPPER1_PATH));
        sessionFactory.setConfiguration(mybatisConfiguration);
        return sessionFactory.getObject();
	}
	
	@Bean
	public SqlSessionTemplate masterSqlSessionTemplate(@Qualifier("master2SqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
		SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionFactory);
		return template;
	}

	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return new DataSourceTransactionManager(firstDataSource());
	}
}
