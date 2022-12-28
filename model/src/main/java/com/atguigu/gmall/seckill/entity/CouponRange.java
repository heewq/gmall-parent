package com.atguigu.gmall.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券范围表
 *
 * @TableName coupon_range
 */
@TableName(value = "coupon_range")
@Data
public class CouponRange implements Serializable {
    /**
     * 主键编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 购物券编号
     */
    private Long couponId;

    /**
     * 范围类型 1、商品(spuid) 2、品类(三级分类id) 3、品牌(tmid)
     */
    private String rangeType;

    /**
     * 范围类型对应的Id
     */
    private Long rangeId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
