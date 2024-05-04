package com.example.captcha.model;

import java.io.Serializable;

/**
 * 功能：CaptchaVO
 * 作者：兮和
 * 时间： 2024/5/4 17:52
 */
public class CaptchaVO implements Serializable {

    /**
     * 验证码base64图片
     */
    private String base64Image;

    /**
     * 认证
     */
    private String token;

    /**
     * 验证码文本
     */
    private String captchaCode;

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }
}

