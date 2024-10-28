package dev.akemi.backend.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * FlowUtils类用于限制某个操作的执行频率，例如验证码请求频率的限制。
 */
@Component
public class FlowUtils {

    @Resource
    StringRedisTemplate template; // 用于操作Redis数据的模板

    /**
     * 检查给定的键是否在规定时间内已存在，以限制操作频率。
     *
     * @param key 用于标识请求的键，一般与IP地址或用户相关。
     * @param blockTime 阻止时间（秒），在此时间内同一键无法重复操作。
     * @return 如果键存在且在阻止时间内，则返回false表示被限制；否则返回true。
     */
    public boolean limitOnceCheck(String key, int blockTime) {
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            return false; // 如果键存在，返回false表示操作被限制
        } else {
            template.opsForValue().set(key, "", blockTime, TimeUnit.SECONDS); // 设置键并指定有效期
            return true; // 键不存在，返回true表示允许操作
        }
    }
}
