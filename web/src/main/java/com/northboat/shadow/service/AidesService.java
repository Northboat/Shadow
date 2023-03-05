package com.northboat.shadow.service;

public interface AidesService {
    public String sendCommandAndGetBack(String account, String command);

    public String login(String name, String password);

    public boolean clearMsg(String account);
}
