package com.example.multidatasource.datasources;

import com.example.multidatasource.config.TenantContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.NonNull;

import javax.sql.DataSource;

public class ClientDataSourceRouter
        extends AbstractRoutingDataSource implements ApplicationContextAware {
    private final DataSource defaultDatasource;

    private ApplicationContext applicationContext;

    public ClientDataSourceRouter(DataSource defaultDatasource) {
        this.defaultDatasource = defaultDatasource;
    }

    @Override
    public void initialize() {
    }

    @Override
    @NonNull
    protected DataSource determineTargetDataSource() {
        Object lookupKey = determineCurrentLookupKey();
        if (checkTenant(lookupKey)) return defaultDatasource;
        try {
            return applicationContext.getBean("getDynamicDataSource", DataSource.class);
        } catch (NoSuchBeanDefinitionException e) {
            return defaultDatasource;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean checkTenant(Object lookupKey) {
        return lookupKey == null;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}