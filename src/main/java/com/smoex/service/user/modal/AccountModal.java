package com.smoex.service.user.modal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smoex.service.user.entity.UserEntity;
import org.springframework.util.StringUtils;

public class AccountModal extends UserEntity {

    // visitor, guest or member
    private String group;

    @JsonIgnore
    private boolean expired;

    public AccountModal() {};

    public AccountModal(UserEntity userEntity) {
        setUserEntity(userEntity);
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public void setUserEntity(UserEntity userEntity) {
        if (userEntity != null) {
            this.setId(userEntity.getId());
            this.setUsername(userEntity.getUsername());
            this.setNickname(userEntity.getNickname());
            this.setToken(userEntity.getToken());
            this.setEmail(userEntity.getEmail());
            this.setPhone(userEntity.getPhone());

            System.out.println(getNickname());
            if (!StringUtils.isEmpty(getPhone()) && !StringUtils.isEmpty(getNickname())) {
                this.setGroup("member");

            } else {
                this.setGroup("guest");
            }
        } else {
            this.setGroup("visitor");
        }
    }

    public void setSecurity(SecurityModel securityModel) {
        switch (securityModel.getType()) {
            case "phone":
                this.setPhone(securityModel.getTarget());
                break;
            case "email":
                this.setEmail(securityModel.getTarget());
                break;
        }
    }

    @JsonIgnore
    public UserEntity getAccountEntity() {
        UserEntity userEntity = new UserEntity();
        if (!StringUtils.isEmpty(getUsername())) {
            userEntity.setUsername(getUsername());
        } else if (!StringUtils.isEmpty(getPhone())) {
            userEntity.setPhone(getPhone());
        } else if (!StringUtils.isEmpty(getEmail())) {
            userEntity.setEmail(getEmail());
        } else {
            throw new RuntimeException("account params is not exist");
        }

        return userEntity;
    }

    @JsonIgnore
    public UserEntity getLoginEntity() {
        if (StringUtils.isEmpty(getPassword())) {
            throw new RuntimeException("account params is not exist");
        }
        UserEntity userEntity = getAccountEntity();
        userEntity.setPassword(getPassword());
        return userEntity;
    }
}
