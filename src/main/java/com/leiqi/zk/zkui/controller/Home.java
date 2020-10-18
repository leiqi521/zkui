package com.leiqi.zk.zkui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Home {
    @RequestMapping("/test.html")
    public String index( ) {
        return "home.ftl";
    }

}
