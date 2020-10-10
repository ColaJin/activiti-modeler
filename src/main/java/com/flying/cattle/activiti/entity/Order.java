package com.flying.cattle.activiti.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.ibatis.mapping.FetchType;

import java.util.Date;

/**
 * 订单信息实体定义
 *
 * @author admin
 */
@Data
@ApiModel
@AllArgsConstructor
public class Order {

    @ApiModelProperty("编号")
    private Long id; // id long 主键
    @ApiModelProperty("订单号")
    private String orderNo; // 订单号 varchar2(100)

    private String customName; // 客户名称 varchar2(100)

    private String customCode; // 客户代码 varchar2(100)

    private Date delivery; // 订单交期 datetime

}
