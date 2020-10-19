package com.leiqi.zk.zkui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OperationController {

    @GetMapping("/loginPage")
    public String loginPage() {
        return "login_page";
    }

    @GetMapping("/index")
    public String index() {
        return "home";
    }

    @GetMapping("/loginError")
    public String loginError(ModelMap map) {
        map.addAttribute("error","用户名或密码错误");
        return "login_error";
    }
}
