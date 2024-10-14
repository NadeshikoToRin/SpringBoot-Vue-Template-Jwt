package dev.akemi.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.akemi.backend.entity.dto.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
