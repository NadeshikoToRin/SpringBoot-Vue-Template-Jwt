package dev.akemi.backend.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
public class AuthorizeVO {
    String userName;
    String role;
    String token;
    Date expire;
}
