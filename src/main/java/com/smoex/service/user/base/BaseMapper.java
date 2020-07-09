package com.smoex.service.user.base;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BaseMapper<T extends BaseEntity> {
    void insert(T t);

    int update(T t);

    int delete(int id);

    T selectById(int id);

    T selectByWhere(Map<String, Object> where);

    List<T> findByWhere(Map<String, Object> where);

    T selectByWhere(BaseQuery query);

    List<T> findByWhere(BaseQuery query);
}
