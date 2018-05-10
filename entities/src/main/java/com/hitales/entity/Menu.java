package com.hitales.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Menu {
    private Long id;

    private String text;// 菜单名称

    private String iconCls;

    private String url;

    private Menu parent;// 上级菜单

    private List<Menu> children = new ArrayList<>();// 下级菜单

    public String getState() {
        if ("高级设置".equals(text)) {//打开系统模块
            return "open";
        }
        //如果孩子为空就打开，否者就是关闭的
        return children.isEmpty() ? "open" : "closed";
    }

    // easyui-tree兼容
    public Object getAttributes() {
        Map<String, Object> map = new HashMap<>();
        map.put("url", url);
        return map;
    }

}
