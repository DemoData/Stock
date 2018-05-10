package com.hitales.entity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "Record")
@JSONType(ignores = {"id", "reportDate", "condition"})
public class Record {
    @JSONField(name = "_id")
    private String rid;//用于mongodb的主键
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
    private String[] orgOdCategories = new String[0];
    private String sourceId;
    private String format;//文本text，半结构化half-text，结构化table
    private boolean deleted;
    private String source;//数据来源，eg:化验，检查，病历文书
    private Double version = 1.0;//初始为1.0
    private List<LogRecord> logs = new ArrayList<>();//用于记录数据类型修改的版本记录
    private String status;
    private Long createTime;
    private Long updateTime = System.currentTimeMillis();//上次更新时间
    private String reportDate;//临时：化验报告日期
    private Map<String, Object> condition;//额外条件的存放

    public Record() {
        initial();
    }

    public void initial() {
        //init info
        List<Map<String, String>> detailArray = new ArrayList<>();
        List<Map<String, String>> formattedText = new ArrayList<>();
        List<Map<String, String>> table = new ArrayList<>();
        LogRecord logRecord = new LogRecord();
        logRecord.setVersion(this.version);
        this.logs.add(logRecord);
        this.info.put("basicInfo", new JSONObject());
        this.info.put("detailArray", detailArray);
        this.info.put("text", "");
        this.info.put("textARS", "");
        this.info.put("formattedText", formattedText);
        this.info.put("table", table);
        this.rid = "";
        this.hospitalId = "";
        this.userId = "";
        this.groupRecordName = "";
        this.patientId = "";
        this.batchNo = "";
        this.templateId = "";
        this.department = "";
        this.setRecordType("");
        this.setSubRecordType("");
        this.sourceId = "";
        this.format = "";
        this.source = "";
        this.status = "";
        this.sourceRecordType = "";
    }

    public void setRecordType(String pRecordType) {
        this.recordType = pRecordType;
        List<LogRecord> logs = getLogs();
        if (logs.size() == 1) {
            logs.get(0).setRecordType(pRecordType);
        }
    }

    public void setSubRecordType(String pSubRecordType) {
        this.subRecordType = pSubRecordType;
        List<LogRecord> logs = getLogs();
        if (logs.size() == 1) {
            logs.get(0).setSubRecordType(pSubRecordType);
        }
    }

    public enum FormatType {
        TEXT("text"),
        HALF_TEXT("half-text"),
        TABLE("table");
        private final String value;

        FormatType(String pValue) {
            this.value = pValue;
        }

        public String value() {
            return this.value;
        }
    }

}
