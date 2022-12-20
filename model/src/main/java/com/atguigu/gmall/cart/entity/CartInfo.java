package com.atguigu.gmall.cart.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 购物车中每个商品
 */
@Data
public class CartInfo implements Serializable {
    /**
     * skuid
     */
    private Long skuId;

    /**
     * 用户id
     */
    private String userId;


    /**
     * 商品第一次放入购物车时价格;
     */
    private BigDecimal cartPrice;

    /**
     * 商品的实时价格
     */
    private BigDecimal skuPrice;

    /**
     * 数量
     */
    private Integer skuNum;

    /**
     * 图片文件
     */
    private String imgUrl;

    /**
     * sku名称 (冗余)
     */
    private String skuName;


    private Integer isChecked;


    private Date createTime;


    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
