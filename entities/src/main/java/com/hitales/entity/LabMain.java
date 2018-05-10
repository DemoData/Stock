package com.hitales.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 化验申请单
 *
 * @author aron
 */
@Data
@Document(collection = "Record")
@JSONType(ignores = {"referenceId", "patientId", "groupRecordName"})
public class LabMain {
    //TODO：把所有表格类型的数据，做成和record一样，使用bean-name
    private String referenceId;
    private String groupRecordName;
    private String patientId;
    @JSONField(name = "化验名称")
    private String labName;
    @JSONField(name = "化验分类名称")
    private String labCateName;
    @JSONField(name = "申请时间")
    private String applyTime;
    @JSONField(name = "采样时间")
    private String samplingTime;
    @JSONField(name = "接收时间")
    private String receiveTime;
    @JSONField(name = "审核时间")
    private String auditTime;
    @JSONField(name = "报告时间")
    private String reportTime;
    @JSONField(name = "仪器编号")
    private String machineCode;
    @JSONField(name = "仪器名称")
    private String machineName;
    @JSONField(name = "申请科室")
    private String applyDept;
    @JSONField(name = "申请病区")
    private String applyWard;

}