package com.atguigu.lease.common.utils;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static  SecretKey secretKey = Keys.hmacShaKeyFor("NRMtXDxwsTpbJQBnvY3UpMx5Uhvd5Wz4".getBytes());
    public static String creatToken(Long userId, String username) {

        String jwt = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .setSubject("LOGIN_USER")
                .claim("userId", userId)
                .claim("username", username)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }

    public static Claims parseToken(String  token){
        if (token == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }

      try {
          JwtParser jwtParser = Jwts.parserBuilder()
                  .setSigningKey(secretKey)
                  .build();
          Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
          return claimsJws.getBody();
      }catch (ExpiredJwtException e) {
          throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
      }catch (JwtException e) {
          throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
      }

    }

    public static void main(String[] args) {
        String token = creatToken(8L, "19155102403");
        System.out.println(token);
    }

}
