package com.atguigu.gmall.product.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CategoryTreeVo {
    private Long categoryId;
    private String categoryName;
    private List<CategoryTreeVo> categoryChild;
}
