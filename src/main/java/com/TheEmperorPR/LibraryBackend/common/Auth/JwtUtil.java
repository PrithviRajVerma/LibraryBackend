package com.TheEmperorPR.LibraryBackend.common.Auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "secret123456";

    public String generateToken(String username, String role){
        return JWT.create()
                .withSubject(username)
                .withClaim("role",role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10 *60*60*1000))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    public DecodedJWT extractDecodedJwt(String token){
        return JWT.require(Algorithm.HMAC256(SECRET_KEY)).build().verify(token);
    }

    public String extractUsername(String token){
        return extractDecodedJwt(token).getSubject();
    }

    public String extractRole(String token){
        return extractDecodedJwt(token).getClaim("role").asString();
    }

    public boolean validateToken(String token, String username){
        return extractUsername(token).equals(username) && !isExpired(token);
    }

    public boolean isExpired(String token){
        return extractDecodedJwt(token).getExpiresAt().before(new Date());
    }
}
