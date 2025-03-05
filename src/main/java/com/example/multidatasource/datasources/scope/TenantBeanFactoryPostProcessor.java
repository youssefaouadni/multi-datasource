package com.example.multidatasource.datasources.scope;

import com.example.multidatasource.datasources.DataSourceScope;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;


@Component
public class TenantBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
        ThreadPoolTaskScheduler aa = new ThreadPoolTaskScheduler();
        aa.initialize();
        factory.registerScope("tenant", new DataSourceScope(aa));
    }
}