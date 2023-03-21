package com.northboat.shadow.controller;

import com.northboat.shadow.pojo.User;
import com.northboat.shadow.service.AidesService;
import com.northboat.shadow.service.impl.AidesServiceImpl;
import com.northboat.shadow.service.impl.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@Controller
public class UserController {

    private UserServiceImpl userService;
    @Autowired
    public void setUserService(UserServiceImpl userService){
        this.userService = userService;
    }

    private AidesService aidesService;
    @Autowired
    public void setAidesService(AidesServiceImpl aidesService){
        this.aidesService = aidesService;
    }

    @RequestMapping("/sign")
    public String sign(){
        return "user/sign";
    }

    @RequestMapping("/signIn")
    public String singIn(HttpSession session, Model model,
                         @RequestParam("account") String account, @RequestParam("pwd") String pwd){
        //System.out.println("进来了");
        User user = userService.getUser(account);
        if(Objects.isNull(user)){
            model.addAttribute("msg", "用户不存在");
            return "user/sign";
        }
        //System.out.println(account + ":" + pwd);
        try{
            String back = aidesService.login(user.getName(), pwd);
            switch (back) {
                case "yes" -> {
                    //System.out.println(account + "登录成功");
                    int online = userService.login(user.getName());
                    // 登录成功
                    session.setAttribute("login", online);
                    session.setAttribute("user", user.getName());
                    model.addAttribute("login", online);
                    model.addAttribute("user", user.getName());
                    return "user/login";
                }
                case "no" -> model.addAttribute("msg", "密码错误");
                case "fail" -> model.addAttribute("msg", "消息发送失败");
                case "timeout" -> model.addAttribute("msg", "反馈超时");
            }
        }catch (Exception e){
            e.printStackTrace();
            model.addAttribute("msg", "消息传输错误");
        }
        return "user/sign";
    }

    @RequestMapping("/login")
    public String login(HttpSession session, Model model){
        Integer login = (Integer) session.getAttribute("login");
        if(!Objects.isNull(login)){
            String user = (String) session.getAttribute("user");
            model.addAttribute("login", login);
            model.addAttribute("user", user);
        }
        return "user/login";
    }

    // 发送邮件
    @RequestMapping("/send")
    public String send(Model model, HttpSession session, @RequestParam("account") String account){
        int status = userService.send(account);
        System.out.println(status);
        if(status == 0){
            model.addAttribute("msg", "昵称错误或者请输入正确的邮箱");
            return "user/login";
        }
        if(status == -1){
            model.addAttribute("msg", "验证码发送失败");
            return "user/login";
        }
        session.setAttribute("user", account);
        if(status == 1){
            return "user/register";
        }
        return "user/verify";
    }

    // 已注册，登录验证
    @RequestMapping("/verify")
    public String verify(Model model, HttpSession session, @RequestParam("code") String code){
        String account = (String) session.getAttribute("user");
        if(Objects.isNull(account)){
            model.addAttribute("msg", "请先获取验证码");
            return "user/login";
        }
        int online = userService.verily(account, code);
        if(online == -1){
            model.addAttribute("msg", "验证失败");
            return "user/verify";
        }
        System.out.println(account + "登录成功");
        User user = userService.getUser(account);
        // 登录成功
        session.setAttribute("login", user.getOnline());
        session.setAttribute("user", user.getName());
        model.addAttribute("login", user.getOnline());
        model.addAttribute("user", user.getName());
        return "user/login";
    }

    // 已注册，登录验证
    @RequestMapping("/register")
    public String register(Model model, HttpSession session, @RequestParam("code") String code,
                           @RequestParam("name") String name){
        String email = (String) session.getAttribute("user");
        if(Objects.isNull(email)){
            model.addAttribute("msg", "请先获取验证码");
            return "user/login";
        }
        int flag = userService.register(email, code, name);
        if(flag == 2){
            model.addAttribute("msg", "验证码错误");
            return "user/register";
        } else if(flag == 3){
            model.addAttribute("msg", "昵称已被使用或含有字符@");
            return "user/register";
        }
        session.setAttribute("login", 1); // 刚注册肯定是 1
        session.setAttribute("user", name);
        model.addAttribute("login", 1);
        model.addAttribute("user", name);
        return "user/login";
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session, Model model){
        String user = (String) session.getAttribute("user");
        if(Objects.isNull(user)){
            session.removeAttribute("login");
            model.addAttribute("msg", "尚未登录");
            return "user/login";
        }
        if(!userService.logout(user)){
            model.addAttribute("msg", "退出登录失败");
            return "user/login";
        }
        session.removeAttribute("user");
        session.removeAttribute("login");
        return "user/login";
    }

    @ResponseBody
    @RequestMapping("/localVerify")
    public String localVerify(@RequestBody Map<String, Object> params){
        String name = (String) params.get("name");
        String email = (String) params.get("email");

//        System.out.println(name + " " + email);
        User user = userService.getUser(name);
        if(Objects.isNull(user)){
            return "nothingness";
        }
        if(user.getEmail().equals(email)){
            return "yes";
        }
        return "no";
    }
}
