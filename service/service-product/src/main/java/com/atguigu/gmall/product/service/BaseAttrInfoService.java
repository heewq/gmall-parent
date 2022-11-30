package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.entity.BaseAttrInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【base_attr_info(属性表)】的数据库操作Service
 * @createDate 2022-11-29 22:22:53
 */
public interface BaseAttrInfoService extends IService<BaseAttrInfo> {
    /**
     * 根据分类id获取平台属性
     *
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> getBaseAttrAndValue(Long category1Id, Long category2Id, Long category3Id);
}
