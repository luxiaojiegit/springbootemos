package com.example.emos.wx.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.emos.wx.db.bean.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* @author xiao
* @description 针对表【tb_checkin(签到表)】的数据库操作Mapper
* @createDate 2022-02-04 22:12:29
* @Entity bean.TbCheckin
*/

public interface TbCheckinMapper extends BaseMapper<TbCheckin> {
    Integer haveCheckin(Map map);
    void insert1(TbCheckin tbCheckin);
    //查询今日签到情况
    public HashMap searchTodayCheckin(int userId);
    //查询签到次数
    public long searchCheckinDays(int userId);
    //查询周签到情况
    public ArrayList<HashMap> searchWeekCheckin(HashMap param);
}




