package com.northboat.shadow.service.impl;

import com.northboat.shadow.mapper.UserMapper;
import com.northboat.shadow.pojo.User;
import com.northboat.shadow.service.AidesService;
import com.northboat.shadow.utils.RabbitMQUtil;
import com.northboat.shadow.utils.RedisUtil;
import com.northboat.shadow.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
public class AidesServiceImpl implements AidesService {

    private UserMapper userMapper;
    @Autowired
    public void setUserMapper(UserMapper userMapper){
        this.userMapper = userMapper;
    }

    private RabbitMQUtil rabbitMQUtil;
    @Autowired
    public void setRabbitMQUtil(RabbitMQUtil rabbitMQUtil){
        this.rabbitMQUtil = rabbitMQUtil;
    }

    private RedisUtil redisUtil;
    @Autowired
    public void setRedisUtil(RedisUtil redisUtil){
        this.redisUtil = redisUtil;
    }

    @Override
    public String sendCommandAndGetBack(String account, String command){
        User user = StringUtil.containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
        if(Objects.isNull(user)){
            return "请先注册";
        }
        if(!rabbitMQUtil.send(user.getName(), command)){
            return "消息发送失败";
        }
        long begin = System.currentTimeMillis();
//        System.out.println(user.getName());
        while(Objects.isNull(redisUtil.get(user.getName())) || redisUtil.get(user.getName()).equals("null")){
            long cur = System.currentTimeMillis();
            long used = (cur-begin) / 1000;
            if(used > 4){
                return "连接超时";
            }
        }
        String back = (String) redisUtil.get(user.getName());
        redisUtil.set(user.getName(), "null");
        return back;
    }
}
