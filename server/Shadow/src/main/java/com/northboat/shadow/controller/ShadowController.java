package com.northboat.shadow.controller;

import com.northboat.shadow.service.AidesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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
//        System.out.println(account + ":" + command);
        Map<String, Object> result = new HashMap<>();
        try{
            String back = aidesService.sendCommandAndGetBack(account, command);
//            System.out.println(back);
            result.put("d", back);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.put("d", "取值错误");
            return result;
        }
    }

}
