package com.example.emos.wx.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.emos.wx.db.bean.TbHolidays;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author xiao
* @description 针对表【tb_holidays(节假日表)】的数据库操作Mapper
* @createDate 2022-02-04 22:12:29
* @Entity bean.TbHolidays
*/
public interface TbHolidaysMapper extends BaseMapper<TbHolidays> {
    Integer searchTodayIsHolidays();
    //假期范围
    public ArrayList<String> searchHolidaysInRange(HashMap param);
}




