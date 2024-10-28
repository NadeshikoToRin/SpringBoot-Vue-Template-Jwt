package dev.akemi.backend.controller;

import dev.akemi.backend.entity.RestBean;
import dev.akemi.backend.entity.vo.request.EmailRegisterVO;
import dev.akemi.backend.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthorizeController {

    private final String TYPE_REGEX = "^(register|reset)$";

    @Resource
    AccountService accountService;

    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = TYPE_REGEX) String type,
                                        HttpServletRequest request){
        return this.messageHandle(() ->
            accountService.registerEmailVerifyCode(type,email,request.getRemoteAddr())
        );
    }

    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVO vo){
        return this.messageHandle(()-> accountService.registerEmailAccount(vo));
    }

    private RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        return message == null ? RestBean.success() : RestBean.failure(400,message);
    }
}
