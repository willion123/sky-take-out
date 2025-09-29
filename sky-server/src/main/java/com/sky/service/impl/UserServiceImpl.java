package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;
    /**
     * 微信登录
     * 通过微信接口验证用户登录凭证，并在系统中创建或更新用户信息
     * @param userLoginDTO 用户登录信息，包含微信登录凭证code
     * @return 登录用户信息
     */
    public User wxLogin(UserLoginDTO userLoginDTO) {

        String openid = getOpenid(userLoginDTO.getCode());

        //如果openid为空，说明微信登录验证失败，抛出登录异常
        if (openid ==  null){
            throw  new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //根据openid查询用户信息，如果用户不存在则创建新用户
        User user = userMapper.getByOpenid(openid);

        if (user ==  null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        return user;
    }

    /**
     * 调用微信接口服务，获取openid
     * @param code 微信登录成功获取的临时登录凭证
     * @return 微信用户的openid
     */
    private String getOpenid(String code) {
        //调用微信接口服务，获取微信用户唯一标识openid
        Map<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN_URL, map);
        
        //解析微信接口返回结果，获取openid
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }


}