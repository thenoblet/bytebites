package gtp.bytebites.auth.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Configuration class for JWT (JSON Web Token) settings.
 *
 * <p>This class binds to properties prefixed with "app.jwt" in the application configuration,
 * allowing for externalized configuration of JWT parameters.</p>
 *
 * <p>Properties configured:</p>
 * <ul>
 *   <li><b>secret</b>: The base secret key used for JWT signing and verification</li>
 *   <li><b>expirationMs</b>: Expiration time for regular access tokens in milliseconds</li>
 *   <li><b>refreshExpirationMs</b>: Expiration time for refresh tokens in milliseconds</li>
 * </ul>
 *
 * @see ConfigurationProperties
 * @see Configuration
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
    private String secret;
    private long expirationMs;
    private long refreshExpirationMs;

    /**
     * Gets the JWT secret key as a byte array in Base64 encoded format.
     * This is the format typically required by JWT signing algorithms.
     *
     * @return byte array representation of the Base64 encoded secret key
     */
    public byte[] getSecretBytes() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    public String getSecret() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 characters long. " +
                            "Current length: " + (secret != null ? secret.length() : "null") + secret);
        }
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public void setRefreshExpirationMs(long refreshExpirationMs) {
        this.refreshExpirationMs = refreshExpirationMs;
    }
}
