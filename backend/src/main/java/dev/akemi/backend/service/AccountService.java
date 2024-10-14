package dev.akemi.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.akemi.backend.entity.dto.Account;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account> , UserDetailsService {
    Account findAccountByNameOrEmail(String text);
}
