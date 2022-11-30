package com.atguigu.gmall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品海报表
 *
 * @TableName spu_poster
 */
@TableName(value = "spu_poster")
@Data
public class SpuPoster implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 商品id
     */
    private Long spuId;
    /**
     * 文件名称
     */
    private String imgName;
    /**
     * 文件路径
     */
    private String imgUrl;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 逻辑删除 1（true）已删除， 0（false）未删除
     */
    private Integer isDeleted;
}
