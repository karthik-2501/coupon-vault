package com.couponvault.config;

import com.couponvault.domain.*;
import com.couponvault.repository.CouponRepository;
import com.couponvault.repository.StoreRepository;
import com.couponvault.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class SeedConfig {
    @Bean
    @Profile("!test")
    CommandLineRunner seed(UserRepository userRepository, StoreRepository storeRepository, CouponRepository couponRepository) {
        return args -> {
            if (userRepository.count() > 0) return;
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            User seller = new User();
            seller.setName("Alice Seller");
            seller.setEmail("alice@example.com");
            seller.setPasswordHash(encoder.encode("password123"));
            userRepository.save(seller);

            User buyer = new User();
            buyer.setName("Bob Buyer");
            buyer.setEmail("bob@example.com");
            buyer.setPasswordHash(encoder.encode("password123"));
            userRepository.save(buyer);

            Store store = new Store();
            store.setName("MegaStore");
            store.setWebsiteUrl("https://example-store.com");
            storeRepository.save(store);

            Coupon c1 = new Coupon();
            c1.setSeller(seller);
            c1.setStore(store);
            c1.setCode("VALID-MEGA20");
            c1.setDescription("20% off orders above 50");
            c1.setDiscountType(DiscountType.PERCENTAGE);
            c1.setDiscountValue(new BigDecimal("20"));
            c1.setMinOrderValue(new BigDecimal("50"));
            c1.setExpiryDate(LocalDate.now().plusDays(20));
            c1.setPrice(new BigDecimal("8.99"));
            c1.setStatus(CouponStatus.VALID);
            couponRepository.save(c1);

            Coupon c2 = new Coupon();
            c2.setSeller(seller);
            c2.setStore(store);
            c2.setCode("BAD-CODE");
            c2.setDescription("Invalid sample code");
            c2.setDiscountType(DiscountType.FIXED);
            c2.setDiscountValue(new BigDecimal("10"));
            c2.setExpiryDate(LocalDate.now().plusDays(5));
            c2.setPrice(new BigDecimal("2.00"));
            c2.setStatus(CouponStatus.INVALID);
            couponRepository.save(c2);
        };
    }
}
