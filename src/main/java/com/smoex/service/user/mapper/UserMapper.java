package com.smoex.service.user.mapper;

import com.smoex.service.user.base.BaseMapper;
import com.smoex.service.user.base.BaseQuery;
import com.smoex.service.user.entity.UserEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<UserEntity> {

    UserEntity selectByWhere(BaseQuery query);
}
