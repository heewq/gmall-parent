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
 * @TableName seckill_goods
 */
@TableName(value = "seckill_goods")
@Data
public class SeckillGoods implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * spu_id
     */
    private Long spuId;

    /**
     * sku_id
     */
    private Long skuId;

    /**
     * 标题
     */
    private String skuName;

    /**
     * 商品图片
     */
    private String skuDefaultImg;

    /**
     * 原价格
     */
    private BigDecimal price;

    /**
     * 秒杀价格
     */
    private BigDecimal costPrice;

    /**
     * 添加日期
     */
    private Date createTime;

    /**
     * 审核日期
     */
    private Date checkTime;

    /**
     * 审核状态
     */
    private String status;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 秒杀商品数
     */
    private Integer num;

    /**
     * 剩余库存数
     */
    private Integer stockCount;

    /**
     * 描述
     */
    private String skuDesc;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
