package com.example.emos.wx.config.shiro;


import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.emos.wx.db.expection.EmosExpection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtUtil {
    //秘钥
    @Value("${emos.jwt.secret}")
    private String secret;

    //过期时间（天）
    @Value("${emos.jwt.expire}")
    private int expire;

    //创建令牌
    public String CreateToken(int userId){
        //设置过期时间，利用偏移量
        Date date = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, expire);
        //创建加密算法
        Algorithm algorithm = Algorithm.HMAC256(secret);
        //创建内部类，通过设置id，获取时间和密钥生成临牌
        JWTCreator.Builder builder= JWT.create();
        String token = builder.withClaim("userId", userId).withExpiresAt(date).sign(algorithm);
        return token;
    }

    //通过临牌过去userId
    public int getUserId(String token){
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userId").asInt();
        } catch (Exception e) {
            throw new EmosExpection("令牌无效");
        }
    }

    //判断令牌是否有效
    public void verifierToken(String token){
        //创建加密算法对象
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        //进行验证，如果验证失败会自动报错
        verifier.verify(token);
    }
}
