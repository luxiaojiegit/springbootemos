package com.example.emos.wx.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.emos.wx.db.bean.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author xiao
* @description 针对表【tb_meeting(会议表)】的数据库操作Service
* @createDate 2022-02-04 22:12:29
*/
public interface TbMeetingService extends IService<TbMeeting> {
    public int insertMeeting(TbMeeting entity);
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);
    public HashMap searchMeetingById(int id);
    public int updateMeetingInfo(HashMap param);
    public void deleteMeetingById(int id);
    public Long searchRoomIdByUUID(String uuid);
}
