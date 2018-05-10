package com.hitales.controller;

import com.hitales.entity.Menu;
import com.hitales.other.BlobToContent;
import com.hitales.other.DataToMysql;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aron
 */
@Controller
@Api(tags = "数据入库处理控制器")
@RequestMapping("/stock")
public class StockController {
    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    private DataToMysql dataToMysql;

    @Autowired
    private BlobToContent blobToContent;

    @GetMapping("/processTest")
    public String processTest() {
        dataToMysql.process();
        return SUCCESS_FLAG;
    }

    @GetMapping("/blobToContent")
    public String blobToContent() {
        blobToContent.processData();
        return SUCCESS_FLAG;
    }

    @RequestMapping("/index")
    public String stock() {
        /*StockInfo info = new StockInfo();
        info.setBatchNo("shch20180416");
        info.setHospitalId("57b1e21fd897cd373ec7a14f");
        info.setUserId("aron3");
        //这里指在templates目录下面去找index.html

        ModelAndView modelAndView = new ModelAndView("/main");
        modelAndView.addObject("stockInfo", info);*/
        return "main";
    }

    @RequestMapping("/menu/initTree")
    @ResponseBody
    public List<Menu> initMenu() {
        List<Menu> menuList = new ArrayList<>();
        Menu menu = new Menu();
        menu.setText("入库模块");
        menu.setId(1L);
        menu.setIconCls("icon-diy6");
        menu.setUrl("/patient");
        menuList.add(menu);
        return menuList;
    }
}