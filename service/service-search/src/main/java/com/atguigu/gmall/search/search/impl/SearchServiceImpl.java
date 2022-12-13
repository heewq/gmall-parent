package com.atguigu.gmall.search.search.impl;

import com.atguigu.gmall.search.Goods;
import com.atguigu.gmall.search.repo.GoodsRepository;
import com.atguigu.gmall.search.search.SearchService;
import com.atguigu.gmall.search.vo.SearchParamVo;
import com.atguigu.gmall.search.vo.SearchRespVo;
import com.atguigu.gmall.search.vo.SearchRespVo.OrderMap;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    private static final int pageSize = 10;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    /**
     * 根据请求参数构建查询条件
     *
     * @param paramVo
     * @return
     */
    private static Query buildQuery(SearchParamVo paramVo) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (paramVo.getCategory1Id() != null) {
            boolQuery.must(QueryBuilders.termQuery("category1Id", paramVo.getCategory1Id()));
        }
        if (paramVo.getCategory2Id() != null) {
            boolQuery.must(QueryBuilders.termQuery("category2Id", paramVo.getCategory2Id()));
        }
        if (paramVo.getCategory3Id() != null) {
            boolQuery.must(QueryBuilders.termQuery("category3Id", paramVo.getCategory3Id()));
        }
        // trademark
        if (!StringUtils.isEmpty(paramVo.getTrademark())) {
            String[] slice = paramVo.getTrademark().split(":");
            boolQuery.must(QueryBuilders.termQuery("tmId", slice[0]));
        }

        if (paramVo.getProps() != null && paramVo.getProps().length > 0) {
            Arrays.stream(paramVo.getProps())
                    .parallel()
                    .forEach(prop -> {
                        String[] slice = prop.split(":");
                        BoolQueryBuilder query = QueryBuilders.boolQuery();
                        query.must(QueryBuilders.termQuery("attrs.attrId", slice[0]));
                        query.must(QueryBuilders.termQuery("attrs.attrValue", slice[1]));
                        NestedQueryBuilder attrs = QueryBuilders.nestedQuery("attrs", query, ScoreMode.None);

                        boolQuery.must(attrs);
                    });
        }
        // keyword
        if (!StringUtils.isEmpty(paramVo.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("title", paramVo.getKeyword()));
        }

        NativeSearchQuery query = new NativeSearchQuery(boolQuery);

        // Sort: 1 hotScore  2 price
        // 1/2:asc/desc
        if (!StringUtils.isEmpty(paramVo.getOrder())) {
            String[] slice = paramVo.getOrder().split(":");
            Sort.Direction direction = "asc".equals(slice[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort;
            switch (slice[0]) {
                case "1":
                    sort = Sort.by(direction, "hotScore");
                    break;
                case "2":
                    sort = Sort.by(direction, "price");
                    break;
                default:
                    sort = Sort.by(Sort.Direction.DESC, "hotScore");
            }
            query.addSort(sort);
        }

        // 分页
        Pageable pageable = PageRequest.of(paramVo.getPageNo() - 1, pageSize);
        query.setPageable(pageable);

        // aggregation
        // by tmId
        TermsAggregationBuilder tmIdAgg = AggregationBuilders.terms("tmIdAgg").field("tmId").size(200);
        // subAggregation by tmName
        tmIdAgg.subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName").size(1));
        // subAggregation by tmLogoUrl
        tmIdAgg.subAggregation(AggregationBuilders.terms("tmLogoAgg").field("tmLogoUrl").size(1));
        query.addAggregation(tmIdAgg);

        // by attrAgg
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attrAgg", "attrs");
        // subAggregation by attrId
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").size(200);
        // subAggregation by attrName
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName").size(1));
        // subAggregation by attrValue
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue").size(100));
        attrAgg.subAggregation(attrIdAgg);
        query.addAggregation(attrAgg);

        // highlight
        if (!StringUtils.isEmpty(paramVo.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title")
                    .preTags("<span style='color:red'>")
                    .postTags("</span>");
            HighlightQuery highlightQuery = new HighlightQuery(highlightBuilder);

            query.setHighlightQuery(highlightQuery);
        }

        return query;
    }

    @Override
    public SearchRespVo search(SearchParamVo paramVo) {
        Query query = buildQuery(paramVo);
        // 查询数据
        SearchHits<Goods> result = restTemplate.search(query, Goods.class, IndexCoordinates.of("goods"));

        // 构建返回结果
        return buildSearchResp(result, paramVo);
    }

    /**
     * 根据查询结果构建返回数据
     *
     * @param hits
     * @param paramVo
     * @return
     */
    private SearchRespVo buildSearchResp(SearchHits<Goods> hits, SearchParamVo paramVo) {
        SearchRespVo searchRespVo = new SearchRespVo();
        // 检索参数
        searchRespVo.setSearchParam(paramVo);
        // 品牌 breadcrumb
        if (!StringUtils.isEmpty(paramVo.getTrademark())) {
            searchRespVo.setTrademarkParam("品牌: " + paramVo.getTrademark().split(":")[1]);
        }

        // 属性 breadcrumb
        if (paramVo.getProps() != null && paramVo.getProps().length > 0) {
            List<SearchRespVo.Props> propsList = Arrays.stream(paramVo.getProps()).map(propsParam -> {
                String[] slice = propsParam.split(":");
                SearchRespVo.Props props = new SearchRespVo.Props();
                props.setAttrName(slice[2]);
                props.setAttrValue(slice[1]);
                props.setAttrId(Long.valueOf(slice[0]));
                return props;
            }).collect(Collectors.toList());

            searchRespVo.setPropsParamList(propsList);
        }


        // 品牌列表 全部数据 聚合 aggregation
        ParsedLongTerms tmIdAgg = hits.getAggregations().get("tmIdAgg");
        List<SearchRespVo.Trademark> trademarks = tmIdAgg.getBuckets()
                .stream()
                .parallel()
                .map(bucket -> {
                    SearchRespVo.Trademark trademark = new SearchRespVo.Trademark();
                    trademark.setTmId(bucket.getKeyAsNumber().longValue());

                    ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
                    Terms.Bucket tmNameBucket = tmNameAgg.getBuckets().get(0);
                    trademark.setTmName(tmNameBucket.getKeyAsString());

                    ParsedStringTerms tmLogoAgg = bucket.getAggregations().get("tmLogoAgg");
                    Terms.Bucket tmLogoBucket = tmLogoAgg.getBuckets().get(0);
                    trademark.setTmLogoUrl(tmLogoBucket.getKeyAsString());

                    return trademark;
                }).collect(Collectors.toList());

        searchRespVo.setTrademarkList(trademarks);


        // 属性列表
        ParsedNested attrAgg = hits.getAggregations().get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<SearchRespVo.Attrs> attrsList = attrIdAgg.getBuckets()
                .stream()
                .map(bucket -> {
                    SearchRespVo.Attrs attrs = new SearchRespVo.Attrs();
                    attrs.setAttrId(bucket.getKeyAsNumber().longValue());

                    ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
                    Terms.Bucket attrNameAggBucket = attrNameAgg.getBuckets().get(0);
                    attrs.setAttrName(attrNameAggBucket.getKeyAsString());

                    ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
                    List<String> attrValue = attrValueAgg.getBuckets()
                            .stream()
                            .map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
                    attrs.setAttrValueList(attrValue);

                    return attrs;
                }).collect(Collectors.toList());

        searchRespVo.setAttrsList(attrsList);

        // url 参数
        String urlParam = buildUrlParam(paramVo);
        searchRespVo.setUrlParam(urlParam);

        // 排序信息
        if (!StringUtils.isEmpty(paramVo.getOrder())) {
            OrderMap orderMap = new OrderMap();
            String[] slice = paramVo.getOrder().split(":");
            orderMap.setType(slice[0]);
            orderMap.setSort(slice[1]);
            searchRespVo.setOrderMap(orderMap);
        }

        // 商品列表
        List<Goods> goodsList = hits.getSearchHits()
                .stream()
                .map(searchHit -> {
                    Goods goods = searchHit.getContent();
                    // 模糊检索高亮显示
                    if (!StringUtils.isEmpty(paramVo.getKeyword())) {
                        goods.setTitle(searchHit.getHighlightField("title").get(0));
                    }
                    return goods;
                }).collect(Collectors.toList());

        searchRespVo.setGoodsList(goodsList);

        // 页码
        searchRespVo.setPageNo(paramVo.getPageNo());

        // 总页码
        long totalHits = hits.getTotalHits();
        searchRespVo.setTotalPages(totalHits % pageSize == 0 ? totalHits / pageSize : totalHits / pageSize + 1);

        return searchRespVo;
    }

    /**
     * 根据查询参数构造请求url
     * // list.html?category3Id=61&trademark=2:华为&props=4:256GB:机身存储&props=3:12GB:运行内存
     *
     * @param paramVo
     * @return
     */
    private String buildUrlParam(SearchParamVo paramVo) {
        StringBuilder builder = new StringBuilder("list.html?");
        if (paramVo.getCategory1Id() != null) {
            builder.append("&category1Id=").append(paramVo.getCategory1Id());
        }
        if (paramVo.getCategory2Id() != null) {
            builder.append("&category2Id=").append(paramVo.getCategory2Id());
        }
        if (paramVo.getCategory3Id() != null) {
            builder.append("&category3Id=").append(paramVo.getCategory3Id());
        }
        if (!StringUtils.isEmpty(paramVo.getKeyword())) {
            builder.append("&keyword=").append(paramVo.getKeyword());
        }
        if (!StringUtils.isEmpty(paramVo.getTrademark())) {
            builder.append("&trademark=").append(paramVo.getTrademark());
        }
        if (paramVo.getProps() != null && paramVo.getProps().length > 0) {
            Arrays.stream(paramVo.getProps()).forEach(propParam -> builder.append("&props=").append(propParam));
        }
//        builder.append("&pageNo=").append(paramVo.getPageNo());

        return builder.toString();
    }

    @Override
    public void onSale(Goods goods) {
        goodsRepository.save(goods);
    }

    @Override
    public void cancelSale(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void updateHotScore(Long skuId, Long score) {
        Document document = Document.create();
        document.put("hotScore", score);
        UpdateQuery updateQuery = UpdateQuery.builder(skuId.toString())
                .withDocAsUpsert(true)
                .withDocument(document)
                .build();

        restTemplate.update(updateQuery, IndexCoordinates.of("goods"));
    }
}
