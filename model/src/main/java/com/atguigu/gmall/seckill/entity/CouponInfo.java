package com.atguigu.gmall.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券表
 *
 * @TableName coupon_info
 */
@TableName(value = "coupon_info")
@Data
public class CouponInfo implements Serializable {
    /**
     * 购物券编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 购物券名称
     */
    private String couponName;

    /**
     * 购物券类型 1 现金券 2 折扣券 3 满减券 4 满件打折券
     */
    private String couponType;

    /**
     * 满额数（3）
     */
    private BigDecimal conditionAmount;

    /**
     * 满件数（4）
     */
    private Long conditionNum;

    /**
     * 活动Id
     */
    private Long activityId;

    /**
     * 减金额（1 3）
     */
    private BigDecimal benefitAmount;

    /**
     * 折扣（2 4）
     */
    private BigDecimal benefitDiscount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 范围类型 1、商品(spuid) 2、品类(三级分类id) 3、品牌(tmid)
     */
    private String rangeType;

    /**
     * 最多领用次数
     */
    private Integer limitNum;

    /**
     * 已领用次数
     */
    private Integer takenCount;

    /**
     * 可以领取的开始日期
     */
    private Date startTime;

    /**
     * 可以领取的结束日期
     */
    private Date endTime;

    /**
     * 修改时间
     */
    private Date operateTime;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 范围描述
     */
    private String rangeDesc;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
