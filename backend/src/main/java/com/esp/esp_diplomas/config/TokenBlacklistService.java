package com.esp.esp_diplomas.config;

import lombok.Getter;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Service
public class TokenBlacklistService {
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void addToBlacklist(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}