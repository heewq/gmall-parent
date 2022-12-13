package com.atguigu.gmall.search;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

// Index = goods , Type = info  es 7.8.0 逐渐淡化type！  修改！
@Data
@Document(indexName = "goods", shards = 3, replicas = 2)
public class Goods {
    // 商品Id skuId
    @Id
    private Long id;

    //默认图片；  所有文本默认两个类型；
    // Keyword（不可分割：让es底层存储的时候）：不用分词
    // Text（可分词）
    // index=false: 不用为这个字段建立倒排索引
    @Field(type = FieldType.Keyword, index = false)
    private String defaultImg;

    //  es 中能分词的字段，这个字段数据类型必须是 text！keyword 不分词！
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title; //支持全文检索
    // es默认会把所有的字符串都当做是 Text；

    @Field(type = FieldType.Double)
    private Double price;

    //  @Field(type = FieldType.Date)   6.8.1
    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime; // 新品

    @Field(type = FieldType.Long)
    private Long tmId;

    @Field(type = FieldType.Keyword)
    private String tmName; //小米

    @Field(type = FieldType.Keyword)
    private String tmLogoUrl;
    //以上品牌信息

    @Field(type = FieldType.Long)
    private Long category1Id;

    @Field(type = FieldType.Keyword)
    private String category1Name;

    @Field(type = FieldType.Long)
    private Long category2Id;

    @Field(type = FieldType.Keyword)
    private String category2Name;

    @Field(type = FieldType.Long)
    private Long category3Id;

    @Field(type = FieldType.Keyword)  //游戏手机  拍照手机
    private String category3Name;
    //商品的精确分类信息


    //  商品的热度！ 我们将商品被用户点查看的次数越多，则说明热度就越高！
    @Field(type = FieldType.Long)
    private Long hotScore = 0L;

    // 平台属性集合对象
    // Nested 支持嵌套查询
    //如果文档中有数组或List类型的属性。而且需要进行检索就必须声明为 nested。并使用Nested进行检索
    @Field(type = FieldType.Nested)
    private List<SearchAttr> attrs;
}
