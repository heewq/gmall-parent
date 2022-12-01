package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.entity.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.product.service.BaseAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【base_attr_value(属性值表)】的数据库操作Service实现
 * @createDate 2022-11-29 22:22:53
 */
@Service
public class BaseAttrValueServiceImpl extends ServiceImpl<BaseAttrValueMapper, BaseAttrValue>
        implements BaseAttrValueService {

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
//        QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("attr_id", attrId);
//        LambdaQueryWrapper<BaseAttrValue> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(BaseAttrValue::getAttrId, attrId);
        LambdaQueryWrapper<BaseAttrValue> queryWrapper
                = new LambdaQueryWrapper<BaseAttrValue>().eq(BaseAttrValue::getAttrId, attrId);
        return list(queryWrapper);
    }
}
