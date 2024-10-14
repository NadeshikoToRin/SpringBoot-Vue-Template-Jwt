package dev.akemi.backend.utils;

// 引入JWT相关的类和异常处理
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

// 引入Spring框架的相关类
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * JwtUtils 类提供了生成、解析和验证JWT（JSON Web Token）的方法
 * 主要用于用户身份验证和授权管理
 */
@Component
public class JwtUtils {

    // 从配置文件中读取JWT密钥
    @Value("${spring.security.jwt.key}")
    String key;

    // 从配置文件中读取JWT的过期时间（以天为单位）
    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;

    //
    public boolean invalidateJwt(String headerToken) {
        String token = this.convertToken(headerToken); // 转换token格式
        if (token == null) { // 如果token格式不正确，返回null
            return false;
        }
        Algorithm algorithm = Algorithm.HMAC256(key); // 使用HMAC256算法
        JWTVerifier jwtVerifier = JWT.require(algorithm).build(); // 构建JWT验证器
        try {
            DecodedJWT jwt = jwtVerifier.verify(token);
            String id = jwt.getId();
            return deleteToken(id,jwt.getExpiresAt());

        } catch (JWTVerificationException e) {
            return false;
        }

    }

    private boolean deleteToken(String uuid, Date expiresAt) {
       if (this.isInvalidToken(uuid)){
           return false;
       }
       Date now = new Date();
       long expire = Math.max(expiresAt.getTime() - now.getTime(),0);
       template.opsForValue().set(Const.JWT_BLACK_LIST + uuid,"",expire, TimeUnit.MILLISECONDS);
       return true;
    }

    public boolean isInvalidToken(String uuid){
         return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }

    /**
     * 创建JWT token
     *
     * @param userDetails 用户详细信息，包括权限等
     * @param id 用户的唯一ID
     * @param username 用户名
     * @return 生成的JWT token字符串
     */
    public String createJwt(UserDetails userDetails, int id, String username) {
        // 使用HMAC256算法创建算法实例，传入密钥
        Algorithm algorithm = Algorithm.HMAC256(key);

        // 获取token的过期时间
        Date expire = this.expireTime();

        // 创建JWT并设置各项声明
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())// 设置JWT ID
                .withClaim("id", id) // 存储用户ID
                .withClaim("name", username) // 存储用户名
                .withClaim("authorities", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority) // 提取用户权限
                        .toList()) // 将权限转换为列表
                .withExpiresAt(expire) // 设置过期时间
                .withIssuedAt(new Date()) // 设置签发时间为当前时间
                .sign(algorithm); // 签名生成token
    }

    /**
     * 计算JWT的过期时间
     *
     * @return 计算后的过期时间（Date对象）
     */
    public Date expireTime() {
        Calendar calendar = Calendar.getInstance(); // 创建日历实例
        // 在当前时间上加上配置的过期时间（以小时为单位，乘以24）
        calendar.add(Calendar.HOUR, expire * 24);
        return calendar.getTime(); // 返回计算后的时间
    }

    /**
     * 解析和验证JWT token
     *
     * @param headerToken 包含JWT的Authorization头部
     * @return 如果验证成功，返回解析后的DecodedJWT对象；否则返回null
     */
    public DecodedJWT resolveJwt(String headerToken) {
        String token = this.convertToken(headerToken); // 转换token格式
        if (token == null) { // 如果token格式不正确，返回null
            return null;
        }
        Algorithm algorithm = Algorithm.HMAC256(key); // 使用HMAC256算法
        JWTVerifier jwtVerifier = JWT.require(algorithm).build(); // 构建JWT验证器
        try {
            DecodedJWT verify = jwtVerifier.verify(token); // 验证token
            if(this.isInvalidToken(verify.getId())){
                return null;
            }
            Date expiresAt = verify.getExpiresAt(); // 获取过期时间
            // 如果当前时间已过期，返回null
            return new Date().after(expiresAt) ? null : verify;
        } catch (JWTVerificationException e) { // 捕获验证异常
            return null; // 验证失败，返回null
        }
    }

    /**
     * 将Authorization头部的token转换为有效的JWT token字符串
     *
     * @param hearToken 包含JWT的Authorization头部
     * @return 转换后的token字符串；如果格式不正确则返回null
     */
    private String convertToken(String hearToken) {
        // 检查token是否为空且以"Bearer "开头
        if (hearToken == null || !hearToken.startsWith("Bearer ")) {
            return null; // 如果不符合格式，返回null
        }
        // 返回去掉"Bearer "前缀的token字符串
        return hearToken.substring(7);
    }

    /**
     * 将解析后的DecodedJWT转换为UserDetails对象
     *
     * @param jwt 解析后的JWT对象
     * @return UserDetails对象，包含用户名和权限
     */
    public UserDetails toUserDetails(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims(); // 获取JWT中的所有声明
        // 构建并返回UserDetails对象
        return User
                .withUsername(claims.get("name").asString()) // 获取用户名
                .password("******") // 密码不返回，保留为******，实际应用中不应返回密码
                .authorities(claims.get("authorities").asArray(String.class)) // 获取用户权限
                .build();
    }

    /**
     * 从DecodedJWT中提取用户ID
     *
     * @param jwt 解析后的JWT对象
     * @return 用户ID
     */
    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims(); // 获取JWT中的所有声明
        return claims.get("id").asInt(); // 返回用户ID
    }
}
