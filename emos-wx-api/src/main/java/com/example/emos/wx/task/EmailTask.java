package com.example.emos.wx.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class EmailTask {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${emos.email.system}")
    private String system;

    @Async //此方法为异步执行
    public void sendAsync(SimpleMailMessage mailMessage){
        mailMessage.setFrom(system);
        mailMessage.setCc(system);
        javaMailSender.send(mailMessage);

    }

}
