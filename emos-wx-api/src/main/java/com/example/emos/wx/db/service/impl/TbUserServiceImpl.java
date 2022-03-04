package com.example.emos.wx.db.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.db.bean.MessageEntity;
import com.example.emos.wx.db.bean.TbUser;
import com.example.emos.wx.db.dao.TbDeptMapper;
import com.example.emos.wx.db.dao.TbUserMapper;
import com.example.emos.wx.db.expection.EmosExpection;
import com.example.emos.wx.db.service.TbUserService;
import com.example.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.jta.WebSphereUowTransactionManager;

import java.util.*;

/**
* @author xiao
* @description 针对表【tb_user(用户表)】的数据库操作Service实现
* @createDate 2022-02-04 22:12:29
*/
@Service
@Scope("prototype")
@Slf4j
public class TbUserServiceImpl extends ServiceImpl<TbUserMapper, TbUser>
    implements TbUserService {
    @Autowired
    TbUserMapper userMapper;

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    MessageTask messageTask;

    @Autowired
    TbDeptMapper deptMapper;


    /**
     * 获取openid
     * @param code
     * @return
     */
    public String getOpenid(String code){
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        map.put("appid", appId);
        System.out.println(appId);
        System.out.println(appSecret);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String response = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String openId = json.getStr("openid");
        if (openId == null || openId.length() == 0) {
            throw new RuntimeException("临时登陆凭证错误");
        }
        return openId;
    }

    /**
     * 用户注册
     * @param resgisterCode
     * @param code
     * @param nickName
     * @param photo
     * @return
     */
    @Override
    public int resgisterUser(String resgisterCode, String code, String nickName, String photo) {
        //检查用户是否注册超级用户
        if (resgisterCode.equals("000000")){
            //是：检查是否数据库已有超级用户
            boolean b = userMapper.haveRootUser();
            System.out.println(b);
            if (!b){
                //没有注册过：进行绑定
                String openId = getOpenid(code);
                HashMap param = new HashMap();
                param.put("openId", openId);
                param.put("nickname", nickName);
                param.put("photo", photo);
                param.put("role", "[0]");
                param.put("status", 1);
                param.put("createTime", new Date());
                param.put("root", true);
                userMapper.insert(param);
                //根据openId获取此条数据添加的主键
                Integer integer = userMapper.searchIdByOpenId(openId);
                //注册信息到mongdb
                MessageEntity entity = new MessageEntity();
                entity.setSenderId(0);
                entity.setSenderName("系统消息");
                entity.setUuid(IdUtil.simpleUUID());
                entity.setMsg("欢迎您注册成为超级管理员，请及时更新你的员工个人信息。");
                entity.setSendTime(new Date());
                messageTask.sendAsync(integer + "", entity);
                return integer;
            }else {
                //如果root已经绑定了，就抛出异常
                throw new EmosExpection("无法绑定超级管理员账号");
            }
        }else {
            //其他用户的注册
            return 0;
        }
    }

    /**
     * 返回用户权限信息
     * @param userId
     * @return
     */
    @Override
    public Set<String> seachUserPermissions(int userId) {
        Set<String> permissions = userMapper.seachUserPermissions(userId);
        return permissions;
    }

    /**
     * 用户登陆
     * @param code
     * @return
     */
    @Override
    public Integer login(String code) {
        String openid = getOpenid(code);
        Integer userId = userMapper.searchIdByOpenId(openid);
        if (userId == null){
            throw new EmosExpection("用户不存在");
        }
        return userId;
    }

    /**
     * 据用户id获取用户信息
     *
     * @param userId
     * @return
     */
    @Override
    public TbUser searchById(int userId) {
        TbUser tbUser = userMapper.searchById(userId);
        return tbUser;
    }

    /**
     * 获取入职时间
     * @param userId
     * @return
     */
    @Override
    public String searchUserHiredate(int userId) {
        return  userMapper.searchUserHiredate(userId);
    }

    /**
     * 获取nickname和图片地址
     * @param userId
     * @return
     */
    @Override
    public HashMap searchUserSummary(int userId) {
        return userMapper.searchUserSummary(userId);
    }

    /**
     *获取部门员工
     * @param keyword
     * @return
     */
    @Override
    public ArrayList<HashMap> searchUserGroupByDept(String keyword) {
        ArrayList<HashMap> list_1 = deptMapper.searchDeptMembers(keyword);
        ArrayList<HashMap> list_2 = userMapper.searchUserGroupByDept(keyword);

        for (HashMap map_1:list_1){
            long deptId = (long) map_1.get("id");
            ArrayList members = new ArrayList();
            for (HashMap map_2:list_2){
               long id = (long) map_2.get("deptId");
               if (id ==deptId){
                   members.add(map_2);
               }
            }
            map_1.put("members",members);
        }
        return list_1;
    }

    @Override
    public ArrayList<HashMap> searchMembers(List param) {
        ArrayList<HashMap> list = userMapper.searchMembers(param);
        return list;
    }

    @Override
    public List<HashMap> selectUserPhotoAndName(List param) {
        List<HashMap> list = userMapper.selectUserPhotoAndName(param);
        return list;
    }

    @Override
    public String searchMemberEmail(int id) {
        String email = userMapper.searchMemberEmail(id);
        return email;
    }
}




