package com.smoex.service.user.controller;

import com.smoex.service.user.common.Constants;
import com.smoex.service.user.entity.UserEntity;
import com.smoex.service.user.modal.AccountModal;
import com.smoex.service.user.modal.SecurityModel;
import com.smoex.service.user.modal.TokenModal;
import com.smoex.service.user.service.UserService;
import com.smoex.service.user.util.RedisUtils;
import com.smoex.service.user.util.SmsUtils;
import com.smoex.service.user.util.TokenUtils;
import com.smoex.service.user.util.ValidatorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    @Resource
    private RedisUtils cache;

    private final UserService userService;

    @Autowired
    public SecurityController(UserService userService) {
        this.userService = userService;
    }

    @ResponseBody
    @RequestMapping(value = "/sendcode", method = {RequestMethod.POST})
    public void sendCode(
            HttpServletRequest request,
            @RequestParam(value = "target") String target,
            @RequestParam(value = "scene") String scene,
            @CookieValue(name = "token", defaultValue = "") String token) {

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(scene)) {
            throw new RuntimeException("非法请求");
        }

        TokenModal tokenModal = TokenUtils.getTokenModal(request, token);

        String securityKey = tokenModal.getUuid() + Constants.SECURITY_KEY + ':' +scene;
        Object securityObj = cache.getNoCatch(securityKey);

        SecurityModel securityModel = new SecurityModel();

        if (securityObj != null) {
            long nowTime = Calendar.getInstance().getTimeInMillis();
            securityModel = (SecurityModel) securityObj;
            if (nowTime - securityModel.getTime() < Constants.SECURITY_RESEND_AGE) {
                throw new RuntimeException("请求太频繁");
            }
        }
        if (ValidatorUtils.isPhone(target)) {
            if (securityModel.getCode() == 0) {
                int code = (int) ((Math.random() * 9 + 1) * 100000);
                securityModel.setCode(code);
            }
            SmsUtils.send(target, securityModel.getCode());
            securityModel.setType("phone");
            System.out.println("security code:" + securityModel.getCode());
        } else {
            throw new RuntimeException("不正确的手机号");
        }

        long nowTime = Calendar.getInstance().getTimeInMillis();

        securityModel.setTarget(target);
        securityModel.setTime(nowTime);
        cache.set(securityKey, securityModel, Constants.SECURITY_MAX_AGE);
    }

    @ResponseBody
    @RequestMapping(value = "/verifycode", method = {RequestMethod.POST})
    public UserEntity verifyCode(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "code") Integer code,
            @RequestParam(value = "scene") String scene,
            @CookieValue(name = "token", defaultValue = "") String token) {

        if (StringUtils.isEmpty((token))) {
            throw new RuntimeException("非法请求");
        }

        if (!"register".equals(scene) && !"login".equals(scene)) {
            throw new RuntimeException("scene 错误");
        }


        TokenModal tokenModal = TokenUtils.getTokenModal(request, token);

        String securityKey = tokenModal.getUuid() + Constants.SECURITY_KEY + ':' +scene;
        Object securityObj = cache.getNoCatch(securityKey);

        if (securityObj == null) {
            throw new RuntimeException("验证码已失效");
        }

        SecurityModel securityModel = (SecurityModel) securityObj;

        if (securityModel.getCode() != code) {
            throw new RuntimeException("验证码不正确");
        }

        String accountKey = tokenModal.getUuid() + Constants.ACCOUNT_KEY;
        Object accountObj = cache.get(accountKey);
        if (accountObj == null) {
            throw new RuntimeException("非法请求2");
        }

        AccountModal accountModal = (AccountModal) accountObj;
        accountModal.setSecurity(securityModel);

        if ("login".equals(scene) || "register".equals(scene)) {
            UserEntity userEntity = userService.getByAccount(accountModal);
            if (userEntity == null) {
                userEntity = userService.create(accountModal);
            }
            accountModal.setExpired(true);
            accountModal.setUserEntity(userEntity);
        }

        if (accountModal.isExpired()) {
            cache.set(accountKey, accountModal);
        }

        // set cache
        cache.set(accountKey, accountModal);

        // set token
        token = TokenUtils.createToken(accountModal.getId(), tokenModal.getUuid(), tokenModal.getIp());
        TokenUtils.setToken(token, response);
        cache.del(securityKey);
        return accountModal;
    }
}
