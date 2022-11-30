package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.entity.BaseCategory1;
import com.atguigu.gmall.product.mapper.BaseCategoryMapper;
import com.atguigu.gmall.product.service.BaseCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BaseCategoryServiceImpl extends ServiceImpl<BaseCategoryMapper, BaseCategory1>
        implements BaseCategoryService {

}
