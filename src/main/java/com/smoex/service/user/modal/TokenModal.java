package com.smoex.service.user.modal;

import com.smoex.service.user.util.TokenUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

public class TokenModal {
    private int id;
    private String uuid;
    private String ip;
    private boolean verified;

    public TokenModal(String token) {
        Map<String, Object> tokenInfo = TokenUtils.parserToken(token);
        if (tokenInfo == null) {
            this.verified = false;
            return;
        }
        if (StringUtils.isEmpty(tokenInfo.get("id")) ||
            StringUtils.isEmpty(tokenInfo.get("ip")) ||
            StringUtils.isEmpty(tokenInfo.get("uuid"))
        ) {
            this.verified = false;
            return;
        }
        this.id = (int) tokenInfo.get("id");
        this.ip = (String) tokenInfo.get("ip");
        this.uuid = (String) tokenInfo.get("uuid");
        this.verified = true;
    }

    public boolean verify(String ip) {
        return verified && this.ip.equals(ip);
    }
    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public String getUuid() {
        return uuid;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "id: " + id + "; uuid: " + uuid + "; ip: " + ip;
    }
}
