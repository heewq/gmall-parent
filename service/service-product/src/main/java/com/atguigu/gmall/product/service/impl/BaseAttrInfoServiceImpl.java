package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.entity.BaseAttrInfo;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【base_attr_info(属性表)】的数据库操作Service实现
 * @createDate 2022-11-29 22:22:53
 */
@Service
public class BaseAttrInfoServiceImpl extends ServiceImpl<BaseAttrInfoMapper, BaseAttrInfo>
        implements BaseAttrInfoService {

    @Override
    public List<BaseAttrInfo> getBaseAttrAndValue(Long category1Id,
                                                  Long category2Id,
                                                  Long category3Id) {
        return baseMapper.getBaseAttrAndValue(category1Id, category2Id, category3Id);
    }
}
