package com.hitales.controller;

import com.hitales.entity.Record;
import com.hitales.entity.StockInfo;
import com.hitales.other.BlobToContent;
import com.hitales.other.DataToMysql;
import com.hitales.service.standard.IDataService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * @author aron
 */
@RestController
@Api(tags = "数据入库处理控制器")
@RequestMapping("/stock")
public class StockController {
    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("patientService")
    private IDataService patientService;

    @Autowired
    private DataToMysql dataToMysql;

    @Autowired
    private BlobToContent blobToContent;

    /**
     * Patient数据处理
     *
     * @return
     */
    @GetMapping("/processPatient")
    public String processPatient() {
        Map<Object, Object> basicInfo = new HashMap<>();
        basicInfo.put("hospitalId", "5ad86cb8acc162a73ee74f16");
        basicInfo.put("batchNo", "shly20180423");
        basicInfo.put("patientPrefix", "shly_");
        patientService.setBasicInfo(basicInfo);

        if (patientService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

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
    public ModelAndView stock() {
        StockInfo info = new StockInfo();
        info.setBatchNo("shch20180416");
        info.setHospitalId("57b1e21fd897cd373ec7a14f");
        info.setUserId("aron3");
        //这里指在templates目录下面去找index.html

        ModelAndView modelAndView = new ModelAndView("/main");
        modelAndView.addObject("stockInfo", info);
        return modelAndView;
    }
}