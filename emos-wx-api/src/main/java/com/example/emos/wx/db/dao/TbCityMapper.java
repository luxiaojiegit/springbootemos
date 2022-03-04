package com.example.emos.wx.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.emos.wx.db.bean.TbCity;

/**
* @author xiao
* @description 针对表【tb_city(疫情城市列表)】的数据库操作Mapper
* @createDate 2022-02-04 22:12:29
* @Entity bean.TbCity
*/
public interface TbCityMapper extends BaseMapper<TbCity> {
    String searchCode(String City);
}




