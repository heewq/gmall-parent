package com.atguigu.gmall.search.repo;

import com.atguigu.gmall.search.bean.Person;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends CrudRepository<Person, Long> {

    List<Person> findAllByAgeGreaterThanEqual(Integer age);

    void deleteByAgeLessThan();

    long countByNameLike(String name);
}
