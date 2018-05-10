package com.hitales.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 化验详单
 *
 * @author aron
 */
@Data
@Document(collection = "Record")
@JSONType(ignores = {"id", "patientId", "groupRecordName"})
public class LabReport {
    private String referenceId;
    @JSONField(name = "文本结果")
    private String textResult;
    @JSONField(name = "数值结果")
    private String numberResult;
    @JSONField(name = "结果单位")
    private String unit;
    @JSONField(name = "结果状态")
    private String reportState;
    @JSONField(name = "正常低值")
    private String lowValue;
    @JSONField(name = "正常高值")
    private String highValue;
    @JSONField(name = "参考范围")
    private String reference;
    @JSONField(name = "异常标识")
    private String abnormalFlag;
    @JSONField(name = "备注")
    private String remark;
    @JSONField(name = "检测时间")
    private String detectionTime;
    @JSONField(name = "化验项名称")
    private String labItemName;
    @JSONField(name = "标本名称")
    private String specimenName;

}