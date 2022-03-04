package com.example.emos.wx.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.emos.wx.db.bean.TbUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.*;

/**
* @author xiao
* @description 针对表【tb_user(用户表)】的数据库操作Mapper
* @createDate 2022-02-04 22:12:29
* @Entity bean.TbUser
*/
@Mapper
public interface TbUserMapper extends BaseMapper<TbUser> {
    //检查是否有超级用户存在
    boolean haveRootUser();
    //添加
    Integer insert(Map map);
    //返回主键
    Integer searchIdByOpenId(String openId);
    //根据用户id获取权限信息
    Set<String> seachUserPermissions(int userId);
    //根据用户id获取用户信息
    TbUser searchById(int userId);
    //查询用户和关联的部门
    HashMap searchNameAndDept(int userId);
    //获取入职时间
    public String searchUserHiredate(int userId);
    //获取name和图片地址
    public HashMap searchUserSummary(int userId);

    public ArrayList<HashMap> searchUserGroupByDept(String keyword);

    public ArrayList<HashMap> searchMembers(List param);

    public HashMap searchUserInfo(int userId);
    public int searchDeptManagerId(int id);
    public int searchGmId();
    public List<HashMap> selectUserPhotoAndName(List param);
    public String searchMemberEmail(int id);
}




