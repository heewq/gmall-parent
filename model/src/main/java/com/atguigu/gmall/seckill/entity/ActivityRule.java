package com.atguigu.gmall.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * 优惠规则
 *
 * @TableName activity_rule
 */
@TableName(value = "activity_rule")
@Data
public class ActivityRule implements Serializable {
    /**
     * 主键编号
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 活动Id
     */
    private Integer activityId;

    /**
     * 满减金额
     */
    private BigDecimal conditionAmount;

    /**
     * 满减件数
     */
    private Long conditionNum;

    /**
     * 优惠金额
     */
    private BigDecimal benefitAmount;

    /**
     * 优惠折扣
     */
    private BigDecimal benefitDiscount;

    /**
     * 优惠级别
     */
    private Long benefitLevel;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
