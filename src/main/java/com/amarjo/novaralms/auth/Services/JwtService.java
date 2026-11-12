package com.amarjo.novaralms.auth.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    @Value("${app.jwt.secret:jo782005am2005yassmoham26111977yassyassmohammoham}")
    private String SECRET_STRING;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    }

        public Map<String, String> generateTokens(String name, String email, String role) {
        String tokenId = UUID.randomUUID().toString();
        String accessToken = Jwts.builder()
                .setId(tokenId)
                .setSubject(email)
                .claim("name", name)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 15))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        String refreshToken = UUID.randomUUID().toString();

        refreshTokenService.saveRefreshToken(refreshToken, email);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }
    public void blacklistToken(String token) {
        try {
            Claims claims = parseToken(token);
            String tokenId = claims.getId();
            Date expiration = claims.getExpiration();
            long diff = expiration.getTime() - System.currentTimeMillis();
            if (diff > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + tokenId,
                        "revoked",
                        diff,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {

        }
    }
    public boolean isTokenBlacklisted(String token) {
        try {
            Claims claims = parseToken(token);
            String tokenId = claims.getId();
            if (tokenId == null) return true;
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + tokenId));
        } catch (Exception e) {
            return true;
        }
    }
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        try {
            Claims claims = parseToken(token);
            boolean isSubjectMatch = claims.getSubject().equals(username);
            boolean isNotExpired = claims.getExpiration().after(new Date());
            boolean isNotBlacklisted = !isTokenBlacklisted(token);

            return isSubjectMatch && isNotExpired && isNotBlacklisted;
        } catch (Exception e) {
            return false;
        }
    }
}