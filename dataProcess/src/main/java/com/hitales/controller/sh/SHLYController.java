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
 * 上海六院Controller
 *
 * @author aron
 */
@RestController
@Api(tags = "数据入库处理控制器")
@RequestMapping("/shly")
public class SHLYController {
    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    //已被tableService取代
    @Autowired
    @Qualifier("assayService")
    private IDataService labService;

    @Autowired
    @Qualifier("textService")
    private IDataService textService;

    @Autowired
    @Qualifier("tableService")
    private IDataService tableService;

    @Autowired
    @Qualifier("shlyMedicalContentService")
    private IDataService medicalContentService;

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
        basicInfo.setVersion(1.1);
        labService.setBasicInfo(basicInfo);
        labService.setXmlPath("config/shly/lab.xml");
        if (labService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processFSExam")
    public String processFSExam() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shly20180423");
        basicInfo.setDepartment("内分泌科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("放射");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("检查记录");
        basicInfo.setSubRecordType("放射");
        basicInfo.setOdCategories(new String[]{"糖尿病"});
        basicInfo.setVersion(1.1);
        textService.setBasicInfo(basicInfo);
        textService.setXmlPath("config/shly/exam-radiology.xml");
        if (textService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processCSExam")
    public String processCSExam() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shly20180423");
        basicInfo.setDepartment("内分泌科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("超声");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("检查记录");
        basicInfo.setSubRecordType("超声");
        basicInfo.setOdCategories(new String[]{"糖尿病"});
        basicInfo.setVersion(1.1);
        textService.setBasicInfo(basicInfo);
        textService.setXmlPath("config/shly/exam-ultrasound.xml");
        if (textService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processPathology")
    public String processPathology() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shly20180423");
        basicInfo.setDepartment("内分泌科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("病理");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("病理");
        basicInfo.setSubRecordType("病理");
        basicInfo.setOdCategories(new String[]{"糖尿病"});
        basicInfo.setVersion(1.1);
        textService.setBasicInfo(basicInfo);
        textService.setXmlPath("config/shly/pathology.xml");
        if (textService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processAdviceHerbal")
    public String processAdviceHerbal() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shly20180423");
        basicInfo.setDepartment("内分泌科");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("草药医嘱");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("治疗方案");
        basicInfo.setSubRecordType("草药医嘱");
        basicInfo.setOdCategories(new String[]{"糖尿病"});
        basicInfo.setVersion(1.1);
        tableService.setBasicInfo(basicInfo);
        tableService.setXmlPath("config/shly/advice-herbal-medication-orders.xml");
        if (tableService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processAdviceMedication")
    public String processAdviceMedication() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shly20180423");
        basicInfo.setDepartment("内分泌科");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("住院用药医嘱");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("治疗方案");
        basicInfo.setSubRecordType("住院用药医嘱");
        basicInfo.setOdCategories(new String[]{"糖尿病"});
        basicInfo.setVersion(1.1);
        tableService.setBasicInfo(basicInfo);
        tableService.setXmlPath("config/shly/advice-medication-orders.xml");
        if (tableService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processAdviceNoMedication")
    public String processAdviceNoMedication() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shly20180423");
        basicInfo.setDepartment("内分泌科");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("住院非药医嘱");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("治疗方案");
        basicInfo.setSubRecordType("住院非药医嘱");
        basicInfo.setOdCategories(new String[]{"糖尿病"});
        basicInfo.setVersion(1.1);
        tableService.setBasicInfo(basicInfo);
        tableService.setXmlPath("config/shly/advice-no-medication-orders.xml");
        if (tableService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processOutPatientRecipe")
    public String processOutPatientRecipe() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shly20180423");
        basicInfo.setDepartment("内分泌科");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("门诊-处方");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("门诊记录");
        basicInfo.setSubRecordType("门诊处方");
        basicInfo.setOdCategories(new String[]{"糖尿病"});
        basicInfo.setVersion(1.1);
        tableService.setBasicInfo(basicInfo);
        tableService.setXmlPath("config/shly/out-patient-recipe.xml");
        if (tableService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processMedicalContent")
    public String processMedicalContent() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shly20180423");
        basicInfo.setDepartment("内分泌科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("病历文书");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setOdCategories(new String[]{"糖尿病"});
        basicInfo.setVersion(1.1);
        medicalContentService.setBasicInfo(basicInfo);
        medicalContentService.setXmlPath("config/shly/medical-content.xml");
        if (medicalContentService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }


    @GetMapping("/processOutPatientVisit")
    public String processOutPatientVisit() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shly20180423");
        basicInfo.setDepartment("内分泌科");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("门诊-就诊记录");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("门诊记录");
        basicInfo.setSubRecordType("就诊记录");
        basicInfo.setOdCategories(new String[]{"糖尿病"});
        basicInfo.setVersion(1.1);
        tableService.setBasicInfo(basicInfo);
        tableService.setXmlPath("config/shly/out-patient-visit-record.xml");
        if (tableService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processReaction")
    public String processReaction() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("5ad86cb8acc162a73ee74f16");
        basicInfo.setBatchNo("shly20180423");
        basicInfo.setDepartment("内分泌科");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("过敏不良反应");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("其他记录");
        basicInfo.setSubRecordType("过敏不良反应");
        basicInfo.setOdCategories(new String[]{"糖尿病"});
        basicInfo.setVersion(1.1);
        tableService.setBasicInfo(basicInfo);
        tableService.setXmlPath("config/shly/patient-allergy-adverse-reaction.xml");
        if (tableService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

}