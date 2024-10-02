package dev.akemi.backend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class JwtUtils {

    // 从配置文件中读取JWT密钥
    @Value("${spring.security.jwt.key}")
    String key;

    // 从配置文件中读取JWT过期时间（以天为单位）
    @Value("${spring.security.jwt.expire}")
    int expire;

    /**
     * 创建JWT token
     * @param userDetails 用户详细信息
     * @param id 用户ID
     * @param username 用户名
     * @return 生成的JWT token
     */
    public String createJwt(UserDetails userDetails, int id, String username) {
        // 使用HMAC256算法创建算法实例，传入密钥
        Algorithm algorithm = Algorithm.HMAC256(key);

        // 获取token的过期时间
        Date expire = this.expireTime();

        // 创建JWT并设置各项声明
        return JWT.create()
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
     * @return 计算后的过期时间
     */
    public Date expireTime() {
        Calendar calendar = Calendar.getInstance(); // 创建日历实例
        // 在当前时间上加上配置的过期时间（以天为单位）
        calendar.add(Calendar.HOUR, expire * 24);
        return calendar.getTime(); // 返回计算后的时间
    }
}
