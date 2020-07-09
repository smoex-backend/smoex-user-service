package com.smoex.service.user.base;

import java.util.Map;

public abstract class BaseService<T extends BaseEntity> {

    private final BaseMapper<T> mapper;

    public BaseService(BaseMapper<T> mapper) {
        this.mapper = mapper;
    }

    public T getById(int id) {
        return mapper.selectById(id);
    }

    public T create(T t) {
        mapper.insert(t);
        return t;
    }

    public T update(T t) {
        mapper.update(t);
        t = getById(t.getId());
        return t;
    }

    public boolean exists(Map<String, Object> where) {
        return mapper.selectByWhere(where) != null;
    }

    protected BaseMapper<T> getMapper() {
        return mapper;
    }


}
