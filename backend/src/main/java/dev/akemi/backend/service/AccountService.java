package dev.akemi.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.akemi.backend.entity.dto.Account;
import dev.akemi.backend.entity.vo.request.EmailRegisterVO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account> , UserDetailsService {
    Account findAccountByNameOrEmail(String text);

    String registerEmailVerifyCode(String type,String  email, String ip);

    String registerEmailAccount(EmailRegisterVO vo);
}
