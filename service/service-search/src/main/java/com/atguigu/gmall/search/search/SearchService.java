package com.atguigu.gmall.search.search;

import com.atguigu.gmall.search.Goods;
import com.atguigu.gmall.search.vo.SearchParamVo;
import com.atguigu.gmall.search.vo.SearchRespVo;

public interface SearchService {
    /**
     * 检索
     *
     * @param paramVo
     * @return
     */
    SearchRespVo search(SearchParamVo paramVo);

    /**
     * 上架 保存到 ElasticSearch
     *
     * @param goods
     */
    void onSale(Goods goods);

    /**
     * 下架 从 ElasticSearch 删除
     *
     * @param skuId
     */
    void cancelSale(Long skuId);


    /**
     * 更新热度分 hotScore
     *
     * @param skuId
     * @param score
     * @return
     */
    void updateHotScore(Long skuId, Long score);
}
