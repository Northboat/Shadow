package com.northboat.shadow.service.impl;

import com.northboat.shadow.mapper.UserMapper;
import com.northboat.shadow.pojo.User;
import com.northboat.shadow.service.UserService;
import com.northboat.shadow.utils.MailUtil;
import com.northboat.shadow.utils.RedisUtil;
import com.northboat.shadow.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {


    private UserMapper userMapper;
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    private RedisUtil redisUtil;
    @Autowired
    public void setRedisUtil(RedisUtil redisUtil){
        this.redisUtil = redisUtil;
    }

    private MailUtil mailUtil;
    @Autowired
    public void setMailUtil(MailUtil mailUtil){
        this.mailUtil = mailUtil;
    }


    @Override
    public int login(String name){
        userMapper.login(name);
        return userMapper.queryByName(name).getOnline();
    }


    @Override
    public int send(String account){
        // 把用户查出来，通过@判断传入的是昵称还是邮箱
        User user = StringUtil.containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
        if(Objects.isNull(user) && !StringUtil.containAt(account)){
            return 0;
        }
        if(Objects.isNull(user)){
            String code;
            try{
                code = mailUtil.send(account);
            }catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
            redisUtil.set(account, code, 600);
            return 1;
        }

        String code;
        try{
            code = mailUtil.send(user.getEmail(), user.getName());
        }catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        // 存验证码用邮箱存
        redisUtil.set(user.getEmail(), code, 600);
        return 2;
    }

    // 验证码验证
    @Override
    public int verily(String account, String code){
        User user = StringUtil.containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
        String c = (String) redisUtil.get(user.getEmail());
        if(c.equals(code)){
            String name = user.getName();
            // 在线状态
            userMapper.login(name);
            redisUtil.del(user.getEmail());
            return 1;
        }
        return -1;
    }

    // 注册所用帐号就是邮箱
    @Override
    public int register(String email, String code, String name) {
        String c = (String) redisUtil.get(email);
        // 2表示验证失败
        if(!c.equals(code)){
            return 2;
        }
        // 3表示昵称不合法或重复
        if(!nameValid(name)){
            return 3;
        }
        // 否则验证成功，注册成功，录入数据库，返回1
        redisUtil.del(email);
        User user = new User(name, email, 1);
        userMapper.add(user);
        return 1;
    }

    @Override
    public boolean nameValid(String name){
        for(char c: name.toCharArray()){
            if(c == '@'){
                return false;
            }
        }
        for(User user: userMapper.queryAll()){
            if(user.getName().equals(name)){
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean logout(String account){
        User user = StringUtil.containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
        String name = user.getName();
        try{
            userMapper.logoff(name);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User getUser(String account){
        return StringUtil.containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
    }
}
