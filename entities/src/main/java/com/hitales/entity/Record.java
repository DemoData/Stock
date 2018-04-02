package com.hitales.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Record {
    private String id;
    private String hospitalId;
    private String userId;
    private String groupRecordName;//一次就诊号
    private String patientId;
    private String batchNo;
    private String templateId;
    private String department;
    private JSONObject info = new JSONObject();
    private String recordType;
    private String subRecordType;
    private String sourceRecordType;
    private String[] odCategories;
    private String[] orgOdCategories;
    private String sourceId;
    private String format;
    private boolean deleted;
    private String source;
    private String status;
    private Long createTime;

    public Record() {
        initial();
    }

    private void initial() {
        //init info
        List<Map<String, String>> detailArray = new ArrayList<>();
        List<Map<String, String>> formattedText = new ArrayList<>();
        List<Map<String, String>> table = new ArrayList<>();
        this.info.put("basicInfo", new JSONObject());
        this.info.put("detailArray", detailArray);
        this.info.put("text", "");
        this.info.put("textARS", "");
        this.info.put("formattedText", formattedText);
        this.info.put("table", table);
        this.hospitalId = "";
        this.userId = "";
        this.groupRecordName = "";
        this.patientId = "";
        this.batchNo = "";
        this.templateId = "";
        this.department = "";
        this.recordType = "";
        this.subRecordType = "";
        this.sourceId = "";
        this.format = "";
        this.source = "";
        this.status = "";
        this.sourceRecordType = "";
    }
}
