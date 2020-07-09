package com.smoex.service.user.controller;

import com.smoex.service.user.common.Constants;
import com.smoex.service.user.entity.UserEntity;
import com.smoex.service.user.modal.AccountModal;
import com.smoex.service.user.modal.TokenModal;
import com.smoex.service.user.service.UserService;
import com.smoex.service.user.util.RedisUtils;
import com.smoex.service.user.util.TextUtils;
import com.smoex.service.user.util.TokenUtils;
import com.smoex.service.user.util.ValidatorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Resource
    private RedisUtils cache;

    private final UserService userService;

    @Autowired
    public AccountController(UserService userService) {
        this.userService = userService;
    }


    @ResponseBody
    @RequestMapping(value = "/info", method = {RequestMethod.GET})
    public UserEntity getInfo(
            HttpServletResponse response,
            HttpServletRequest request,
            @CookieValue(name = "token", defaultValue = "") String token) {

        AccountModal accountModal = new AccountModal();
        String ip = request.getRemoteAddr();
        String accountKey;
        // 如果当前 TOKEN 为空，即用户第一次请求次接口
        if (StringUtils.isEmpty(token)) {
            // 根据信息创建一个 TOKEN
            String uuid = TextUtils.createUUID();
            // 游客的 ID 为 0
            token = TokenUtils.createToken(0, uuid, ip);
            accountModal.setUserEntity(null);
            TokenUtils.setToken(token, response);
            accountKey = uuid +  Constants.ACCOUNT_KEY;
            // 返回一个公用的游客账号，ID 为 0
        } else {
            // 判断 TOKEN 不为空的情况，即有调用过此接口
            TokenModal tokenModal = new TokenModal(token);
            System.out.println(tokenModal.toString());
            // 验证 TOKEN 中的信息是否正确, ID, UUID 都已经存在
            if (!tokenModal.verify(ip)) {
                throw new RuntimeException("登录已过期");
            }
            // 根据 uuid 获取 CACHE 中存储的信息
            accountKey = tokenModal.getUuid() + Constants.ACCOUNT_KEY;
            Object accountObj = cache.get(accountKey);
            System.out.println(accountObj);
            // 如果 id 为 0， 则是游客 ID
            if (tokenModal.getId() != 0) {
                // 如果缓存中拿不到当前用户的信息，则进入数据库中查找
                if (accountObj == null) {
                    UserEntity userEntity = userService.getById(tokenModal.getId());
                    accountModal.setUserEntity(userEntity);
                } else {
                    accountModal = (AccountModal) accountObj;
                    // 如果缓存中的信息已经过期，则进入数据库中查找
                    if (accountModal.isExpired()) {
                        UserEntity userEntity = userService.getById(accountModal.getId());
                        accountModal.setUserEntity(userEntity);
                    }
                }
            } else {
                accountModal.setUserEntity(null);
            }
        }
        if (!StringUtils.isEmpty(accountKey)) {
            cache.set(accountKey, accountModal);
        }
        return accountModal;
    }

    @ResponseBody
    @RequestMapping(value = "/register", method = {RequestMethod.POST})
    public UserEntity register(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password,
            @CookieValue(name = "token", defaultValue = "") String token) {
        if (StringUtils.isEmpty((token))) {
            throw new RuntimeException("非法请求");
        }

        String registerKey = token + Constants.REGISTER_KEY;
        Object obj = cache.getNoCatch(registerKey);

        if (obj == null) {
            throw new RuntimeException("验证已失效");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("test name");
        cache.del(registerKey);
        return userEntity;
    }

    @ResponseBody
    @RequestMapping(value = "/login", method = {RequestMethod.POST})
    public UserEntity login(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "account") String account,
            @RequestParam(value = "password") String password,
            @CookieValue(name = "token", defaultValue = "") String token) {

        if (StringUtils.isEmpty((token))) {
            throw new RuntimeException("非法请求");
        }

        TokenModal tokenModal = TokenUtils.getTokenModal(request, token);

        String accountKey = tokenModal.getUuid() + Constants.ACCOUNT_KEY;
        Object accountObj = cache.get(accountKey);

        if (accountObj == null) {
            accountObj =  new AccountModal();
        }
        AccountModal loginModel = new AccountModal();
        loginModel.setPassword(TextUtils.digestMd5(password));

        if (ValidatorUtils.isPhone(account)) {
            loginModel.setPhone(account);
        } else if (ValidatorUtils.isUsername(account)) {
            loginModel.setUsername(account);
        } else {
            throw new RuntimeException("不正确的 USERNAME");
        }

        UserEntity userEntity = userService.getByLogin(loginModel);

        if (userEntity == null) {
            throw new RuntimeException("username and password isn't correct");
        }

        // set cache
        AccountModal accountModal = (AccountModal) accountObj;
        accountModal.setUserEntity(userEntity);
        cache.set(accountKey, accountModal);

        // set token
        token = TokenUtils.createToken(accountModal.getId(), tokenModal.getUuid(), tokenModal.getIp());
        TokenUtils.setToken(token, response);
        return accountModal;
    }

    @ResponseBody
    @RequestMapping(value = "/logout", method = {RequestMethod.GET})
    public UserEntity logout(
            HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(name = "token", defaultValue = "") String token) {

        String ip = request.getRemoteAddr();
        TokenModal tokenModal = new TokenModal(token);
        if (!tokenModal.verify(ip)) {
            throw new RuntimeException("非法请求2");
        }

        String accountKey = tokenModal.getUuid() + Constants.ACCOUNT_KEY;
        // set cache
        AccountModal accountModal = new AccountModal();
        accountModal.setUserEntity(null);
        cache.set(accountKey, accountModal);

        // set token
        token = TokenUtils.createToken(accountModal.getId(), tokenModal.getUuid(), ip);
        TokenUtils.setToken(token, response);
        return accountModal;
    }
}
