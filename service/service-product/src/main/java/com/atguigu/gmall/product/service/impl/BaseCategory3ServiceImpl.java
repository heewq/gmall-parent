package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.entity.BaseCategory3;
import com.atguigu.gmall.product.mapper.BaseCategory3Mapper;
import com.atguigu.gmall.product.service.BaseCategory3Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【base_category3(三级分类表)】的数据库操作Service实现
 * @createDate 2022-11-29 22:22:53
 */
@Service
public class BaseCategory3ServiceImpl extends ServiceImpl<BaseCategory3Mapper, BaseCategory3>
        implements BaseCategory3Service {
    @Override
    public List<BaseCategory3> getCategory3ByC2Id(Long category2Id) {
        QueryWrapper<BaseCategory3> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category2_id", category2Id);
        return baseMapper.selectList(queryWrapper);
    }
}
