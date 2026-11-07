package com.sky.constant;

/**
 * 请求地址常量
 */
public class URLConstant {

    // 微信请求登录服务器接口
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    // 微信登录请求参数
    public static final String GRANT_TYPE = "grant_type";

    // 微信登录请求参数
    public static final String JS_CODE = "js_code";

    // 微信登录请求参数
    public static final String SECRET = "secret";

    // 微信登录请求参数
    public static final String APPID = "appid";

    // 微信登录请求GRANT_TYPE的值
    public static final String AUTHORIZATION_CODE = "authorization_code";

    // 微信返回的字段
    public static final String OPEN_ID = "openid";

}
