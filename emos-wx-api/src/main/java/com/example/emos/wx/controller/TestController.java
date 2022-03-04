package com.example.emos.wx.controller;

import com.example.emos.wx.controller.form.TestFrom;
import com.example.emos.wx.common.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/test")
@Api("测试Web接口")
public class TestController {

    @PostMapping("/sayHello")
    @ApiOperation("最简单的测试方法")
    public R sayHello(@Valid @RequestBody TestFrom from){
        return R.ok().put("message","HelloWorld"+from.getName());
    }

    @PostMapping("/addUser")
    @ApiOperation("测试授权功能")
    @RequiresPermissions(value = {"A","B"},logical = Logical.OR)
    public R add(){
        return R.ok("添加成功");
    }
}

