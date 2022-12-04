package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.entity.BaseCategory2;
import com.atguigu.gmall.product.vo.CategoryTreeVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【base_category2(二级分类表)】的数据库操作Service
 * @createDate 2022-11-29 22:22:53
 */
public interface BaseCategory2Service extends IService<BaseCategory2> {

    /**
     * 根据一级分类id查询对应的二级分类
     *
     * @param category1Id 一级分类id
     * @return
     */
    List<BaseCategory2> getCategory2ByC1Id(Long category1Id);

    List<CategoryTreeVo> getCategoryTree();

    CategoryTreeVo getCategoryTreeWithC3Id(Long c3Id);
}
