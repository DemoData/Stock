package com.hitales.controller.sh;

import com.hitales.entity.Record;
import com.hitales.service.standard.IDataService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 上海仁济Controller
 *
 * @author aron
 */
@RestController
@Api(tags = "数据入库处理控制器")
@RequestMapping("/shrj")
public class SHRJController {
    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("shrjTableService")
    private IDataService tableService;

    @Autowired
    @Qualifier("shrjMedicalContentService")
    private IDataService medicalContentService;

    @GetMapping("/processAssay")
    public String processAssay() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a117");
        basicInfo.setBatchNo("shrj20180508");
        basicInfo.setDepartment("风湿免疫科");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("化验");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("化验记录");
        basicInfo.setSubRecordType("化验");
        basicInfo.setOdCategories(new String[]{"SLE"});
        tableService.setBasicInfo(basicInfo);
        tableService.setXmlPath("config/shrj/lab-test.xml");
        if (tableService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processOrders")
    public String processOrders() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a117");
        basicInfo.setBatchNo("shrj20180508");
        basicInfo.setDepartment("风湿免疫科");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("医嘱单");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("治疗方案");
        basicInfo.setSubRecordType("医嘱单");
        basicInfo.setOdCategories(new String[]{"SLE"});
        tableService.setBasicInfo(basicInfo);
        tableService.setXmlPath("config/shrj/orders-test.xml");
        if (tableService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processMedicalContent")
    public String processMedicalContent() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a117");
        basicInfo.setBatchNo("shrj20180508");
        basicInfo.setDepartment("风湿免疫科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("病历文书");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setOdCategories(new String[]{"SLE"});
        medicalContentService.setBasicInfo(basicInfo);
        medicalContentService.setXmlPath("config/shrj/medical-content.xml");
        if (medicalContentService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

}