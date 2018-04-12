package com.hitales.controller.shtr;

import com.hitales.service.standard.IDataService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author aron
 */
@RestController
@Api(tags = "上海同仁医院控制器")
@RequestMapping("/shtr")
public class SHTRController {
    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

   /* @Autowired
    @Qualifier("shtrMedicalHistoryService")
    private IDataService medicalHistoryService;

    @Autowired
    @Qualifier("shtrInspectionService")
    private IDataService inspectionService;*/

    @Autowired
    @Qualifier("shtrAssayService")
    private IDataService assayService;

    /*@GetMapping("/processMedicalHistory")
    public String processMedicalHistory() {
        if (medicalHistoryService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processInspection")
    public String processInspection() {
        if (inspectionService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }*/

    @GetMapping("/processAssay")
    public String processAssay() {
        if (assayService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }
}
