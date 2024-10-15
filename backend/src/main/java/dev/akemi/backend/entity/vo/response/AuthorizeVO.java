package dev.akemi.backend.entity.vo.response;

import dev.akemi.backend.entity.BaseData;
import lombok.Data;

import java.util.Date;

@Data
public class AuthorizeVO  {
    String username;
    String role;
    String token;
    Date expire;
}
