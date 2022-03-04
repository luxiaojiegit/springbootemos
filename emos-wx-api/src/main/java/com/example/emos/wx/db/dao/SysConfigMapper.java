package com.example.emos.wx.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.emos.wx.db.bean.SysConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author xiao
* @description 针对表【sys_config】的数据库操作Mapper
* @createDate 2022-02-04 21:49:22
* @Entity bean.SysConfig
*/
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
    List<SysConfig> selectAllparm();
}




