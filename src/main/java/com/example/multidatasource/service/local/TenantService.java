package com.example.multidatasource.service.local;

import com.example.multidatasource.config.TenantContext;
import jakarta.transaction.Transactional;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public abstract class TenantService {


    private final EntityManagerFactoryBuilder factoryBuilder;


    public TenantService(EntityManagerFactoryBuilder factoryBuilder) {
        this.factoryBuilder = factoryBuilder;
    }

    private static boolean schemaExists(String tenantId, Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getCatalogs();

            while (resultSet.next()) {
                String existingSchema = resultSet.getString("TABLE_CAT");
                if (existingSchema.equals(tenantId)) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    @Transactional
    public void createTenant(String tenantId) throws SQLException {
        try (Connection connection = routingDataSource().getConnection(); Statement statement = connection.createStatement()) {
            if (!schemaExists(tenantId, connection)) {
                statement.executeUpdate("CREATE SCHEMA `" + tenantId + "`");

                TenantContext.setCurrentTenant(tenantId);
                DataSource dt = dataSource();
                LocalContainerEntityManagerFactoryBean a = entityManagerFactory(factoryBuilder, dt);
                a.afterPropertiesSet();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Lookup
    @Qualifier("getDynamicDataSource")
    public abstract DataSource dataSource();


    @Lookup
    @Qualifier("routingDataSource")
    public abstract DataSource routingDataSource();

    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder factoryBuilder, DataSource dataSource) {
        Map<String, Object> vendorProperties = getVendorProperties();
        return factoryBuilder.dataSource(dataSource).properties(vendorProperties).packages("com.asmtunis.promenu.entities").build();
    }

    protected Map<String, Object> getVendorProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "create");
        properties.put("spring.jpa.hibernate.naming_strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        return new LinkedHashMap<>(determineHibernateProperties(properties, new HibernateSettings().ddlAuto(() -> "create")));
    }

    public Map<String, Object> determineHibernateProperties(Map<String, String> jpaProperties, HibernateSettings settings) {
        Assert.notNull(jpaProperties, "JpaProperties must not be null");
        Assert.notNull(settings, "Settings must not be null");
        return getAdditionalProperties(jpaProperties, settings);
    }

    private Map<String, Object> getAdditionalProperties(Map<String, String> existing, HibernateSettings settings) {
        Map<String, Object> result = new HashMap<>(existing);
        applyScanner(result);
        String ddlAuto = "create";
        if (StringUtils.hasText(ddlAuto)) {
            result.put(AvailableSettings.HBM2DDL_AUTO, ddlAuto);
        } else {
            result.remove(AvailableSettings.HBM2DDL_AUTO);
        }
        Collection<HibernatePropertiesCustomizer> customizers = settings.getHibernatePropertiesCustomizers();
        if (!ObjectUtils.isEmpty(customizers)) {
            customizers.forEach((customizer) -> customizer.customize(result));
        }
        return result;
    }

    private void applyScanner(Map<String, Object> result) {
        if (!result.containsKey(AvailableSettings.SCANNER) && ClassUtils.isPresent("org.hibernate.boot.archive.scan.internal.DisabledScanner", null)) {
            result.put(AvailableSettings.SCANNER, "org.hibernate.boot.archive.scan.internal.DisabledScanner");
        }
    }
}

