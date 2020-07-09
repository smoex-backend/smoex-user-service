package com.smoex.service.user.util;

import com.smoex.service.user.common.Constants;
import com.smoex.service.user.modal.TokenModal;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class TokenUtils {

    private static Logger log = LoggerFactory.getLogger(TokenUtils.class);
    private static final String SECURITY_KEY = "smoexsecurity";

    //该方法使用HS256算法和Secret:bankgl生成signKey
    private static Key getKeyInstance() {
        //We will sign our JavaWebToken with our ApiKey secret
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECURITY_KEY);//加密，里面的字符串可自行定义
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        return signingKey;
    }

    /**
     * 使用HS256签名算法和生成的signingKey最终的Token,claims中是有效载荷
     *
     * @param claims 待转化的数据
     * @return token字符串
     */
    public static String createToken(Map<String, Object> claims) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(nowMillis + 1000 * 60 * 60 * 24 * 7))//超时时间，设置为7天
                .setIssuedAt(now)
                .setNotBefore(now)
                .signWith(SignatureAlgorithm.HS256, getKeyInstance())
                .compact();
    }

    /**
     * 解析Token，同时也能验证Token，当验证失败返回null
     *
     * @param jwt token字符串
     * @return 解析的数据
     */
    public static Map<String, Object> parserToken(String jwt) {
        try {
            Map<String, Object> jwtClaims =
                    Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(jwt).getBody();
            return jwtClaims;
        } catch (Exception e) {
            log.error("json web token verify failed : " + e.getMessage());
            return null;
        }
    }

    public static TokenModal getTokenModal(HttpServletRequest request, String token) {
        String ip = request.getRemoteAddr();

        TokenModal tokenModal = new TokenModal(token);
        if (!tokenModal.verify(ip)) {
            throw new RuntimeException("非法请求2");
        }
        return tokenModal;
    }


    public static void setToken(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setDomain(".smoex.com");
        cookie.setHttpOnly(true);//如果设置了"HttpOnly"属性，那么通过程序(JS脚本、Applet等)将无法访问该Cookie
        cookie.setMaxAge(Constants.COOKIE_MAX_AGE);//设置生存期为1小时
        response.addCookie(cookie);
    }
    public static String createToken(int id, String uuid, String ip) {
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("id", id);
        tokenInfo.put("uuid", uuid);
        tokenInfo.put("ip", ip);
        return TokenUtils.createToken(tokenInfo);
    }
}