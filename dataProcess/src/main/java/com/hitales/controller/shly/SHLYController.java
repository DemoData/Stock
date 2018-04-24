package com.hitales.controller.shly;

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
@RequestMapping("/shly")
public class SHLYController {
    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("assayService")
    private IDataService assayService;

    @Autowired
    @Qualifier("inspectionService")
    private IDataService inspectionService;

    @GetMapping("/processAssay")
    public String processAssay() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shly20180423");
        basicInfo.setDepartment("内分泌科");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("化验");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("化验记录");
        basicInfo.setSubRecordType("化验");
        basicInfo.setOdCategories(new String[]{"糖尿病"});
        assayService.setBasicInfo(basicInfo);

        if (assayService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processInspection")
    public String processInspection() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shly20180423");
        basicInfo.setDepartment("内分泌科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("检查");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("检查记录");
        basicInfo.setSubRecordType("检查");
        basicInfo.setOdCategories(new String[]{"糖尿病"});
        inspectionService.setBasicInfo(basicInfo);
        if (inspectionService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

}