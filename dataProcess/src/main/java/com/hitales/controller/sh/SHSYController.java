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
 * 上海十院Controller
 *
 * @author aron
 */
@RestController
@Api(tags = "数据入库处理控制器")
@RequestMapping("/shsy")
public class SHSYController {
    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("shsyExamService")
    private IDataService shsyExamService;

    @Autowired
    @Qualifier("tableService")
    private IDataService tableService;

    @Autowired
    @Qualifier("medicalContentService")
    private IDataService medicalContentService;

    @GetMapping("/processAssay")
    public String processAssay() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5aefb729acc162a73ee74f23");
        basicInfo.setBatchNo("shsy20180507");
        basicInfo.setDepartment("急诊科");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("化验");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("化验记录");
        basicInfo.setSubRecordType("化验");
        basicInfo.setOdCategories(new String[]{});
        tableService.setBasicInfo(basicInfo);
        tableService.setXmlPath("config/shsy/lab.xml");
        if (tableService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processExamDSL")
    public String processExamDSL() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shsy20180507");
        basicInfo.setDepartment("急诊科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("心电图");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("检查记录");
        basicInfo.setSubRecordType("心电图");
        basicInfo.setOdCategories(new String[]{});
        shsyExamService.setBasicInfo(basicInfo);
        shsyExamService.setXmlPath("config/shsy/exam-dsl.xml");
        if (shsyExamService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processExamES")
    public String processExamES() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shsy20180507");
        basicInfo.setDepartment("急诊科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("肠胃镜");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("检查记录");
        basicInfo.setSubRecordType("肠胃镜");
        shsyExamService.setBasicInfo(basicInfo);
        shsyExamService.setXmlPath("config/shsy/exam-es.xml");
        if (shsyExamService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processExamHYX")
    public String processExamHYX() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shsy20180507");
        basicInfo.setDepartment("急诊科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("核医学");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("检查记录");
        basicInfo.setSubRecordType("核医学");
        shsyExamService.setBasicInfo(basicInfo);
        shsyExamService.setXmlPath("config/shsy/exam-hyx.xml");
        if (shsyExamService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processExamRIS")
    public String processExamRIS() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shsy20180507");
        basicInfo.setDepartment("急诊科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("放射");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("检查记录");
        basicInfo.setSubRecordType("放射");
        shsyExamService.setBasicInfo(basicInfo);
        shsyExamService.setXmlPath("config/shsy/exam-ris.xml");
        if (shsyExamService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processExamUS")
    public String processExamUS() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shsy20180507");
        basicInfo.setDepartment("急诊科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("超声");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("检查记录");
        basicInfo.setSubRecordType("超声");
        shsyExamService.setBasicInfo(basicInfo);
        shsyExamService.setXmlPath("config/shsy/exam-us.xml");
        if (shsyExamService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processPathology")
    public String processPathology() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shsy20180507");
        basicInfo.setDepartment("急诊科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("病理");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("病理");
        basicInfo.setSubRecordType("病理");
        shsyExamService.setBasicInfo(basicInfo);
        shsyExamService.setXmlPath("config/shsy/pathology.xml");
        if (shsyExamService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processMedicalContent")
    public String processMedicalContent() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shsy20180507");
        basicInfo.setDepartment("急诊科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("病历文书");
        basicInfo.setStatus("AMD识别完成");
        medicalContentService.setBasicInfo(basicInfo);
        medicalContentService.setXmlPath("config/shsy/medical-content.xml");
        if (medicalContentService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

}