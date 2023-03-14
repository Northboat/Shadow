package com.northboat.shadow.service.impl;

import com.northboat.shadow.mapper.UserMapper;
import com.northboat.shadow.pojo.User;
import com.northboat.shadow.service.AidesService;
import com.northboat.shadow.utils.RabbitMQUtil;
import com.northboat.shadow.utils.RedisUtil;
import com.northboat.shadow.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    public List<String> sendCommandAndGetBack(String account, String command, int timeout){
        User user = StringUtil.containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
        List<String> res = new ArrayList<>();
        if(Objects.isNull(user)){
            res.add("请先注册");
            return res;
        }
        if(!rabbitMQUtil.send(user.getName(), command)){
            res.add("消息发送失败");
            return res;
        }
        long begin = System.currentTimeMillis();
        int size = Objects.isNull(redisUtil.lget(user.getName())) ? 0 : redisUtil.lget(user.getName()).size();
        while(Objects.isNull(redisUtil.lget(user.getName())) || redisUtil.lget(user.getName()).size() == size){
            long cur = System.currentTimeMillis();
            long used = (cur-begin) / 1000;
            if(used > timeout){
                res.add("连接超时");
                return res;
            }
        }
        List l = redisUtil.lget(user.getName());
        for (Object o : l) {
            String s = (String) o;
            res.add(s);
        }
        redisUtil.ldel(user.getName());
        return res;
    }

    @Override
    public String login(String account, String password){
        User user = StringUtil.containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
        redisUtil.ldel(user.getName());
        if(!rabbitMQUtil.send(user.getName(), "/login " + password)){
            return "fail";
        }
        long begin = System.currentTimeMillis();
        while(Objects.isNull(redisUtil.lget(user.getName())) || redisUtil.lget(user.getName()).size() == 0){
            long cur = System.currentTimeMillis();
            long used = (cur-begin) / 1000;
            if(used > 9){
                return "timeout";
            }
        }
        String res = (String) redisUtil.lpop(user.getName());
//        System.out.println("nmsl");
//        System.out.println(res);
        return res;
    }

    @Override
    public boolean clearMsg(String account){
        User user = StringUtil.containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
        return rabbitMQUtil.clear(user.getName());
    }
}
