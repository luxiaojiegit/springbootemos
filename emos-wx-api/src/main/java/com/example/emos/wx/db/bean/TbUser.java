package com.example.emos.wx.db.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 * @TableName tb_user
 */
@TableName(value ="tb_user")
@Data
public class TbUser implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * 长期授权字符串
     */
    @TableField(value = "open_id")
    private String openId;

    /**
     * 昵称
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 头像网址
     */
    @TableField(value = "photo")
    private String photo;

    /**
     * 姓名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 性别
     */
    @TableField(value = "sex")
    private Object sex;

    /**
     * 手机号码
     */
    @TableField(value = "tel")
    private String tel;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 入职日期
     */
    @TableField(value = "hiredate")
    private Date hiredate;

    /**
     * 角色
     */
    @TableField(value = "role")
    private Object role;

    /**
     * 是否是超级管理员
     */
    @TableField(value = "root")
    private Integer root;

    /**
     * 部门编号
     */
    @TableField(value = "dept_id")
    private int deptId;

    /**
     * 状态
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}