package com.example.captcha.enums;

import com.example.captcha.model.ResponseModel;

import java.text.MessageFormat;

/**
 * 功能：
 * 作者：兮和
 * 时间：2024/5/4 17:56
 */
public enum RepCodeEnum {
    SUCCESS("0000","成功"),
    ERROR("0001","操作失败"),
    EXCEPTION("9999","服务器内部异常"),

    NULL_ERROR("0011","{0}不能为空"),

    API_CAPTCHA_INVALID("6110","验证码已失效，请重新获取"),
    API_CAPTCHA_COORDINATE_ERROR("6111","验证码失败"),
    API_CAPTCHA_ERROR("6112","获取验证码失败");

    private String code;
    private String desc;

    RepCodeEnum(String code, String desc){
        this.code=code;
        this.desc=desc;
    }

    public String getCode(){
        return code;
    }

    public String getDesc(){
        return desc;
    }

    public ResponseModel parseError(Object... fieldNames){
        ResponseModel errorMessage=new ResponseModel();
        String newDesc= MessageFormat.format(this.desc,fieldNames);

        errorMessage.setRepCode(this.code);
        errorMessage.setRepMsg(newDesc);
        return errorMessage;
    }

}