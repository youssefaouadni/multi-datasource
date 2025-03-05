package com.example.multidatasource.datasources;

import com.example.multidatasource.config.TenantContext;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataSourceScope implements Scope {
    private final Map<String, BeanContainer> beans = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Runnable> destructionCallbacks
            = Collections.synchronizedMap(new HashMap<>());

    public DataSourceScope(TaskScheduler taskScheduler) {
        taskScheduler.scheduleWithFixedDelay(this::clean, Duration.ofMinutes(1));
    }

    public void clean() {
        long l = System.currentTimeMillis() - Duration.ofMinutes(15).toMillis();
        List<String> toDelete = beans.keySet().stream().filter(a -> beans.get(a).getTimeStamp() < l).toList();
        toDelete.forEach(beans::remove);
    }

    @Override
    @NonNull
    public Object get(@NonNull String name, @NonNull ObjectFactory<?> objectFactory) {
        String tenant = getConversationId();
        if (beans.containsKey(tenant)) {
            BeanContainer c = beans.get(tenant);
            c.setTimeStamp(System.currentTimeMillis());
            return c.getBean();
        }
        Object bean = objectFactory.getObject();

        beans.put(tenant, new BeanContainer(System.currentTimeMillis(), bean));
        return bean;
    }

    @Override
    public Object remove(@NonNull String name) {
        return beans.remove(name);
    }

    @Override
    public void registerDestructionCallback(@NonNull String name, @NonNull Runnable callback) {
        destructionCallbacks.put(name, callback);
    }

    @Override
    public Object resolveContextualObject(@NonNull String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return TenantContext.getCurrentTenant();
    }
}

class BeanContainer {
    private final Object bean;
    private long timeStamp;

    public BeanContainer(long timeStamp, Object bean) {
        this.timeStamp = timeStamp;
        this.bean = bean;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Object getBean() {
        return bean;
    }
}