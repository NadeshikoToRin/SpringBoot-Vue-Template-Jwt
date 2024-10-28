package dev.akemi.backend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.akemi.backend.entity.dto.Account;
import dev.akemi.backend.entity.vo.request.EmailRegisterVO;
import dev.akemi.backend.mapper.AccountMapper;
import dev.akemi.backend.service.AccountService;
import dev.akemi.backend.utils.Const;
import dev.akemi.backend.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * AccountServiceImpl类实现了AccountService接口，用于管理账户的相关逻辑，
 * 包括用户登录、注册、验证码发送和账户信息验证等操作。
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    AmqpTemplate amqpTemplate; // 用于消息队列发送验证码

    @Resource
    StringRedisTemplate stringRedisTemplate; // 用于存储和读取Redis中的数据

    @Resource
    FlowUtils flowUtils; // 工具类，用于限制请求频率

    @Resource
    PasswordEncoder encoder; // 用于加密用户密码

    /**
     * 根据用户名或邮箱加载用户详情，用于Spring Security的认证过程。
     *
     * @param username 用户名或邮箱
     * @return 用户详细信息
     * @throws UsernameNotFoundException 当用户不存在时抛出异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username); // 查找用户
        if (account == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build(); // 构建并返回UserDetails对象
    }

    /**
     * 根据用户名或邮箱查找账户信息。
     *
     * @param text 用户名或邮箱
     * @return Account对象，包含账户信息
     */
    @Override
    public Account findAccountByNameOrEmail(String text) {
        return this.query()
                .eq("username", text).or()
                .eq("email", text)
                .one(); // 根据用户名或邮箱查询单一账户
    }

    /**
     * 生成并发送邮箱验证码，同时检查请求频率。
     *
     * @param type 验证码类型
     * @param email 目标邮箱
     * @param ip 请求的IP地址，用于频率限制
     * @return 若操作频繁则返回提示信息，否则返回null
     */
    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) { // 防止多线程下的频率控制问题
            if (!this.verifyLimit(ip)) {
                return "操作频繁，请稍后再试";
            }
            Random random = new Random();
            int code = random.nextInt(900000) + 100000; // 生成6位随机验证码
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail", data); // 发送验证码到消息队列
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES); // 将验证码存储在Redis，3分钟有效
            return null;
        }
    }

    /**
     * 验证同一IP的操作频率，确保请求不超出限制。
     *
     * @param ip 请求的IP地址
     * @return 是否允许发送验证码，true为允许，false为超出限制
     */
    private boolean verifyLimit(String ip) {
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return flowUtils.limitOnceCheck(key, 60); // 使用FlowUtils工具类，限制每分钟的请求次数
    }

    /**
     * 完成邮箱注册账户的创建，检查验证码的正确性和账户是否已存在。
     *
     * @param vo EmailRegisterVO对象，包含注册所需信息
     * @return 若成功则返回null，失败则返回错误信息
     */
    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email = vo.getEmail();
        String username = vo.getUsername();
        String key = Const.VERIFY_EMAIL_DATA + email;
        String code = stringRedisTemplate.opsForValue().get(key); // 从Redis中获取验证码
        if (code == null) return "请先获取验证码"; // 验证码不存在
        if (!code.equals(vo.getCode())) return "验证码输入错误，请重新输入"; // 验证码不匹配
        if (this.existAccountByEmail(email)) {
            return "此邮箱已注册";
        }
        if (this.existAccountByUsername(username)) {
            return "此用户名已注册";
        }
        String password = encoder.encode(vo.getPassword()); // 加密用户密码
        Account account = new Account(null, username, password, "user", email, new Date());
        if (this.save(account)) {
            stringRedisTemplate.delete(key); // 注册成功后删除Redis中的验证码
            return null;
        } else {
            return "内部错误，请联系管理员"; // 数据库保存失败
        }
    }

    /**
     * 检查指定邮箱是否已被注册。
     *
     * @param email 需要检查的邮箱
     * @return 若邮箱已注册则返回true，否则返回false
     */
    private boolean existAccountByEmail(String email) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email)); // 使用MyBatis-Plus查询邮箱是否存在
    }

    /**
     * 检查指定用户名是否已被注册。
     *
     * @param username 需要检查的用户名
     * @return 若用户名已注册则返回true，否则返回false
     */
    private boolean existAccountByUsername(String username) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username", username)); // 使用MyBatis-Plus查询用户名是否存在
    }
}
