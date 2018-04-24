package com.hitales.controller.ch.jyk;

import com.hitales.entity.Record;
import com.hitales.service.standard.IDataService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "长海医院检验科数据处理控制器")
@RequestMapping("/chjy")
public class ClinicalLabContoller {

    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("chyxAssayService")
    private IDataService assayService;

    @Autowired
    @Qualifier("chyxInspectionService")
    private IDataService inspectionService;

    @Autowired
    @Qualifier("chyxMedicalHistoryService")
    private IDataService medicalHistoryService;

    @Autowired
    @Qualifier("chyxMicroorganismService")
    private IDataService microorganismService;

    @Autowired
    @Qualifier("chyxPathologyService")
    private IDataService pathologyService;

    @Autowired
    @Qualifier("chyxMedicalOrderService")
    private IDataService medicalOrderService;

    @Autowired
    @Qualifier("chyxOperationDetailService")
    private IDataService operationDetailService;

    @Autowired
    @Qualifier("chyxOperationMainService")
    private IDataService operationMainService;

    /**
     * 长海医院化验数据处理
     *
     * @return
     */
    @GetMapping("/processAssay")
    public String processAssay() {
        if (assayService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    /**
     * 长海医院检查数据处理
     *
     * @return
     */
    @GetMapping("/processInspection")
    public String processInspection() {
        if (inspectionService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    /**
     * 长海医院病历文本数据处理
     *
     * @return
     */
    @GetMapping("/processMedicalHistory")
    public String processMedicalHistory() {
        if (medicalHistoryService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }


    /**
     * 长海医院微生物数据处理
     *
     * @return
     */
    @GetMapping("/processMicroorganism")
    public String processMicroorganism() {
        if (microorganismService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }


    /**
     * 长海医院病理数据处理
     *
     * @return
     */
    @GetMapping("/processPathology")
    public String processPathology() {
        if (pathologyService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

//==============肝癌===============

    @GetMapping("/ga/processAssay")
    public String processGAAssay() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setUserId("5acaddea85271af7af884b3a");
        basicInfo.setBatchNo("shch2018040901");
        basicInfo.setDepartment("检验科");
        basicInfo.setFormat("table");
        basicInfo.setDeleted(false);
        basicInfo.setSource("化验");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("化验记录");
        basicInfo.setSubRecordType("化验");
        assayService.setBasicInfo(basicInfo);
        if (assayService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/ga/processInspection")
    public String processGAInspection() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setUserId("5acaddea85271af7af884b3a");
        basicInfo.setBatchNo("shch2018040901");
        basicInfo.setDepartment("检验科");
        basicInfo.setFormat("text");
        basicInfo.setDeleted(false);
        basicInfo.setSource("检查");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("检查记录");
        basicInfo.setSubRecordType("检查");
        inspectionService.setBasicInfo(basicInfo);
        if (inspectionService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/ga/processMedicalHistory")
    public String processGAMedicalHistory() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setUserId("5acaddea85271af7af884b3a");
        basicInfo.setBatchNo("shch2018040901");
        basicInfo.setDepartment("检验科");
        basicInfo.setFormat("text");
        basicInfo.setDeleted(false);
        basicInfo.setSource("病历文书");
        basicInfo.setStatus("AMD识别完成");
        medicalHistoryService.setBasicInfo(basicInfo);
        if (medicalHistoryService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/ga/processMicroorganism")
    public String processGAMicroorganism() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setUserId("5acaddea85271af7af884b3a");
        basicInfo.setBatchNo("shch2018040901");
        basicInfo.setDepartment("检验科");
        basicInfo.setFormat("table");
        basicInfo.setDeleted(false);
        basicInfo.setSource("微生物");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("化验记录");
        basicInfo.setSubRecordType("微生物");
        microorganismService.setBasicInfo(basicInfo);
        if (microorganismService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/ga/processPathology")
    public String processGAPathology() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setUserId("5acaddea85271af7af884b3a");
        basicInfo.setBatchNo("shch2018040901");
        basicInfo.setDepartment("检验科");
        basicInfo.setRecordType("病理");
        basicInfo.setSubRecordType("病理");
        basicInfo.setFormat("text");
        basicInfo.setDeleted(false);
        basicInfo.setSource("病理");
        basicInfo.setStatus("AMD识别完成");
        pathologyService.setBasicInfo(basicInfo);
        if (pathologyService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/ga/processOperationMain")
    public String processGAOperationMain() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setBatchNo("shch2018040901");
        basicInfo.setDepartment("检验科");
        basicInfo.setRecordType("手术操作记录");
        basicInfo.setSubRecordType("手术事件");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("手术事件主记录");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setVersion(2.0);
        basicInfo.setOdCategories(new String[]{"肝癌"});
        operationMainService.setBasicInfo(basicInfo);
        if (operationMainService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/ga/processOperationDetail")
    public String processGAOperationDetail() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setBatchNo("shch2018040901");
        basicInfo.setDepartment("检验科");
        basicInfo.setRecordType("手术操作记录");
        basicInfo.setSubRecordType("手术明细");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("手术明细");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setVersion(2.0);
        basicInfo.setOdCategories(new String[]{"肝癌"});
        operationDetailService.setBasicInfo(basicInfo);
        if (operationDetailService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }


    //==============胃肠肿瘤===============

    @GetMapping("/zl/processAssay")
    public String processZLAssay() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setBatchNo("shch20180416");
        basicInfo.setDepartment("肿瘤科");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("化验");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("化验记录");
        basicInfo.setSubRecordType("化验");
        basicInfo.setOdCategories(new String[]{"胃肠肿瘤"});
        assayService.setBasicInfo(basicInfo);
        if (assayService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/zl/processInspection")
    public String processZLInspection() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setBatchNo("shch20180416");
        basicInfo.setDepartment("肿瘤科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("检查");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("检查记录");
        basicInfo.setSubRecordType("检查");
        basicInfo.setOdCategories(new String[]{"胃肠肿瘤"});
        inspectionService.setBasicInfo(basicInfo);
        if (inspectionService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/zl/processMedicalHistory")
    public String processZLMedicalHistory() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setBatchNo("shch20180416");
        basicInfo.setDepartment("肿瘤科");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("病历文书");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setOdCategories(new String[]{"胃肠肿瘤"});
        medicalHistoryService.setBasicInfo(basicInfo);
        if (medicalHistoryService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/zl/processMicroorganism")
    public String processZLMicroorganism() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setBatchNo("shch20180416");
        basicInfo.setDepartment("肿瘤科");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("微生物");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setRecordType("化验记录");
        basicInfo.setSubRecordType("微生物");
        basicInfo.setOdCategories(new String[]{"胃肠肿瘤"});
        microorganismService.setBasicInfo(basicInfo);
        if (microorganismService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/zl/processPathology")
    public String processZLPathology() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setBatchNo("shch20180416");
        basicInfo.setDepartment("肿瘤科");
        basicInfo.setRecordType("病理");
        basicInfo.setSubRecordType("病理");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("病理");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setOdCategories(new String[]{"胃肠肿瘤"});
        pathologyService.setBasicInfo(basicInfo);
        if (pathologyService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/zl/processMedicalOrder")
    public String processZLMedicalOrder() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setBatchNo("shch20180416");
        basicInfo.setDepartment("肿瘤科");
        basicInfo.setRecordType("治疗方案");
        basicInfo.setSubRecordType("药品医嘱");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("药品医嘱");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setOdCategories(new String[]{"胃肠肿瘤"});
        medicalOrderService.setBasicInfo(basicInfo);
        if (medicalOrderService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/zl/processOperationMain")
    public String processZLOperationMain() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setBatchNo("shch20180416");
        basicInfo.setDepartment("肿瘤科");
        basicInfo.setRecordType("手术操作记录");
        basicInfo.setSubRecordType("手术事件");
        basicInfo.setFormat(Record.FormatType.TEXT.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("手术事件主记录");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setOdCategories(new String[]{"胃肠肿瘤"});
        operationMainService.setBasicInfo(basicInfo);
        if (operationMainService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/zl/processOperationDetail")
    public String processZLOperationDetail() {
        Record basicInfo = new Record();
        basicInfo.setHospitalId("57b1e21fd897cd373ec7a14f");
        basicInfo.setBatchNo("shch20180416");
        basicInfo.setDepartment("肿瘤科");
        basicInfo.setRecordType("手术操作记录");
        basicInfo.setSubRecordType("手术明细");
        basicInfo.setFormat(Record.FormatType.TABLE.value());
        basicInfo.setDeleted(false);
        basicInfo.setSource("手术明细");
        basicInfo.setStatus("AMD识别完成");
        basicInfo.setOdCategories(new String[]{"胃肠肿瘤"});
        operationDetailService.setBasicInfo(basicInfo);
        if (operationDetailService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

}