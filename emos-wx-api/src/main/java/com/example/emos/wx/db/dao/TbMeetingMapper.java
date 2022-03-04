package com.example.emos.wx.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.emos.wx.db.bean.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author xiao
* @description 针对表【tb_meeting(会议表)】的数据库操作Mapper
* @createDate 2022-02-04 22:12:29
* @Entity bean.TbMeeting
*/
public interface TbMeetingMapper extends BaseMapper<TbMeeting> {
    public int insertMeeting(TbMeeting entity);
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);
    public boolean searchMeetingMembersInSameDept(String uuid);
    public int updateMeetingInstanceId(HashMap map);
    public HashMap searchMeetingById(int id);
    public ArrayList<HashMap> searchMeetingMembers(int id);
    public int updateMeetingInfo(HashMap param);
    public int deleteMeetingById(int id);
}




