package com.northboat.shadow.controller;

import com.northboat.shadow.service.AidesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ShadowController {
    private AidesService aidesService;
    @Autowired
    public void setAidesService(AidesService aidesService){
        this.aidesService = aidesService;
    }

    @RequestMapping("/commanding")
    public Map<String, Object> commanding(@RequestBody Map<String, Object> params){
        //System.out.println(rabbitMQUtil.getClass());
        String account = (String) params.get("account");
        String command = (String) params.get("command");
        int timeout = Integer.parseInt((String) params.get("timeout"));
        Map<String, Object> result = new HashMap<>();
        List<String> back = aidesService.sendCommandAndGetBack(account, command, timeout);
        result.put("data", back);
        return result;
    }

    @RequestMapping("/clean")
    public Map<String, Object> clean(@RequestBody Map<String, Object> params){
        //System.out.println(rabbitMQUtil.getClass());
        String account = (String) params.get("account");
        Map<String, Object> result = new HashMap<>();
        String msg = aidesService.clearMsg(account);
        result.put("data", msg);
        return result;
    }

}
