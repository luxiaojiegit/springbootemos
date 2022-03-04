package com.example.emos.wx.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.emos.wx.db.bean.TbWorkday;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author xiao
* @description 针对表【tb_workday】的数据库操作Mapper
* @createDate 2022-02-04 22:12:29
* @Entity bean.TbWorkday
*/
public interface TbWorkdayMapper extends BaseMapper<TbWorkday> {
    Integer searchTodayIsWork();
    //工作范围
    public ArrayList<String> searchWorkdayInRange(HashMap param);
}




