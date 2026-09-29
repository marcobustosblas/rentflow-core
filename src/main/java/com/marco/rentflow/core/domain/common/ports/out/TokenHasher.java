package com.marco.rentflow.core.domain.common.ports.out;

public interface TokenHasher {
    /**
     * Hashes a raw token.
     *
     * @param rawToken the plain-text token; must not be null
     * @return the hashed representation (hex-encoded)
     */
    String hash(String rawToken);

    /**
     * Verifies whether a raw token matches a stored hash.
     *
     * @param rawToken the plain-text token to check
     * @param hash     the stored hash to compare against
     * @return true if the raw token hashes to the given hash
     */
    boolean matches(String rawToken, String hash);
}
