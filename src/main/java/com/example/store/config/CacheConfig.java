package com.example.store.config;

import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class CacheConfig {

    public static final String CUSTOMERS_CACHE = "customers";
    public static final String CUSTOMER_SEARCH_CACHE = "customer-search";
    public static final String ORDERS_CACHE = "orders";
    public static final String PRODUCTS_CACHE = "products";

    @Value("${store.cache.expiration.minutes:30}")
    private long cacheExpirationMinutes;

    @Value("${store.cache.eviction.interval:1800000}")
    private long cacheEvictionInterval;

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager() {
            @Override
            @NonNull
            protected Cache createConcurrentMapCache(@NonNull String name) {
                log.debug("Creating cache '{}' with {}min expiration", name, cacheExpirationMinutes);

                return new ConcurrentMapCache(name, CacheBuilder.newBuilder()
                        .expireAfterWrite(cacheExpirationMinutes, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .recordStats()
                        .build().asMap(), true);
            }
        };
    }


    @CacheEvict(value = {CUSTOMERS_CACHE, CUSTOMER_SEARCH_CACHE, ORDERS_CACHE, PRODUCTS_CACHE}, allEntries = true)
    @Scheduled(fixedRateString = "${store.cache.eviction.interval:1800000}")
    public void evictAllCaches() {
        log.info("Scheduled cache eviction completed - cleared all store caches (interval: {} ms)", cacheEvictionInterval);
    }

    @CacheEvict(value = CUSTOMER_SEARCH_CACHE, allEntries = true)
    @Scheduled(fixedRateString = "${store.cache.search.eviction.interval:600000}")
    public void evictSearchCache() {
        log.debug("Evicting customer search cache to ensure fresh search results");
    }

}
