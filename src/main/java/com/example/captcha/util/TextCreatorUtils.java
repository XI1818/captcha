package com.example.captcha.util;

import java.util.Random;

/**
 * 功能：验证码文本生成类
 * 作者：兮和
 * 时间：2024/5/4 17:49
 */
public class TextCreatorUtils {
    /**
     * 生成验证码所需字符，除去容易识别的字符
     */
    private static char[] charSequence = {
            'A', 'B', 'D', 'E', 'F', 'G',
            'H', 'J', 'K', 'P', 'Q', 'R',
            'T', 'Y', 'Z', '2', '3', '4',
            '5', '6', '7', '8', '9', 'a',
            'b', 'c', 'd', 'e', 'f', 'y',
            'n', 'm', 'n', 'p', 'w', 'x'
    };
    /**
     * 根据系统时间创建随机数
     */
    private static Random random = new Random(System.currentTimeMillis());
    /**
     * 验证码长度
     */
    private static final int LENGTH = 5;

    private TextCreatorUtils(){

    }

    /**
     * 产生验证码文本
     * @return 验证码字符串
     */
    public static String getText() {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < LENGTH; ++i) {
            text.append(charSequence[random.nextInt(charSequence.length)]);
        }
        return text.toString();
    }
}
