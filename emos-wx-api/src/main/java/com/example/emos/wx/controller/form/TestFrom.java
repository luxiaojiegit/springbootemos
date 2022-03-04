package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

//表示swagger的实体类
@ApiModel
@Data
public class TestFrom {
    @NotNull //不能为空
//    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,15}$")
//    @ApiModelProperty("姓名")
    private String name;
}
