package com.example.captcha.services;

import com.example.captcha.enums.RepCodeEnum;
import com.example.captcha.model.CaptchaVO;
import com.example.captcha.model.ResponseModel;
import com.example.captcha.util.ImageCreatorUtils;
import com.example.captcha.util.TextCreatorUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    private static final String CODE_PERFIX = "captcha";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public ResponseModel createCaptcha() {
        String token;
        String text = TextCreatorUtils.getText();

        boolean success = false;
        do {
            token = RandomStringUtils.randomAlphanumeric(16);
            String cacheKey = CODE_PERFIX + ":" + token;
            success = stringRedisTemplate.opsForValue().setIfAbsent(cacheKey, text, 180, TimeUnit.SECONDS);
        } while (!success);

        BufferedImage bi = ImageCreatorUtils.createImage(text);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "jpg", out);
        } catch (IOException e) {
            e.printStackTrace();
            // 处理异常，可能返回错误响应或重新尝试
        }

        // 使用java.util.Base64替换org.springframework.util.Base64Utils
        String base64Image = Base64.getEncoder().encodeToString(out.toByteArray());
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setToken(token);
        captchaVO.setBase64Image(base64Image);

        return ResponseModel.successData(captchaVO);
    }

    @Override
    public ResponseModel verifyCaptcha(CaptchaVO captchaVO) {
        if (StringUtils.isBlank(captchaVO.getToken()) || StringUtils.isBlank(captchaVO.getCaptchaCode())) {
            return ResponseModel.errorMsg(RepCodeEnum.NULL_ERROR);
        }

        String cacheKey = CODE_PERFIX + ":" + captchaVO.getToken();
        String expectedCode = stringRedisTemplate.opsForValue().get(cacheKey);

        if (expectedCode == null) {
            return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_INVALID);
        } else {
            stringRedisTemplate.delete(cacheKey);
        }

        if (expectedCode.equalsIgnoreCase(captchaVO.getCaptchaCode())) {
            return ResponseModel.success();
        }

        return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_COORDINATE_ERROR);
    }
}