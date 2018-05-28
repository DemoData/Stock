package com.hitales.controller;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.config.MongoDataSourceConfig;
import com.hitales.common.support.ProgressCount;
import com.hitales.entity.StockInfo;
import com.hitales.service.standard.IDataService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author aron
 */
@Controller
@Api(tags = "Patient处理控制器")
@RequestMapping("/patient")
public class PatientController {
    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("patientService")
    private IDataService patientService;

    @Autowired
    @Qualifier(MongoDataSourceConfig.HRS_MONGO_TEMPLATE)
    protected MongoTemplate hrsMongoTemplate;

    /**
     * Patient数据处理
     *
     * @return
     */
    @RequestMapping("/processStart")
    @ResponseBody
    public String processPatient(@ModelAttribute StockInfo basicInfo) {
        patientService.setBasicInfo(basicInfo);
        //TODO:修改为在页面上配置字段映射
        patientService.setXmlPath("config/shsy/patient.xml");
        if (patientService.processData()) {
            return SUCCESS_FLAG;
        }
        return SUCCESS_FLAG;
    }

    /**
     * Patient数据处理
     *
     * @return
     */
    @RequestMapping("/processPatientWithoutParam")
    @ResponseBody
    public String processPatientWithoutParam() {
        StockInfo basicInfo = new StockInfo();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a117");
        basicInfo.setBatchNo("20180521");
        basicInfo.setPrefix("shrj");
        patientService.setBasicInfo(basicInfo);
        patientService.setXmlPath("config/rjny/patient-gz.xml");
        if (patientService.processData()) {
            return SUCCESS_FLAG;
        }
        return SUCCESS_FLAG;
    }

    /**
     * 获取数据操作进度
     *
     * @param hospitalName
     * @return
     */
    @RequestMapping("/getHospital")
    @ResponseBody
    public List<JSONObject> getHospital(@RequestParam(value = "q",required = false) String hospitalName) {
        Query patientQuery = new Query();
        if (hospitalName != null || "".equals(hospitalName)) {
            patientQuery.addCriteria(Criteria.where("name").regex(hospitalName));
        }
        List<JSONObject> hospital = hrsMongoTemplate.find(patientQuery, JSONObject.class, "Hospital");
        return hospital;
    }

    /**
     * 获取数据操作进度
     *
     * @param key
     * @return
     */
    @RequestMapping("/getProgress")
    @ResponseBody
    public Integer getProgress(String key) {
        Integer count = ProgressCount.getProgress(key);
        return count == null ? 0 : count;
    }

    @RequestMapping(value = {"", "/"})
    public String patient() {
        return "patient";
    }

}