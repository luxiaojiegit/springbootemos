package com.example.emos.wx.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.emos.wx.db.bean.TbCheckin;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author xiao
* @description 针对表【tb_checkin(签到表)】的数据库操作Service
* @createDate 2022-02-04 22:12:29
*/
public interface TbCheckinService extends IService<TbCheckin> {
    public String validCanCheckIn(int userId, String date);
    public void checkin(HashMap param);
    public void createFaceModel(int userId,String path);
    //当天签到信息
    public HashMap searchTodayCheckin(int userId);
    //签到总天数
    public long searchCheckinDays(int userId);
    //周签到信息
    public ArrayList<HashMap> searchWeekCheckin(HashMap param);
    //月签到信息(其他就是调用周签到)
    public ArrayList<HashMap> searchMonthCheckin(HashMap param);

}
