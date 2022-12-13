package com.atguigu.gmall.search.repo;

import com.atguigu.gmall.search.Goods;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsRepository extends PagingAndSortingRepository<Goods, Long> {
}
