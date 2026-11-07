package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.sky.constant.MessageConstant;
import com.sky.constant.URLConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.UserLoginVO;
import jdk.internal.net.http.common.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 微信用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        // 调用微信接口服务，获得用户的open id
        String openid = getResponse(userLoginDTO);

        // 判断open id是否为空
        if (openid == null || openid.isEmpty()) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 判断用户是否为新用户，如果是则创建新用户
        User user = userMapper.getByOpenid(openid);
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);

        return userLoginVO;
    }

    /**
     * 调用微信接口服务获取openid
     * @param userLoginDTO
     * @return
     */
    private String getResponse(UserLoginDTO userLoginDTO) {
        return getResponse(userLoginDTO.getCode());
    }

    private String getResponse(String code) {
        // 设置请求参数
        Map<String, String> params = new HashMap<>();
        params.put(URLConstant.GRANT_TYPE, URLConstant.AUTHORIZATION_CODE);
        params.put(URLConstant.JS_CODE, code);
        params.put(URLConstant.SECRET, weChatProperties.getSecret());
        params.put(URLConstant.APPID, weChatProperties.getAppid());

        // 设置请求URL
        String response = HttpClientUtil.doGet(URLConstant.WX_LOGIN, params);

        // json解析为对象，获取openid字段
        JSONObject jsonObject = JSON.parseObject(response);
        return jsonObject.getString(URLConstant.OPEN_ID);
    }
}
