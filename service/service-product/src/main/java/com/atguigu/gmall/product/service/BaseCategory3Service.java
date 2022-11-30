package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.entity.BaseCategory3;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【base_category3(三级分类表)】的数据库操作Service
 * @createDate 2022-11-29 22:22:53
 */
public interface BaseCategory3Service extends IService<BaseCategory3> {
    /**
     * 根据二级分类id查询对应的三级分类
     *
     * @param category2Id 二级分类id
     * @return
     */
    List<BaseCategory3> getCategory3ByC2Id(Long category2Id);
}
