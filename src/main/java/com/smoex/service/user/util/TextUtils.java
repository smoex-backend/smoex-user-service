package com.smoex.service.user.util;

import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class TextUtils {
    public static String digestMd5(String str) {
        try {
            return DigestUtils.md5DigestAsHex(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("字符串加密错误");
        }
    }

    public static String createUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
