package com.leiqi.zk.zkui.controller;

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

                            String name = "/".equals(zkPath) ? zkPath : zkPath + "/" + p;
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
        String[] propChkGroup = request.getParameterValues("propChkGroup");

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
            case "Save Property":
//                if (!newProperty.equals("") && !currentPath.equals("") && authRole.equals(ZooKeeperUtil.ROLE_ADMIN)) {
//                    //Save the new node.
//                    ZooKeeperUtil.INSTANCE.createNode(currentPath, newProperty, newValue, ServletUtil.INSTANCE.getZookeeper(request, response, zkServerLst[0], globalProps));
//                    request.getSession().setAttribute("flashMsg", "Property Saved!");
//                    if (ZooKeeperUtil.INSTANCE.checkIfPwdField(newProperty)) {
//                        newValue = ZooKeeperUtil.INSTANCE.SOPA_PIPA;
//                    }
//                    dao.insertHistory((String) request.getSession().getAttribute("authName"), request.getRemoteAddr(), "Saving Property: " + currentPath + "," + newProperty + "=" + newValue);
//                }
//                response.sendRedirect("/home?zkPath=" + displayPath);
                break;
            case "Update Property":
//                if (!newProperty.equals("") && !currentPath.equals("") && authRole.equals(ZooKeeperUtil.ROLE_ADMIN)) {
//                    //Save the new node.
//                    ZooKeeperUtil.INSTANCE.setPropertyValue(currentPath, newProperty, newValue, ServletUtil.INSTANCE.getZookeeper(request, response, zkServerLst[0], globalProps));
//                    request.getSession().setAttribute("flashMsg", "Property Updated!");
//                    if (ZooKeeperUtil.INSTANCE.checkIfPwdField(newProperty)) {
//                        newValue = ZooKeeperUtil.INSTANCE.SOPA_PIPA;
//                    }
//                    dao.insertHistory((String) request.getSession().getAttribute("authName"), request.getRemoteAddr(), "Updating Property: " + currentPath + "," + newProperty + "=" + newValue);
//                }
//                response.sendRedirect("/home?zkPath=" + displayPath);
                break;
            case "Search":
//                Set<LeafBean> searchResult = ZooKeeperUtil.INSTANCE.searchTree(searchStr, ServletUtil.INSTANCE.getZookeeper(request, response, zkServerLst[0], globalProps), authRole);
//                templateParam.put("searchResult", searchResult);
//                ServletUtil.INSTANCE.renderHtml(request, response, templateParam, "search.ftl.html");
                break;
            case "Delete":
//                if (authRole.equals(ZooKeeperUtil.ROLE_ADMIN)) {
//
//                    if (propChkGroup != null) {
//                        for (String prop : propChkGroup) {
//                            List delPropLst = Arrays.asList(prop);
//                            ZooKeeperUtil.INSTANCE.deleteLeaves(delPropLst, ServletUtil.INSTANCE.getZookeeper(request, response, zkServerLst[0], globalProps));
//                            request.getSession().setAttribute("flashMsg", "Delete Completed!");
//                            dao.insertHistory((String) request.getSession().getAttribute("authName"), request.getRemoteAddr(), "Deleting Property: " + delPropLst.toString());
//                        }
//                    }
//                    if (nodeChkGroup != null) {
//                        for (String node : nodeChkGroup) {
//                            List delNodeLst = Arrays.asList(node);
//                            ZooKeeperUtil.INSTANCE.deleteFolders(delNodeLst, ServletUtil.INSTANCE.getZookeeper(request, response, zkServerLst[0], globalProps));
//                            request.getSession().setAttribute("flashMsg", "Delete Completed!");
//                            dao.insertHistory((String) request.getSession().getAttribute("authName"), request.getRemoteAddr(), "Deleting Nodes: " + delNodeLst.toString());
//                        }
//                    }
//
//                }
//                response.sendRedirect("/home?zkPath=" + displayPath);
                break;
            default:
//                response.sendRedirect("/home");
        }

        return "1";
    }

    @GetMapping("/loginError")
    public String loginError(ModelMap map) {
        map.addAttribute("error", "用户名或密码错误");
        return "login_error";
    }
}
