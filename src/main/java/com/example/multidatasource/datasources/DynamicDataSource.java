package com.example.multidatasource.datasources;

import com.example.multidatasource.config.TenantContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
public class DynamicDataSource {

    final Environment env;

    public DynamicDataSource(Environment env) {
        this.env = env;
    }


    @Bean
    @Scope(scopeName = "tenant")
    @Qualifier("dynamicDatabase")
    public DataSource getDynamicDataSource() {
        String tenant = TenantContext.getCurrentTenant();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(env.getProperty("spring.datasource.driver-class-name")));
        dataSource.setUrl("jdbc:mysql://localhost:9999/" + tenant);

        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));

        return dataSource;
    }


    public DataSource defaultDataSource(String defaultDatabaseUrl) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(env.getProperty("spring.datasource.driver-class-name")));
        dataSource.setUrl(defaultDatabaseUrl);
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));

        return dataSource;
    }

    @Primary
    @Bean
    public DataSource routingDataSource(@Value("${spring.datasource.url}") String defaultDatabaseUrl) {
        return new ClientDataSourceRouter(defaultDataSource(defaultDatabaseUrl));
    }


    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.multidatasource.baseentity")
                .build();
    }
}
