package com.smoex.service.user.common;

public class Constants {
    //cookie的有效期默认为30天
    public final static int COOKIE_MAX_AGE = 60*60*24*30;
    public final static int ACCOUNT_MAX_AGE = 60*60*24*30;
    public static final String ACCOUNT_KEY = ":account";
    public static final String REGISTER_KEY = ":register";
    public static final String LOGIN_KEY = ":login";
    public static final String SECURITY_KEY = ":security";
    public final static int SECURITY_MAX_AGE = 60*5;
    public final static int SECURITY_RESEND_AGE = 60*1000;
}
