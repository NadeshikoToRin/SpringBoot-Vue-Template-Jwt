package dev.akemi.backend.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {
    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;

    @RabbitHandler
    public void sendMailMessage(Map<String,Object> data){
        String email = (String) data.get("email");
        Integer code = (Integer) data.get("code");
        String type = (String) data.get("type");
        SimpleMailMessage message = switch(type){
            case "register" -> creatMessage("欢迎注册我们的网站",
                    "零点邮件注册校验码为："+code+"\n验证码3分钟内有效，请勿泄露给其他人。",email);
            case "reset" -> creatMessage("重置密码验证码",
                    "您正在进行重置密码操作，验证码："+code+"\n验证码3分钟内有效，请勿泄露给其他人。",email);
            default -> null;
        };
        if(message==null) return;
        sender.send(message);
    }

    private SimpleMailMessage creatMessage(String title,String content,String target){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(target);
        message.setFrom(username);
        return message;
    }
}
