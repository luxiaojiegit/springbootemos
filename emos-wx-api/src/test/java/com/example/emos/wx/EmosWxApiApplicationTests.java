package com.example.emos.wx;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.example.emos.wx.config.SystemConstants;

import com.example.emos.wx.db.bean.TbMeeting;
import com.example.emos.wx.db.service.MessageService;
import com.example.emos.wx.db.service.TbMeetingService;
import com.example.emos.wx.task.MessageTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class EmosWxApiApplicationTests {
    @Autowired
    SystemConstants constants;

    @Autowired
    MessageTask messageTask;

    @Autowired
    MessageService messageService;
    @Autowired
    TbMeetingService tbMeetingService;
    @Test
    void contextLoads() {
//        //判断签到
//        DateTime date = DateUtil.date();//当前时间
//        System.out.println("当前时间："+date);
//        //上班时间
//        DateTime startTime = DateUtil.parse( constants.getAttendanceTime());
//        System.out.println("上班时间："+startTime);
//        //下班时间
//        DateTime endTime = DateUtil.parse(constants.getClosingEndTime());
//        for (int i = 1; i <= 100; i++) {
//            MessageEntity message = new MessageEntity();
//            message.setUuid(IdUtil.simpleUUID());
//            message.setSenderId(0);
//            message.setSenderName("系统消息");
//            message.setMsg("这是第" + i + "条测试消息");
//            message.setSendTime(new Date());
//            String id=messageService.insertMessage(message);
//
//            MessageRefEntity ref=new MessageRefEntity();
//            ref.setMessageId(id);
//            ref.setReceiverId(23); //注意：这是接收人ID
//            ref.setLastFlag(true);
//            ref.setReadFlag(false);
//            messageService.insertRef(ref);
            for (int i = 2 ; i<100 ; i++){
                TbMeeting meeting=new TbMeeting();
                meeting.setId((long)i);
                meeting.setUuid(IdUtil.simpleUUID());
                meeting.setTitle("测试会议"+i);
                meeting.setCreatorId(23L); //ROOT用户ID
                meeting.setDate(DateUtil.today());
                meeting.setPlace("线上会议室");
                meeting.setStart("08:30");
                meeting.setEnd("10:30");
                meeting.setType((short) 1);
                meeting.setMembers("[23,16]");
                meeting.setDesc("会议研讨Emos项目上线测试");
                meeting.setInstanceId(IdUtil.simpleUUID());
                meeting.setStatus((short)3);
                meeting.setCreateTime(new Date());
                tbMeetingService.insertMeeting(meeting);
            }


    }


}
