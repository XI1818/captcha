package com.example.captcha.controller;

import com.example.captcha.model.CaptchaVO;
import com.example.captcha.model.ResponseModel;
import com.example.captcha.services.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 验证码控制器，提供验证码生成和验证的接口。
 */
@CrossOrigin
@RestController
public class CaptchaController {

    private final CaptchaService captchaService;

    @Autowired
    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * 生成新的验证码。
     *
     * @return 包含验证码信息的响应模型。
     */
    @GetMapping("/createcaptcha")
    public ResponseModel generateCaptcha() {
        return captchaService.createCaptcha();
    }

    /**
     * 验证用户输入的验证码。
     *
     * @param captchaVO 用户提供的验证码数据。
     * @return 表示验证结果的响应模型。
     */
    @PostMapping("/verify")
    public ResponseModel checkCaptcha(CaptchaVO captchaVO) {
        return captchaService.verifyCaptcha(captchaVO);
    }

}