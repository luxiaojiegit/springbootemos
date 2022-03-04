package com.example.emos.wx.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.emos.wx.db.bean.TbUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
* @author xiao
* @description 针对表【tb_user(用户表)】的数据库操作Service
* @createDate 2022-02-04 22:12:29
*/
public interface TbUserService extends IService<TbUser> {
    //注册用户
    int resgisterUser(String resgisterCode,String code,String nickName,String photo);
    //根据用户id获取权限信息
    Set<String> seachUserPermissions(int userId);
    //用户登陆：查询是否的存在该用户
    Integer login(String code);
    //根据用户id获取用户信息
    TbUser searchById(int userId);
    //获取入职时间
    public String searchUserHiredate(int userId);
    //获取nickname和图片地址
    public HashMap searchUserSummary(int userId);

    public ArrayList<HashMap> searchUserGroupByDept(String keyword);

    public ArrayList<HashMap> searchMembers(List param);

    public List<HashMap> selectUserPhotoAndName(List param);

    public String searchMemberEmail(int id);

}
