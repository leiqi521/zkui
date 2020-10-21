package com.leiqi.zk.zkui.controller;

import com.alibaba.fastjson.JSONObject;
import com.leiqi.zk.zkui.vo.LeafBean;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    public String home(@RequestParam(value = "zkPath", defaultValue = "/") String zkPath, ModelMap map, Authentication authentication) {
        try {
            List<String> childrenList = zkClient.getChildren().forPath(zkPath);
            AtomicReference<List<String>> nodeLst = new AtomicReference<>();
            List<LeafBean> leafLst = new ArrayList<>();
            Optional.ofNullable(childrenList).ifPresent(list -> {
                List<String> nodeList = list.stream().filter(x -> !"zookeeper".equals(x)).sorted().collect(Collectors.toList());
                nodeLst.set(nodeList);
                if (!nodeList.isEmpty()) {
                    nodeList.forEach(p -> {
                        try {

                            String name = ("/".equals(zkPath) ? zkPath : zkPath + "/") + p;
                            List<String> cList = zkClient.getChildren().forPath(name);
                            //判断节点是不是孙子节点
                            if (cList.isEmpty()) {
                                byte[] value = zkClient.getData().forPath(name);
                                LeafBean leafBean = new LeafBean(name, p, value);
                                leafLst.add(leafBean);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                Collections.sort(leafLst, Comparator.comparing(LeafBean::getName));
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
            map.addAttribute("leafLst", leafLst);
            map.addAttribute("zkPath", zkPath);
            map.addAttribute("displayPath", displayPath);
            map.addAttribute("parentPath", parentPath);
            map.addAttribute("currentPath", currentPath);
            map.addAttribute("authRole", authentication.getAuthorities().iterator().next().toString());
            map.addAttribute("authName", authentication.getName());
            map.addAttribute("breadCrumbLst", displayPath.split("/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "home";
    }

    public final static String ROLE_ADMIN = "ROLE_ADMIN";

    @PostMapping("/home")
    public String home(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String action = request.getParameter("action");
        String currentPath = request.getParameter("currentPath");
        String displayPath = request.getParameter("displayPath");
        String newProperty = request.getParameter("newProperty");
        String newValue = request.getParameter("newValue");
        String newNode = request.getParameter("newNode");
        String[] nodeChkGroup = request.getParameterValues("nodeChkGroup");
        String searchStr = request.getParameter("searchStr").trim();
        String authRole = authentication.getAuthorities().iterator().next().toString();
        switch (action) {
            case "Save Node":
                try {
                    if (!"".equals(newNode) && !"".equals(currentPath) && ROLE_ADMIN.equals(authRole)) {
                        //Save the new node.
                        zkClient.create().creatingParentsIfNeeded().forPath(currentPath + newNode);
                    }
                    response.sendRedirect("/home?zkPath=" + displayPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "Update Property":
                if (!"".equals(newProperty) && !"".equals(currentPath) && ROLE_ADMIN.equals(authRole)) {
                    //Save the new node.
                    try {
                        zkClient.setData().forPath(currentPath + newProperty, newValue.getBytes());
                        response.sendRedirect("/home?zkPath=" + displayPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "Search":
                try {
                    Set<LeafBean> searchResult = new TreeSet<>();
                    setLeaf(searchStr, "/", searchResult);
                    String re = JSONObject.toJSONString(searchResult);
                    response.sendRedirect("/search?searchResult=" + URLEncoder.encode(re, StandardCharsets.UTF_8.name()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "Delete":
                if (ROLE_ADMIN.equals(authRole)) {

                    if (nodeChkGroup != null) {
                        for (String node : nodeChkGroup) {
                            try {
                                zkClient.delete().forPath(node);
                                response.sendRedirect("/home?zkPath=" + displayPath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
            default:
                try {
                    response.sendRedirect("/home");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return "";
    }

    @GetMapping("/loginError")
    public String loginError(ModelMap map) {
        map.addAttribute("error", "用户名或密码错误");
        return "login_error";
    }

    @GetMapping("/search")
    public String search(ModelMap map, @RequestParam(required = false, value = "searchResult") String searchResult) {
        List<LeafBean> searchResult1 = JSONObject.parseArray(searchResult, LeafBean.class);
        map.addAttribute("searchResult", searchResult1);
        return "search";
    }

    private void setLeaf(String searchName, String path, Set<LeafBean> searchResult) {
        try {
            List<String> list = zkClient.getChildren().forPath(path);
            if (list != null && !list.isEmpty()) {
                List<String> filterList = list.stream().filter(x -> !"zookeeper".equals(x)).collect(Collectors.toList());
                filterList.forEach(x -> {
                    String p = ("/".equals(path) ? path : path + "/") + x;
                    setLeaf(searchName, p, searchResult);
                });
            } else {
                byte[] data = zkClient.getData().forPath(path);
                String name = path.substring(path.lastIndexOf("/") + 1);
                if (name.contains(searchName)) {
                    LeafBean leafBean = new LeafBean(path, name, data);
                    searchResult.add(leafBean);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
