package com.example.emos.wx.db.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.bean.TbCheckin;
import com.example.emos.wx.db.bean.TbFaceModel;
import com.example.emos.wx.db.dao.*;
import com.example.emos.wx.db.expection.EmosExpection;
import com.example.emos.wx.db.service.TbCheckinService;
import com.example.emos.wx.task.EmailTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static cn.hutool.json.XMLTokener.entity;

/**
* @author xiao
* @description 针对表【tb_checkin(签到表)】的数据库操作Service实现
* @createDate 2022-02-04 22:12:29
*/
@Service
@Scope("prototype")
@Slf4j
public class TbCheckinServiceImpl extends ServiceImpl<TbCheckinMapper, TbCheckin>
    implements TbCheckinService {

    @Autowired
    SystemConstants constants;

    @Autowired
    TbHolidaysMapper tbHolidaysMapper;

    @Autowired
    TbWorkdayMapper tbWorkdayMapper;

    @Autowired
    TbCheckinMapper tbCheckinMapper;

    @Autowired
    TbFaceModelMapper tbFaceModelMapper;

    @Autowired
    TbCityMapper cityMapper;

    @Value("${emos.email.hr}")
    private String hr;

    @Autowired
    private EmailTask emailTask;

    @Value("${emos.face.checkinUrl}")
    private String checkin;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.code}")
    private String code;

    @Autowired
    TbUserMapper userMapper;

    /**
     * 查看是否已经签到
     * @param userId
     * @param date
     * @return
     */
    @Override
    public String validCanCheckIn(int userId, String date) {
        boolean bool_1 = tbHolidaysMapper.searchTodayIsHolidays()!=null?true:false;
        boolean bool_2 = tbWorkdayMapper.searchTodayIsWork()!=null?true:false;
        String type = "工作日";
        if (DateUtil.date().isWeekend()){
            type = "休息日";
        }
        if (bool_1){
            type = "休息日";
        }else if (bool_2){
            type = "工作日";
        }
        if (type.equals("休息日")){
            return "休息日不需要打卡";
        }else {
            //当前时间
            DateTime now = DateUtil.date();
            //上班开始打卡时间
            String start = constants.getAttendanceStartTime();
            DateTime statTime = DateUtil.parse(start);
            //上班结束打卡时间
            String end = constants.getAttendanceEndTime();
            DateTime endTime = DateUtil.parse(end);

            if (now.isBefore(statTime)){
                return "没有到上班打卡时间";
            }else if (now.isAfter(endTime)){
                return "已经过了上班打卡时间";
            }else{
                //查看是否重复考勤
                HashMap hashMap = new HashMap();
                hashMap.put("userId",userId);
                hashMap.put("date",date);
                hashMap.put("start",start);
                hashMap.put("end",end);
                boolean bool = tbCheckinMapper.haveCheckin(hashMap)!=null?true:false;
                return bool ? "今日已经考勤，不可重复" : "今日可以考勤";
            }

        }
    }

    /**
     * 签到
     * @param param
     */
    @Override
    public void checkin(HashMap param) {
        //判断签到
        DateTime date = DateUtil.date();//当前时间
        //上班时间
        DateTime startTime = DateUtil.parse( constants.getAttendanceTime());
        //下班时间
        DateTime endTime = DateUtil.parse(constants.getClosingEndTime());
        String address = (String) param.get("address");
        String country = (String) param.get("country");
        String province = (String) param.get("province");
        int status = 1;
        if (date.compareTo(startTime)<=0){
            status = 1;//正常敲到
        }else if (date.compareTo(startTime)>0 && date.compareTo(endTime)<0){
            status = 2;//迟到
        }
        //查询签到人的人脸数据模型
        int userId = (int) param.get("userId");
        String faceModel = tbFaceModelMapper.searchFaceModel(userId);
        if (faceModel ==null){
            throw new EmosExpection("不存在人脸模型");
        }else {
            String path = (String) param.get("path");
            HttpRequest post = HttpUtil.createPost(checkin);
            //传入参数
            post.form("photo", FileUtil.file(path),"targetModel",faceModel);
            post.form("code",code);
            //执行
            HttpResponse response = post.execute();
            if (response.getStatus() !=200){
                log.error("人脸识别异常");
                throw new EmosExpection("人脸识别异常");
            }

            String body = response.body();
            if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)){
                throw new EmosExpection(body);
            }else if ("False".equals(body)){
                throw new EmosExpection("签到无效，非本人签到");
            }else if("True".equals(body)){
                int risk =1;
                String city = (String) param.get("city");
                String district = (String) param.get("district");
                if (!StrUtil.isBlank(city) && !StrUtil.isBlank(district)){
                    String code = cityMapper.searchCode(city);
                    String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + district;
                    try {
                        Document document = Jsoup.connect(url).get();
                        Elements docuements = document.getElementsByClass(" list-detail");
                        for(Element one : docuements){
                            String s = one.text().split(" ")[1];
                            if ("高风险".equals(s)){
                                risk = 3;
                                //发送邮件
                                HashMap hashMap = userMapper.searchNameAndDept(userId);
                                String name = (String) hashMap.get("name");
                                String deptName = (String) hashMap.get("dept_name");
                                deptName = deptName!=null?deptName:"";
                                SimpleMailMessage mss = new SimpleMailMessage();
                                mss.setTo(hr);
                                mss.setSubject("员工"+name+"身处高风险地区");
                                mss.setText(deptName + "员工" + name + "，" + DateUtil.format(new Date(), "yyyy年MM月dd日") + "处于" + address + "，属于新冠疫情高风险地区，请及时与该员工联系，核实情况！");
                                //发送邮件
                                emailTask.sendAsync(mss);
                            }else if("中风险".equals(s)){
                                risk = risk<2?2:risk;
                            }
                        }
                    } catch (IOException e) {
                        log.error("执行异常", e);
                        throw new EmosExpection("获取风险等级失败");
                    }
                }
                //保存签到记录
                TbCheckin entity=new TbCheckin();
                System.out.println(userId);
                entity.setUserId(userId);
                entity.setAddress(address);
                entity.setCountry(country);
                entity.setProvince(province);
                entity.setCity(city);
                entity.setDistrict(district);
                entity.setStatus((byte) status);
                entity.setRisk(risk);
                entity.setDate(DateUtil.today());
                entity.setCreateTime(date);
                tbCheckinMapper.insert1(entity);
            }

        }
    }

    /**
     * 创建人脸
     * @param userId
     * @param path
     */
    @Override
    public void createFaceModel(int userId, String path) {
        HttpRequest post = HttpUtil.createPost(createFaceModelUrl);
        post.form("photo",FileUtil.file(path));
        post.form("code",code);
        HttpResponse response = post.execute();
        String body = response.body();
        if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
            throw new EmosExpection(body);
        }else {
            TbFaceModel tbFaceModel = new TbFaceModel();
            tbFaceModel.setUserId(userId);
            tbFaceModel.setFaceModel(body);
            tbFaceModelMapper.insertFaceModelEntity(tbFaceModel);
        }
    }

    @Override
    public HashMap searchTodayCheckin(int userId) {
        HashMap hashMap = tbCheckinMapper.searchTodayCheckin(userId);
        return hashMap;
    }

    @Override
    public long searchCheckinDays(int userId) {
        return tbCheckinMapper.searchCheckinDays(userId);
    }

    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        ArrayList<HashMap> checkinList = tbCheckinMapper.searchWeekCheckin(param);
        //假期
        ArrayList<String> holidays = tbHolidaysMapper.searchHolidaysInRange(param);
        //工作日
        ArrayList<String> works = tbWorkdayMapper.searchWorkdayInRange(param);

        //开始时间
        DateTime startDate = DateUtil.parse(param.get("startDate").toString());
        //结束解释
        DateTime endDate = DateUtil.parse(param.get("endDate").toString());

        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        ArrayList arrayList = new ArrayList();
        range.forEach(one ->{
            String data = one.toString("yyyy-MM-dd");
            //查看今天是不是假期或者工作日
            String type="工作日";
            if (one.isWeekend()){
                type = "假期";
            }
            if (holidays!=null&&holidays.contains(data)){
                type = "假期";
            }
            if (works!=null&&works.contains(data)){
                type = "工作日";
            }
            //查看是否旷工
            String status ="";
            boolean flage = false;
            if (type.equals("工作日") && DateUtil.compare(one,DateUtil.date()) <=0){
                status = "缺勤";
                for (HashMap<String,String> map:checkinList){
                    if (map.containsValue(data)){
                        status = map.get("status");
                        flage = true;
                        break;
                    }
                }
            }
            //在下班前没有签到的:status=""
            DateTime endTime = DateUtil.parse(constants.attendanceEndTime);
            String today = DateUtil.today();
            if (data.equals(today) && DateUtil.date().isBefore(endTime)&& flage==false){
                status = "";
            }
            HashMap hashMap = new HashMap();
            hashMap.put("data",data);
            hashMap.put("status",status);
            hashMap.put("type",type);
            //这是周几周几
            hashMap.put("day",one.dayOfWeekEnum().toChinese("周"));
            arrayList.add(hashMap);
        });

        return arrayList;
    }

    /**
     * 返回月签到
     * @param param
     * @return
     */
    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return this.searchWeekCheckin(param);
    }
}




