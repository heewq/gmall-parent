package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.entity.BaseCategory2;
import com.atguigu.gmall.product.mapper.BaseCategory2Mapper;
import com.atguigu.gmall.product.service.BaseCategory2Service;
import com.atguigu.gmall.product.vo.CategoryTreeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【base_category2(二级分类表)】的数据库操作Service实现
 * @createDate 2022-11-29 22:22:53
 */
@Service
public class BaseCategory2ServiceImpl extends ServiceImpl<BaseCategory2Mapper, BaseCategory2>
        implements BaseCategory2Service {

    @Override
    public List<BaseCategory2> getCategory2ByC1Id(Long category1Id) {
        QueryWrapper<BaseCategory2> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category1_id", category1Id);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<CategoryTreeVo> getCategoryTree() {
        return baseMapper.getCategoryTree();
    }
}
