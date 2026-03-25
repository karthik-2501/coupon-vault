package com.couponvault.repository;

import com.couponvault.domain.Store;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, String> {
    Optional<Store> findByNameIgnoreCase(String name);
}
