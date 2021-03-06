package com.fenxiang.hbase.phoenix.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;


@Deprecated
//@Configuration
//@MapperScan(basePackages = "com.fenxiang.hbase.phoenix.dao.phoenix.**", sqlSessionFactoryRef = "PhoenixSqlSessionFactory")
public class PhoenixDataSourceConfig {

    @Bean(name = "PhoenixDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.test1")
    public DataSource phoenixDataSource() throws IOException {
        ResourceLoader loader = new DefaultResourceLoader();
        InputStream inputStream = loader.getResource("classpath:application.properties")
                .getInputStream();
        Properties properties = new Properties();
        properties.load(inputStream);
        Set<Object> keys = properties.keySet();
        Properties dsProperties = new Properties();

        for (Object key : keys) {
            if (key.toString().startsWith("datasource")) {
                dsProperties.put(key.toString().replace("datasource.", ""), properties.get(key));
            }
        }
        HikariDataSourceFactory factory = new HikariDataSourceFactory();
        factory.setProperties(dsProperties);
        inputStream.close();
        return factory.getDataSource();
    }

    @Bean(name = "PhoenixSqlSessionFactory")
    public SqlSessionFactory phoenixSqlSessionFactory(
            @Qualifier("PhoenixDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        ResourceLoader loader = new DefaultResourceLoader();
        String resource = "classpath:mybatis-config.xml";
        factoryBean.setConfigLocation(loader.getResource(resource));
        factoryBean.setSqlSessionFactoryBuilder(new SqlSessionFactoryBuilder());
        factoryBean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/mybatis-phoenix-config.xml"));
//        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("mybatis/classpath:xx/xxx/*.xml"));
        return factoryBean.getObject();
    }

}