package org.example.ai_api.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    @Autowired
    private CacheManager cacheManager;

    public void refreshSystemFileListCache() {
        evictCacheByName("allFiles");
        evictCacheByName("filesByTarget");
    }

    private void evictCacheByName(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            logger.info("手动刷新缓存: {}", cacheName);
        }
    }
}
