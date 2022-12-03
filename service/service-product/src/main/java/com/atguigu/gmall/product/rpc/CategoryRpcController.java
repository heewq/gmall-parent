package com.atguigu.gmall.product.rpc;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.BaseCategory2Service;
import com.atguigu.gmall.product.vo.CategoryTreeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inner/rpc/product")
public class CategoryRpcController {

    @Autowired
    private BaseCategory2Service categoryService;

    /**
     * 获取全部分类数据并组织成树形结构
     *
     * @return
     */
    @GetMapping("/category/tree")
    public Result<List<CategoryTreeVo>> getCategoryTree() {
        List<CategoryTreeVo> categoryTreeVos = categoryService.getCategoryTree();
        return Result.ok(categoryTreeVos);
    }
}
