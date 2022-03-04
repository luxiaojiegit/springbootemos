package com.example.emos.wx.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.emos.wx.db.bean.TbDept;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author xiao
* @description 针对表【tb_dept】的数据库操作Mapper
* @createDate 2022-02-04 22:12:29
* @Entity bean.TbDept
*/
public interface TbDeptMapper extends BaseMapper<TbDept> {

    public ArrayList<HashMap> searchDeptMembers(String keyword);
}




