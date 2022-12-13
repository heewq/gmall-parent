package com.atguigu.gmall.search.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "person") // 文档存到person索引
public class Person {
    @Id //主键
    private Long id;
    @Field(value = "name", type = FieldType.Text) // 文本字段可以全文检索
    private String name;
    @Field(value = "age", type = FieldType.Integer)
    private Integer age;
}
