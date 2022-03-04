package com.example.emos.wx.db.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.emos.wx.db.bean.TbMeeting;
import com.example.emos.wx.db.dao.TbMeetingMapper;
import com.example.emos.wx.db.dao.TbUserMapper;
import com.example.emos.wx.db.expection.EmosExpection;
import com.example.emos.wx.db.service.TbMeetingService;
import org.apache.shiro.crypto.hash.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author xiao
* @description 针对表【tb_meeting(会议表)】的数据库操作Service实现
* @createDate 2022-02-04 22:12:29
*/
@Service
public class TbMeetingServiceImpl extends ServiceImpl<TbMeetingMapper, TbMeeting>
    implements TbMeetingService {
    @Autowired
    TbUserMapper tbUserMapper;
    @Autowired
    TbMeetingMapper tbMeetingMapper;
    @Value("${emos.workflow.url}")
    private String workflow;

    @Value("${emos.recieveNotify}")
    private String recieveNotify;

    @Value("${emos.code}")
    private String code;

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public int insertMeeting(TbMeeting entity) {
        //保存数据
        int row = tbMeetingMapper.insertMeeting(entity);
        if (row != 1) {
            throw new EmosExpection("会议添加失败");
        }
        //TODO 开启审批工作流
        String uuid = entity.getUuid();
        int  creatorId = entity.getCreatorId().intValue();
        String start = entity.getStart();
        String date = entity.getDate();
        startMeetingWorkflow(uuid,creatorId,date,start);
        return 0;
    }

    /**
     * 获取新开会议信息
     * @param param
     * @return
     */
    @Override
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param) {
        ArrayList<HashMap> list = tbMeetingMapper.searchMyMeetingListByPage(param);
        String date = null;
        //保存新开的会议
        ArrayList<HashMap> result = new ArrayList<>();
        //保存会议的内容
        JSONArray array = null;
        //保存新开会议的内容
        HashMap hashMap = null;
        for (HashMap map : list){
            String date1 = map.get("date").toString();
            System.out.println(date1);
            if (!date1.equals(date)){
                date = date1;
                hashMap = new HashMap();
                result.add(hashMap);
                hashMap.put("date",date);
                array = new JSONArray();
                hashMap.put("list",array);
            }
            array.put(map);
        }
        return result;
    }
    private void startMeetingWorkflow(String uuid, int creatorId, String date, String start ) {
        HashMap info = tbUserMapper.searchUserInfo(creatorId); //查询创建者用户信息

        JSONObject json = new JSONObject();
        json.set("url", recieveNotify);
        json.set("uuid", uuid);
        json.set("openId", info.get("openId"));
        json.set("code",code);
        json.set("date",date);
        json.set("start", start);
        System.out.println("url"+recieveNotify);
        System.out.println("uuid"+uuid);
        System.out.println("openId"+info.get("openId"));
        System.out.println("code"+code);
        System.out.println("date"+date);
        System.out.println("start"+start);
        String[] roles = info.get("roles").toString().split("，");
        //如果不是总经理创建的会议
        if (!ArrayUtil.contains(roles, "总经理")) {
            //查询总经理ID和同部门的经理的ID
            Integer managerId = tbUserMapper.searchDeptManagerId(creatorId);
            json.set("managerId", managerId); //部门经理ID
            Integer gmId = tbUserMapper.searchGmId();//总经理ID
            json.set("gmId", gmId);
            //查询会议员工是不是同一个部门
            boolean bool = tbMeetingMapper.searchMeetingMembersInSameDept(uuid);
            json.set("sameDept", bool);
        }
        String url = workflow+"/workflow/startMeetingProcess";
        //请求工作流接口，开启工作流
        HttpResponse response = HttpRequest.post(url).header("Content-Type", "application/json").body(json.toString()).execute();
        System.out.println(response.getStatus());
        if (response.getStatus() == 200) {
            json = JSONUtil.parseObj(response.body());
            //如果工作流创建成功，就更新会议状态
            String instanceId = json.getStr("instanceId");
            HashMap param = new HashMap();
            param.put("uuid", uuid);
            param.put("instanceId", instanceId);
            int row = tbMeetingMapper.updateMeetingInstanceId(param); //在会议记录中保存工作流实例的ID
            if (row != 1) {
                throw new EmosExpection("保存会议工作流实例ID失败");
            }
        }
    }

    @Override
    public HashMap searchMeetingById(int id) {
        HashMap hashMap = tbMeetingMapper.searchMeetingById(id);
        ArrayList<HashMap> list = tbMeetingMapper.searchMeetingMembers(id);
        hashMap.put("members",list);
        return hashMap;
    }

    /**
     * 修改会议
     * @param param
     * @return
     */
    @Override
    public int updateMeetingInfo(HashMap param) {
        int id = (int) param.get("id");
        String date = param.get("date").toString();
        String start = param.get("start").toString();
        String instanceId = param.get("instanceId").toString();
        //查询以前的会议
        HashMap hashMap = tbMeetingMapper.searchMeetingById(id);
        int creatorId = Integer.parseInt(hashMap.get("creatorId").toString());
        String uuid = hashMap.get("uuid").toString();
        //更新会议记录
        int i = tbMeetingMapper.updateMeetingInfo(param);
        if (i!=1){
            log.error("会议更新失败");
            throw new EmosExpection("会议更新失败");
        }
        //会议更新成功后删除旧工作流，UUid是每创建一个工作流，会定时创建一个时效为15分钟的视频会议，根据这个uuid删除这个会议
        JSONObject json = new JSONObject();
        json.set("instanceId", instanceId);
        json.set("reason", "会议被修改");
        json.set("uuid",uuid);
        json.set("code",code);
        String url = workflow+"/workflow/deleteProcessById";
        HttpResponse resp = HttpRequest.post(url).header("content-type", "application/json").body(json.toString()).execute();
        if (resp.getStatus()!=200){
            log.error("删除工作流失败");
            throw new EmosExpection("删除工作流失败");
        }
        //创建新工作流
        startMeetingWorkflow(uuid,creatorId,date,start);
        return 0;

    }

    /**
     * 删除会议和工作流
     * @param id
     */
    @Override
    public void deleteMeetingById(int id) {
        HashMap hashMap = tbMeetingMapper.searchMeetingById(id);
        String uuid = hashMap.get("uuid").toString();
        String instanceId = hashMap.get("instanceId").toString();
        DateTime date = DateUtil.parse(hashMap.get("date") + " " + hashMap.get("start"));

        DateTime now = DateUtil.date();
        System.out.println(now);
        System.out.println(date);
        System.out.println((date.offset(DateField.MINUTE,-20)));
        //将date就那些偏移
//        if(now.isAfterOrEquals(date.offset(DateField.MINUTE,-20))){
//            throw new EmosExpection("距离会议开始不足20分钟，不能删除会议");
//        };
        int row = tbMeetingMapper.deleteMeetingById(id);
        if (row != 1) {
            throw new EmosExpection("会议删除失败");
        }
        //删除会议工作流
        JSONObject json = new JSONObject();
        json.set("instanceId", instanceId);
        json.set("reason", "会议被取消");
        json.set("code",code);
        json.set("uuid",uuid);
        String url = workflow+"/workflow/deleteProcessById";
        HttpResponse resp = HttpRequest.post(url).header("content-type", "application/json").body(json.toString()).execute();
        if (resp.getStatus() != 200) {
            log.error("删除工作流失败");
            throw new EmosExpection("删除工作流失败");
        }
    }

    @Override
    public Long searchRoomIdByUUID(String uuid) {
        Object o = redisTemplate.opsForValue().get(uuid);
        long roomid = Long.parseLong(o.toString());
        return roomid;
    }
}




