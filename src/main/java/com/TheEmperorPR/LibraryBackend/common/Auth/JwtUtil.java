package com.TheEmperorPR.LibraryBackend.common.Auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:secret123456}")
    private String secretKey;

    @Value("${jwt.expiration-hours:10}")
    private long expirationHours;

    public String generateToken(CustomUserDetails user){
        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("id",user.getId())
                .withClaim("role",user.getRole().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationHours * 60 * 60 * 1000))
                .sign(Algorithm.HMAC256(secretKey));
    }

    public DecodedJWT extractDecodedJwt(String token) {
        return JWT.require(Algorithm.HMAC256(secretKey))
                .build()
                .verify(token);
    }

    public boolean isTokenValid(String token) {
        try {
            DecodedJWT jwt = extractDecodedJwt(token);
            return jwt.getExpiresAt().after(new Date());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return extractDecodedJwt(token).getSubject();
    }

    public Long getUserId(String token) {
        return extractDecodedJwt(token).getClaim("id").asLong();
    }

    public String getRole(String token) {
        return extractDecodedJwt(token).getClaim("role").asString();
    }
}
