package com.example.captcha.services;

import com.example.captcha.model.CaptchaVO;
import com.example.captcha.model.ResponseModel;

/**
 * 功能：
 * 作者：兮和
 * 时间：2024/5/4 17:57
 */
public interface CaptchaService {

    /**
     * 创建验证码
     * @return 创建信息
     */
    ResponseModel createCaptcha();

    /**
     * 验证用户输入的验证码
     * @param captchaVO 认证token,用户输入的验证码captchaWord
     * @return 验证结果
     */
    ResponseModel verifyCaptcha(CaptchaVO captchaVO);
}
