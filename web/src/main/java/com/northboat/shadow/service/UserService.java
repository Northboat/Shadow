package com.northboat.shadow.service;

import com.northboat.shadow.pojo.User;

public interface UserService {

    public int register(String email, String code, String name);
    public int send(String email);
    public int verily(String account, String code);
    public boolean nameValid(String name);
    public boolean logout(String user);
    public User getUser(String account);
    public int login(String name);

}
