package com.example.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.controller.form.CheckinForm;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.SearchMonthCheckinForm;
import com.example.emos.wx.db.expection.EmosExpection;
import com.example.emos.wx.db.service.TbCheckinService;
import com.example.emos.wx.db.service.TbUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

@RequestMapping("/checkin")
@RestController
@Slf4j
@Api("签到模块")
public class CheckinController {
    @Autowired
    JwtUtil jwtUtil;

    @Value("${emos.image-folder}")
    private String imageFolder;

    @Autowired
    TbCheckinService tbCheckinService;

    @Autowired
    TbUserService tbUserService;

    @Autowired
    SystemConstants constants;

    @GetMapping("/validCanCheckIn")
    @ApiOperation("检查当天是否可签到")
    public R validCanCheckIn( @RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        String result = tbCheckinService.validCanCheckIn(userId, DateUtil.today());
        return R.ok(result);
    }

    @PostMapping ("/checkin")
    @ApiOperation("签到")
    public R checkin(@Valid CheckinForm form, @RequestParam("photo")MultipartFile file,
                     @RequestHeader("token") String token){
        if (file==null){
            throw new EmosExpection("照片没有上传");
        }
        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        String path = imageFolder +"/"+fileName;
        if (!fileName.endsWith(".jpg")){
            throw new EmosExpection("照片格式不对");
        }else {
            try {
                file.transferTo(Paths.get(path));
                HashMap param=new HashMap();
                param.put("userId",userId);
                param.put("path",path);
                param.put("city",form.getCity());
                param.put("district",form.getDistrict());
                param.put("address",form.getAddress());
                param.put("country",form.getCountry());
                param.put("province",form.getProvince());
                tbCheckinService.checkin(param);
                return R.ok("签到成功");
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new EmosExpection("保存图片错误");
            }finally {
                FileUtil.del(path);
            }
        }
    }

    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestHeader("token") String token,@RequestParam("photo")MultipartFile file){
        if (file ==null){
            throw new EmosExpection("照片没有上传");
        }
        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        String path = imageFolder +"/"+fileName;
        if (!fileName.endsWith(".jpg")){
            throw new EmosExpection("图片格式错误");
        }else {
            try {
                file.transferTo(Paths.get(path));
                tbCheckinService.createFaceModel(userId,path);
                return R.ok("人脸建模成功");
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new EmosExpection("保存图片错误");
            }finally {
                FileUtil.del(path);
            }
        }
    }

    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询用户当日签到数据")
    public R searchTodayCheckin(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap map = tbCheckinService.searchTodayCheckin(userId);
        map.put("attendanceTime",constants.attendanceTime);
        map.put("closingTime", constants.closingTime);
        long days = tbCheckinService.searchCheckinDays(userId);
        map.put("checkinDays", days);

        //判断日期是否在用户入职前
        DateTime hiredate = DateUtil.parse(tbUserService.searchUserHiredate(userId));
        //本周开始日期，也就是周一
        DateTime beginTime = DateUtil.beginOfWeek(DateUtil.date());
        if (beginTime.isBefore(hiredate)){
            beginTime = hiredate;
        }
        //本周结束日期
        DateTime entTime = DateUtil.endOfWeek(DateUtil.date());

        //创建map传入参数到业务层
        HashMap param = new HashMap();
        param.put("startDate", beginTime.toString());
        param.put("endDate", entTime.toString());
        param.put("userId", userId);

        ArrayList<HashMap> list = tbCheckinService.searchWeekCheckin(param);
        map.put("weekCheckin", list);
        return R.ok().put("result",map);
    }

    @PostMapping("/searchMonthCheckin")
    @ApiOperation("查询用户某月签到数据")
    public R searchMonthCheckin(@Valid @RequestBody SearchMonthCheckinForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        //查询入职日期
        DateTime hiredate = DateUtil.parse(tbUserService.searchUserHiredate(userId));
        //把月份处理成双数字
        String month = form.getMonth() < 10 ? "0" + form.getMonth() : form.getMonth().toString();
        //某年某月的起始日期
        DateTime startDate = DateUtil.parse(form.getYear() + "-" + month + "-01");
        //如果查询的月份早于员工入职日期的月份就抛出异常
        if (startDate.isBefore(DateUtil.beginOfMonth(hiredate))){
            throw new EmosExpection("只能查询考勤之后日期的数据");
        }
        //如果查询月份与入职月份恰好是同月，本月考勤查询开始日期设置成入职日期
        if (startDate.isBefore(hiredate)){
            startDate = hiredate;
        }
        //结束日期
        DateTime endDate = DateUtil.endOfMonth(startDate);
        HashMap hashMap = new HashMap();
        hashMap.put("userId", userId);
        hashMap.put("startDate",startDate.toString());
        hashMap.put("endDate",endDate.toString());
        ArrayList<HashMap> list = tbCheckinService.searchMonthCheckin(hashMap);
        //统计月考勤数据
        int sum_1 = 0, sum_2 = 0, sum_3 = 0;
        for (HashMap map : list){
            String type = (String) map.get("type");
            String status = (String) map.get("status");
            if (type.equals("工作日")){
                if ("正常".equals(status)) {
                    sum_1++;
                } else if ("迟到".equals(status)) {
                    sum_2++;
                } else if ("缺勤".equals(status)) {
                    sum_3++;
                }
            }
        }
        return  R.ok().put("list",list).put("sum_1",sum_1).put("sum_2",sum_2).put("sum_3",sum_3);
    }
}
