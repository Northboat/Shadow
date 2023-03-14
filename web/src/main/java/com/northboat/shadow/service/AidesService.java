package com.northboat.shadow.service;

import java.util.List;

public interface AidesService {
    public List<String> sendCommandAndGetBack(String account, String command, int timeout);

    public String login(String name, String password);

    public boolean clearMsg(String account);
}
