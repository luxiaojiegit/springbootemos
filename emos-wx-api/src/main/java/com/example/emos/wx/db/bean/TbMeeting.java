package com.example.emos.wx.db.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会议表
 * @TableName tb_meeting
 */
@TableName(value ="tb_meeting")
@Data
public class TbMeeting implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * UUID
     */
    @TableField(value = "uuid")
    private String uuid;

    /**
     * 会议题目
     */
    @TableField(value = "title")
    private String title;

    /**
     * 创建人ID
     */
    @TableField(value = "creator_id")
    private Long creatorId;

    /**
     * 日期
     */
    @TableField(value = "date")
    private String date;

    /**
     * 开会地点
     */
    @TableField(value = "place")
    private String place;

    /**
     * 开始时间
     */
    @TableField(value = "start")
    private String start;

    /**
     * 结束时间
     */
    @TableField(value = "end")
    private String end;

    /**
     * 会议类型（1在线会议，2线下会议）
     */
    @TableField(value = "type")
    private short type;

    /**
     * 参与者
     */
    @TableField(value = "members")
    private Object members;

    /**
     * 会议内容
     */
    @TableField(value = "desc")
    private String desc;

    /**
     * 工作流实例ID
     */
    @TableField(value = "instance_id")
    private String instanceId;

    /**
     * 状态（1未开始，2进行中，3已结束）
     */
    @TableField(value = "status")
    private short status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}