package com.northboat.shadow.controller;

import com.northboat.shadow.pojo.User;
import com.northboat.shadow.service.UserService;
import com.northboat.shadow.websocket.WebSocketServer;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;


@Controller
public class ChatController {

    private UserService userService;
    @Autowired
    public void setUserService(UserService userService){
        this.userService = userService;
    }

    // 加入公共聊天室
    @RequestMapping("/channel")
    public String channel(HttpSession session, Model model){
        int count = WebSocketServer.getOnlineCount() + 1;
//        System.out.println(WebSocketServer.getWebSocketSet().size());
        model.addAttribute("count", count);
        return "chat/channel";
    }

    // 首先要验证是否登录，再验证是否重复登录
    @RequestMapping("/aides")
    public String aides(HttpSession session, Model model){
        // 去除注释开启登录验证
        String account = (String) session.getAttribute("user");
        Integer login = (Integer) session.getAttribute("login");
        if(Objects.isNull(account) || login == 0){
            model.addAttribute("msg", "请先登录");
            return "user/login";
        }
        User user = userService.getUser(account);
        if(Objects.isNull(user)){
            model.addAttribute("msg", "请先注册");
            return "user/login";
        }
        if(user.getOnline() != login){
            model.addAttribute("msg", "帐号已在其他地方登录，请重新登陆");
            session.removeAttribute("user");
            session.removeAttribute("login");
            return "user/login";
        }

        return "chat/aides";
    }

}
