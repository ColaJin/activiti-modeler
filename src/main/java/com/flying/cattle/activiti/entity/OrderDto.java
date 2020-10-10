package com.flying.cattle.activiti.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 订单信息dto
 *
 * @author admin
 */
@Data
@ApiModel(value = "订单信息")
public class OrderDto {


    @ApiModelProperty(value = "id，主键")
    Long id;


    @ApiModelProperty(value = "订单号，")
    String orderNo;


    @ApiModelProperty(value = "客户名称，")
    String customName;


    @ApiModelProperty(value = "客户代码，")
    String customCode;


    @ApiModelProperty(value = "订单交期，格式：yyyy-MM-dd HH:mm:ss，")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    Date delivery;


    @ApiModelProperty(value = "产品类型id")
    Long productId;

    @ApiModelProperty(value = "产品类型名称")
    String productProductName;

    @ApiModelProperty(value = "组件数量，")
    Double assemblyNum;

    @ApiModelProperty(value = "电流分档配置id，")
    Long electricCfgId;

    @ApiModelProperty(value = "电流分档配置名称，")
    String electricCfgName;

    @ApiModelProperty(value = "电流IV字段，")
    String electricIvField;

    @ApiModelProperty(value = "电压分档配置id，")
    Long voltageCfgId;

    @ApiModelProperty(value = "电压分档配置名称，")
    String voltageCfgName;

    @ApiModelProperty(value = "电压IV字段，")
    String voltageIvField;

    @ApiModelProperty(value = "功率分档配置id，")
    Long powerCfgId;

    @ApiModelProperty(value = "功率分档配置名称，")
    String powerCfgName;

    @ApiModelProperty(value = "功率IV字段，")
    String powerIvField;

    @ApiModelProperty(value = "投入量，默认0")
    Double investment = 0d;


    @ApiModelProperty(value = "已排产数量，默认0")
    Double discharged = 0d;


    @ApiModelProperty(value = "待排产数量，默认组件数量")
    Double pending;


    @ApiModelProperty(value = "已产出数量，默认0")
    Double produced = 0d;


    @ApiModelProperty(value = "已产出功率，单位：W")
    Double producedPower = 0d;


    @ApiModelProperty(value = "还需生产数量，默认计划产量")
    Double expectant;


    @ApiModelProperty(value = "还需生产功率，单位：W")
    Double expectantPower;


    @ApiModelProperty(value = "已发货数量，默认0")
    Double deliver = 0d;


    @ApiModelProperty(value = "已发货功率，单位：W")
    Double deliverPower = 0d;


    @ApiModelProperty(value = "进度，单位：%")
    Double progress = 0d;

    @ApiModelProperty(value = "销售项目，")
    String saleProgram;

    @ApiModelProperty(value = "备注，")
    String remark;


    @ApiModelProperty(value = "用户id，")
    Long userId;


    @ApiModelProperty(value = "用户姓名，")
    String userName;


    @ApiModelProperty(value = "创建时间，格式：yyyy-MM-dd HH:mm:ss，")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    Date createTime;

    @ApiModelProperty(value = "订单类型，")
    private Integer orderType;

    @ApiModelProperty(value = "erp订单id，")
    private Long erpOrderId;

    @ApiModelProperty(value = "行号，")
    private Integer lineId;

    @ApiModelProperty(value = "产品编码，")
    private String productCode;

    @ApiModelProperty(value = "产品名称，")
    private String productName;

    @ApiModelProperty(value = "产品规格，")
    private String productSpecs;

    @ApiModelProperty(value = "税号，")
    private String dutyParagraph;

    @ApiModelProperty(value = "等级，")
    private String levelGrade; //等级

    @ApiModelProperty(value = "生产部门编码，")
    private String depCode;  //生产部门编码

    @ApiModelProperty(value = "生产部门名称，")
    private String depName; //生产部门名称
}