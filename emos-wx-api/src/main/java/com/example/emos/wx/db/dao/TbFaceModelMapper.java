package com.example.emos.wx.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.emos.wx.db.bean.TbFaceModel;

/**
* @author xiao
* @description 针对表【tb_face_model】的数据库操作Mapper
* @createDate 2022-02-04 22:12:29
* @Entity bean.TbFaceModel
*/
public interface TbFaceModelMapper extends BaseMapper<TbFaceModel> {
    public String searchFaceModel(int userId);
    public void insertFaceModelEntity(TbFaceModel faceModelEntity);
    public int deleteFaceModel(int userId);
}




