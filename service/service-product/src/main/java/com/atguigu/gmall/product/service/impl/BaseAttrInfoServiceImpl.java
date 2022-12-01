package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.entity.BaseAttrInfo;
import com.atguigu.gmall.product.entity.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.atguigu.gmall.product.service.BaseAttrValueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【base_attr_info(属性表)】的数据库操作Service实现
 * @createDate 2022-11-29 22:22:53
 */
@Service
public class BaseAttrInfoServiceImpl extends ServiceImpl<BaseAttrInfoMapper, BaseAttrInfo>
        implements BaseAttrInfoService {
    @Autowired
    private BaseAttrValueService baseAttrValueService;

    @Override
    public List<BaseAttrInfo> getBaseAttrAndValue(Long category1Id,
                                                  Long category2Id,
                                                  Long category3Id) {
        return baseMapper.getBaseAttrAndValue(category1Id, category2Id, category3Id);
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        // 把属性名保存到 base_attr_info
        save(baseAttrInfo);
        //    得到保存的id
        Long attrId = baseAttrInfo.getId();
        // 把属性值保存到 base_attr_value

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        attrValueList.forEach(baseAttrValue -> {
            baseAttrValue.setAttrId(attrId);
            baseAttrValueService.save(baseAttrValue);
        });
    }

    @Override
    public void updateAttrInfo(BaseAttrInfo baseAttrInfo) {
        // 修改 base_attr_info
        updateById(baseAttrInfo);

        // 修改 base_attr_value
        // 删除没有传递的值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        List<Long> ids = new ArrayList<>();
        attrValueList.forEach(baseAttrValue -> {
            if (baseAttrValue.getId() != null) ids.add(baseAttrValue.getId());
        });

        if (ids.size() > 0) {
            baseAttrValueService.lambdaUpdate()
                    .eq(BaseAttrValue::getAttrId, baseAttrInfo.getId())
                    .notIn(BaseAttrValue::getId, ids)
                    .remove();
        } else {
            baseAttrValueService.lambdaUpdate()
                    .eq(BaseAttrValue::getAttrId, baseAttrInfo.getId())
                    .remove();
        }


        // 新增/修改
        for (BaseAttrValue baseAttrValue : attrValueList) {
            if (baseAttrValue.getId() == null) {
                // 新增 未传递 attrId
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueService.save(baseAttrValue);
            } else {
                // 修改
                baseAttrValueService.updateById(baseAttrValue);
            }
        }
    }
}
