package io.github.myacelw.mybatis.dynamic.core.service.impl;

import io.github.myacelw.mybatis.dynamic.core.typehandler.AbstractSecretTypeHandler;

import java.util.Base64;

public class MySecretTypeHandler extends AbstractSecretTypeHandler {

    public final static String PREFIX = "SECRET: ";

    /**
     * 加密
     */
    protected String encrypt(String text) {
        if (text == null) {
            return null;
        }
        // 对文本进行Base64编码
        String base64Encoded = Base64.getEncoder().encodeToString(text.getBytes());
        // 添加前缀
        return PREFIX + base64Encoded;
    }

    /**
     * 解密
     */
    protected String decrypt(String text) {
        if (text == null) {
            return null;
        }
        return new String(Base64.getDecoder().decode(text.substring(PREFIX.length())));
    }

}
