package com.example.emos.wx;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.bean.SysConfig;
import com.example.emos.wx.db.bean.TbMeeting;
import com.example.emos.wx.db.dao.SysConfigMapper;

import com.example.emos.wx.db.service.TbMeetingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;


@SpringBootApplication
@MapperScan("com.example.emos.wx.db.dao")
@ServletComponentScan
@Slf4j
@EnableAsync//开启异步多线程
public class EmosWxApiApplication {
    @Autowired
    SysConfigMapper sysConfigMapper;

    @Autowired
    SystemConstants constants;


    @Value("${emos.image-folder}")
    private String imageFolder;

    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }

    @PostConstruct
    public void init(){
        List<SysConfig> sysConfigs = sysConfigMapper.selectAllparm();
        sysConfigs.forEach(one->{
            String key = one.getParamKey();

            //进行驼峰转换
            key = StrUtil.toCamelCase(key);
            String value = one.getParamValue();

            //进行驼峰转换
            value = StrUtil.toCamelCase(value);
            //进行反射
            try {
                Field file = constants.getClass().getDeclaredField(key);
                file.set(constants,value);
            } catch (Exception e) {
                log.error("反射执行异常",e);
            }
        });

        new File(imageFolder).mkdirs();
    }



}
