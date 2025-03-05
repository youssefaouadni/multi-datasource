package com.example.multidatasource.service.local;

import com.example.multidatasource.baseentity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, String> {
    Optional<Tenant> findByUserNameAndPassword(String userName, String password);

    Optional<Tenant> findByTenantId(String tenantId);

    boolean existsByTenantId(String id);

    boolean existsByUserName(String username);

    boolean existsByPassword(String password);
}