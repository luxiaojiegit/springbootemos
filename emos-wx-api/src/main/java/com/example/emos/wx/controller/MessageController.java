package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.DeleteMessageRefByIdForm;
import com.example.emos.wx.controller.form.SearchMessageByIdForm;
import com.example.emos.wx.controller.form.SearchMessageByPageForm;
import com.example.emos.wx.controller.form.UpdateUnreadMessageForm;
import com.example.emos.wx.db.service.MessageService;
import com.example.emos.wx.db.service.impl.MessageServiceImpl;
import com.example.emos.wx.task.MessageTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.management.Query;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

@RequestMapping("/message")
@Api("消息模块接口")
@RestController
public class MessageController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    MessageService service;

    @Autowired
    MessageTask messageTask;

    @PostMapping("/searchMessageByPage")
    @ApiOperation("获取分页消息队列")
    public R searchMessageByPage(@Valid @RequestBody SearchMessageByPageForm form
    , @RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        int page = form.getPage();
        int length = form.getLength();
        long start = (page-1)*length;
        List<HashMap> hashMaps = service.searchMessageByPage(userId, start, length);
        return R.ok().put("result",hashMaps);
    }

    @PostMapping("/searchMessageById")
    @ApiOperation("根据ID查询消息")
    public R searchMessageById(@Valid @RequestBody SearchMessageByIdForm form){
        HashMap hashMap = service.searchMessageById(form.getId());
        return R.ok().put("result",hashMap);
    }

    @PostMapping("/updateUnreadMessage")
    @ApiOperation("未读消息更新成已读消息")
    public R updateUnreadMessage(@Valid @RequestBody UpdateUnreadMessageForm form){
        long l = service.updateUnreadMessage(form.getId());
        return R.ok().put("result",l==1?true:false);
    }

    @PostMapping("/deleteMessageRefById")
    @ApiOperation("删除消息")
    public R deleteMessageRefById(@Valid @RequestBody DeleteMessageRefByIdForm form){
        long rows=service.deleteMessageRefById(form.getId());
        return R.ok().put("result", rows    == 1 ? true : false);
    }

    @GetMapping("/refreshMessage")
    @ApiOperation("刷新用户的消息")
    public R refreshMessage(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        //异步接收消息
        messageTask.receiveAysnc(userId + "");
        //查询接收了多少条消息
        long lastRows=service.searchLastCount(userId);
        //查询未读数
        Query query = new Query();
        long unreadRows = service.searchUnreadCount(userId);
        System.out.println(unreadRows);
        System.out.println(lastRows);
        return R.ok().put("lastRows", lastRows).put("unreadRows", unreadRows);
    }
}
