package com.example.emos.wx.controller;

import cn.hutool.json.JSONUtil;
import com.example.emos.wx.config.tencent.TLSSigAPIv2;
import com.example.emos.wx.controller.form.*;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.db.expection.EmosExpection;
import com.example.emos.wx.db.service.TbUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Api("用户模块接口")
public class UserController {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    TbUserService tbUserService;

    @Autowired
    JwtUtil jwtUtil;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @Value("${trtc.appid}")
    private Integer appid;

    @Value("${trtc.key}")
    private String key;

    @Value("${trtc.expire}")
    private Integer expire;

    @PostMapping("/register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegisterForm registerForm){
        //注册用户
        int userId = tbUserService.resgisterUser(registerForm.getRegisterCode(), registerForm.getCode(), registerForm.getNickname(), registerForm.getPhoto());
        //生成令牌
        String token = jwtUtil.CreateToken(userId);
        //返回该用户权限
        Set<String> permissions = tbUserService.seachUserPermissions(userId);
        //利用redis设置令牌
        saveTokenByRedis(token,userId);
        return R.ok("用户注册成功").put("token",token).put("permission",permissions);
    }

    @PostMapping("/login")
    @ApiOperation("登陆系统")
    public R login(@Valid @RequestBody LoginForm loginForm){
        Integer userId = tbUserService.login(loginForm.getCode());
        //生成临牌
        String token = jwtUtil.CreateToken(userId);
        //redis
        saveTokenByRedis(token,userId);
        //返回用户权限
        Set<String> permissions = tbUserService.seachUserPermissions(userId);
        return R.ok("登陆成功").put("token",token).put("permissions",permissions);
    }

    @GetMapping("/searchUserSummary")
    @ApiOperation("查询用户摘要信息")
    public R searchUserSummary(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap map = tbUserService.searchUserSummary(userId);
        return R.ok().put("result", map);
    }

    /**
     * 利用redis设置令牌
     * @param token
     * @param userId
     */
    private void saveTokenByRedis(String token,int userId){
        redisTemplate.opsForValue().set(token,userId+"",cacheExpire, TimeUnit.DAYS);
    }


    @PostMapping("/searchUserGroupByDept")
    @ApiOperation("查询员工列表，按照部门分组排列")
    @RequiresPermissions(value = {"ROOT", "EMPLOYEE:SELECT"}, logical = Logical.OR)
    public R searchUserGroupByDept(@Valid @RequestBody SearchUserGroupByDeptForm form) {
        ArrayList<HashMap> list = tbUserService.searchUserGroupByDept(form.getKeyword());
        return R.ok().put("result", list);
    }

    @PostMapping("/searchMembers")
    @ApiOperation("查询成员")
    @RequiresPermissions(value = {"ROOT","MEETING:INSERT", "MEETING:UPDATE"},logical = Logical.OR)
    public R searchMembers(@Valid @RequestBody SearchMembersForm form) {
        if (!JSONUtil.isJsonArray(form.getMembers())){
            throw new EmosExpection("members不是json数组");
        }
        List<Integer> integers = JSONUtil.parseArray(form.getMembers()).toList(Integer.class);
        ArrayList<HashMap> list = tbUserService.searchMembers(integers);
        return R.ok().put("result",list);
    }
    @PostMapping("/selectUserPhotoAndName")
    @ApiOperation("查询用户姓名和头像")
    @RequiresPermissions(value = {"WORKFLOW:APPROVAL"})
    public R selectUserPhotoAndName(@Valid @RequestBody SelectUserPhotoAndNameForm form){
        if (!JSONUtil.isJsonArray(form.getIds())){
            throw new EmosExpection("不是json数组");
        }
        List<Integer> integers = JSONUtil.parseArray(form.getIds()).toList(Integer.class);
        List<HashMap> hashMaps = tbUserService.selectUserPhotoAndName(integers);
        return R.ok().put("result",hashMaps);
    }

    @GetMapping("/genUserSig")
    @ApiOperation("生成用户签名")
    public R genUserSig(@RequestHeader("token") String token) {
        int id = jwtUtil.getUserId(token);
        String email = tbUserService.searchMemberEmail(id);
        TLSSigAPIv2 api = new TLSSigAPIv2(appid, key);
        String userSig = api.genUserSig(email, expire);
        return R.ok().put("userSig", userSig).put("email", email);
    }

}
