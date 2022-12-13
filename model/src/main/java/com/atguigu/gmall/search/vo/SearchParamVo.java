package com.atguigu.gmall.search.vo;

import lombok.Data;

/**
 * 封装检索用的所有参数
 */
@Data
public class SearchParamVo {
    // 分类参数
    Long category1Id;
    Long category2Id;
    Long category3Id;

    // 关键字检索
    String keyword;

    // 品牌检索
    String trademark;

    // 平台属性检索
    String[] props;

    // 排序和分页
    String order = "1:desc";
    Integer pageNo = 1;
}
