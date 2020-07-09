package com.smoex.service.user.service;

import com.smoex.service.user.base.BaseService;
import com.smoex.service.user.entity.UserEntity;
import com.smoex.service.user.mapper.UserMapper;
import com.smoex.service.user.modal.AccountModal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService extends BaseService<UserEntity> {
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserMapper userMapper) {
        super(userMapper);
        this.userMapper = userMapper;
    }

    public UserEntity create(AccountModal accountModal) {
        return super.create(accountModal);
    }

    public UserEntity getByToken(String token) {
        AccountModal accountModal = new AccountModal();
        accountModal.setToken(token);
        return userMapper.selectByWhere(accountModal);
    }

    public UserEntity getByLogin(AccountModal accountModal) {
        return userMapper.selectByWhere(accountModal.getLoginEntity());
    }

    public UserEntity getByAccount(AccountModal accountModal) {
        return userMapper.selectByWhere(accountModal.getAccountEntity());
    }

}
