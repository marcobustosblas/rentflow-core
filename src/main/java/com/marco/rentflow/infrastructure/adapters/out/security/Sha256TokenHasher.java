package com.marco.rentflow.infrastructure.adapters.out.security;

import com.marco.rentflow.core.domain.common.ports.out.TokenHasher;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Component
public class Sha256TokenHasher implements TokenHasher {

    @Override
    public String hash(String rawToken) {
        Objects.requireNonNull(rawToken, "rawToken cannot be null");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on every JVM.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    @Override
    public boolean matches(String rawToken, String hash) {
        Objects.requireNonNull(rawToken, "rawToken cannot be null");
        Objects.requireNonNull(hash, "hash cannot be null");
        // SHA-256 produces fixed-length output, so a simple equals is fine here.
        // If we migrate to a variable-length algorithm, use MessageDigest.isEqual.
        return hash(rawToken).equals(hash);
    }
}