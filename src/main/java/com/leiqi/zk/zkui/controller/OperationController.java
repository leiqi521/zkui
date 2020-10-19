package com.leiqi.zk.zkui.controller;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Controller
public class OperationController {
    @Autowired
    private CuratorFramework zkClient;

    @GetMapping("/loginPage")
    public String loginPage() {
        return "login_page";
    }

    @GetMapping("/home")
    public String home(@RequestParam(value = "zkPath", defaultValue = "/") String zkPath, ModelMap map) {
        try {
            List<String> childrenList = zkClient.getChildren().forPath(zkPath);
            AtomicReference<List<String>> nodeLst =new AtomicReference<>();
            AtomicReference<List<String>> leafLst =new AtomicReference<>();
            Optional.ofNullable(childrenList).ifPresent(list -> {
                nodeLst.set(list.stream().filter(x -> !"zookeeper".equals(x)).sorted().collect(Collectors.toList()));

            });
            String currentPath, parentPath, displayPath;
            if (zkPath.equals("/")) {
                currentPath = "/";
                displayPath = "/";
                parentPath = "/";
            } else {
                currentPath = zkPath + "/";
                displayPath = zkPath;
                parentPath = zkPath.substring(0, zkPath.lastIndexOf("/"));
                if (parentPath.equals("")) {
                    parentPath = "/";
                }
            }
            map.addAttribute("nodeLst", nodeLst.get());
            map.addAttribute("leafLst", new ArrayList<>());
            map.addAttribute("zkpath", zkPath);
            map.addAttribute("displayPath", displayPath);
            map.addAttribute("parentPath", parentPath);
            map.addAttribute("currentPath", currentPath);
            map.addAttribute("breadCrumbLst", displayPath.split("/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "home";
    }

    @GetMapping("/loginError")
    public String loginError(ModelMap map) {
        map.addAttribute("error", "用户名或密码错误");
        return "login_error";
    }
}
