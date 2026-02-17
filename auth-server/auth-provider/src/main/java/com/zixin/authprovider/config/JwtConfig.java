package com.zixin.authprovider.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration
public class JwtConfig {

    @Value("${jwt.rsa.public-key}")
    private String publicKeyPem;

    @Value("${jwt.rsa.private-key}")
    private String privateKeyPem;

    @Bean
    public RSAPublicKey rsaPublicKey() {
        try {
            String key = publicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA public key", e);
        }
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey() {
        try {
            String key = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA private key", e);
        }
    }

    /**
     * JWT Encoder（签名）
     */
    @Bean
    public JwtEncoder jwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        JWK jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();

        JWKSource<SecurityContext> jwkSource =
                new ImmutableJWKSet<>(new JWKSet(jwk));

        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * JWT Decoder（验签）
     */
    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
