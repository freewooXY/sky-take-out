package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("员工分页查询传递的参数的数据模型")
public class EmployeePageQueryDTO implements Serializable {

    //员工姓名
    @ApiModelProperty("员工姓名")
    private String name;

    //页码
    @ApiModelProperty("当前页码")
    private int page;

    //每页显示记录数
    @ApiModelProperty("每页记录数")
    private int pageSize;

}
