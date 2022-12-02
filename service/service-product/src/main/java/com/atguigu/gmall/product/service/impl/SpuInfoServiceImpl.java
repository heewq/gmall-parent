package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.entity.SpuImage;
import com.atguigu.gmall.product.entity.SpuInfo;
import com.atguigu.gmall.product.entity.SpuSaleAttr;
import com.atguigu.gmall.product.entity.SpuSaleAttrValue;
import com.atguigu.gmall.product.mapper.SpuInfoMapper;
import com.atguigu.gmall.product.service.SpuImageService;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.atguigu.gmall.product.service.SpuSaleAttrValueService;
import com.atguigu.gmall.product.vo.SpuSaveInfoVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vcwfhe
 * @description 针对表【spu_info(商品表)】的数据库操作Service实现
 * @createDate 2022-11-29 22:22:53
 */
@Service
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoMapper, SpuInfo>
        implements SpuInfoService {
    @Autowired
    private SpuImageService spuImageService;
    @Autowired
    private SpuSaleAttrService spuSaleAttrService;
    @Autowired
    private SpuSaleAttrValueService spuSaleAttrValueService;

    @Override
    public void saveSpuInfo(SpuSaveInfoVo spuSaveInfoVo) {
        // 保存spu_info表
        SpuInfo spuInfo = new SpuInfo();
        BeanUtils.copyProperties(spuSaveInfoVo, spuInfo);
        this.save(spuInfo);

        // 保存spu_image表
        List<SpuImage> spuImageList = spuSaveInfoVo.getSpuImageList()
                .stream()
                .map(item -> {
                    SpuImage spuImage = new SpuImage();
                    BeanUtils.copyProperties(item, spuImage);
                    spuImage.setSpuId(spuInfo.getId());
                    return spuImage;
                }).collect(Collectors.toList());
        spuImageService.saveBatch(spuImageList);

        // 保存spu_sale_attr表
        List<SpuSaleAttr> spuSaleAttrList = spuSaveInfoVo.getSpuSaleAttrList()
                .stream()
                .map(item -> {
                    SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
                    BeanUtils.copyProperties(item, spuSaleAttr);
                    spuSaleAttr.setSpuId(spuInfo.getId());
                    return spuSaleAttr;
                }).collect(Collectors.toList());
        spuSaleAttrService.saveBatch(spuSaleAttrList);

        // 保存spu_sale_attr_value
        List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaveInfoVo.getSpuSaleAttrList()
                .stream()
                .flatMap(item -> item.getSpuSaleAttrValueList()
                        .stream()
                        .map(val -> {
                            SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
                            BeanUtils.copyProperties(val, spuSaleAttrValue);
                            spuSaleAttrValue.setSpuId(spuInfo.getId());
                            spuSaleAttrValue.setSaleAttrName(item.getSaleAttrName());
                            return spuSaleAttrValue;
                        })).collect(Collectors.toList());
        spuSaleAttrValueService.saveBatch(spuSaleAttrValueList);
    }
}
