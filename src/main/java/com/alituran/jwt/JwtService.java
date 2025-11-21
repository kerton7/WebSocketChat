package com.alituran.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtService {

    @Value("${security.jwt.secret_key}")
    private String SECRET_KEY;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder().setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*60*2))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .claim("role",userDetails.getAuthorities().iterator().next().getAuthority())
                .compact();
    }

    public Key getKey(){
        byte[] decode = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(decode);
    }

    public <T> T exportToken(String token, Function<Claims,T> function){
        Claims body = Jwts.parserBuilder().setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody();
        return function.apply(body);
    }

    public String getUsernameFromToken(String token) {
        return exportToken(token, Claims::getSubject);
    }

    public boolean validateToken(String token) {
        Date date = exportToken(token, Claims::getExpiration);
        return new Date().before(date);
    }

    public String getRoleFromToken(String token) {
        return exportToken(token, claims->claims.get("role").toString());
    }


}
