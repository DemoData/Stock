package com.hitales.entity;

import lombok.Data;

@Deprecated
@Data
public class OperationMain {
    private Integer id;
    private String groupRecordName;//一次就诊号
    private String departmentDesc;//手术科室说明
    private String firstAssistant;//一助
    private String secondAssistant;//二助
    private String thirdAssistant;//三助
    private String fourthAssistant;//四助
    private String type;//手术类型
    private String operationPhysician;//手术医师
    private String firstOperateNurse;//第一台上护士
    private String secondOperateNurse;//第二台上护士
    private String isolationFlag;//隔离标志
    private String outTime;//出手术室具体时间
    private String postOperationDiagnose;//术后诊断
    private String firstSupplyNurse;//第一供应护士
    private String secondSupplyNurse;//第二供应护士
    private String illnessExplain;//病情说明
    private String operationNumber;//手术号
    private String preDiagnose;//术前主要诊断
    private String bloodTransfusionQuantity;//输血量
    private String narcosisSatisfaction;//麻醉满意程度
    private String startTime;//手术开始具体时间
    private String emergencyOrHospital;//门急诊OR住院
    private String narcosisType;//麻醉方法
    private String department;//手术科室
    private String part;//手术体位
    private String peeVolume;//尿量
    private String bloodProvider;//输血者
    private String endTime;//手术结束具体时间
    private String patientId;//病人标识
    private String narcosisPhysician;//麻醉医师
    private String narcosisEndTime;//麻醉结束具体时间
    private String operationRoomNo;//手术间
    private String operationRoomName;//手术室
    private String operationLevel;//手术等级
    private String infusionQuantity;//输液量
    private String operationName;//手术名称
    private String emergencyFlag;//急诊标志
    private String narcosisStartTime;//麻醉开始具体时间
    private String inTime;//进手术室具体时间
    private String smoothFlag;//手术过程顺利标志
    private String lostBloodVolume;//失血量
    private String patientLocatedDepartment;//病人所在科室

    public enum ColumnMapping {
        GROUP_RECORD_NAME("groupRecordName", "一次就诊号", false),
        DEPARTMENT_DESC("departmentDesc", "手术科室说明", true),
        FIRST_ASSISTANT("firstAssistant", "一助", true),
        SECOND_ASSISTANT("secondAssistant", "二助", true),
        THIRD_ASSISTANT("thirdAssistant", "三助", true),
        FOURTH_ASSISTANT("fourthAssistant", "四助", true),
        TYPE("type", "手术类型", true),
        OPERATION_PHYSICIAN("operationPhysician", "手术医师", true),
        FIRST_OPERATE_NURSE("firstOperateNurse", "第一台上护士", true),
        SECOND_OPERATE_NURSE("secondOperateNurse", "第二台上护士", true),
        ISOLATION_FLAG("isolationFlag", "隔离标志", true),
        OUT_TIME("outTime", "出手术室具体时间", true),
        POST_OPERATION_DIAGNOSE("postOperationDiagnose", "术后诊断", true),
        FIRST_SUPPLY_NURSE("firstSupplyNurse", "第一供应护士", true),
        SECOND_SUPPLY_NURSE("secondSupplyNurse", "第二供应护士", true),
        ILLNESS_EXPLAIN("illnessExplain", "病情说明", true),
        OPERATION_NUMBER("operationNumber", "手术号", true),
        PRE_DIAGNOSE("preDiagnose", "术前主要诊断", true),
        BLOOD_TRANSFUSION_QUANTITY("bloodTransfusionQuantity", "输血量", true),
        NARCOSIS_SATISFACTION("narcosisSatisfaction", "麻醉满意程度", true),
        START_TIME("startTime", "手术开始具体时间", true),
        EMERGENCY_OR_HOSPITAL("emergencyOrHospital", "门急诊OR住院", true),
        NARCOSIS_TYPE("narcosisType", "麻醉方法", true),
        DEPARTMENT("department", "手术科室", true),
        PART("part", "手术体位", true),
        PEE_VOLUME("peeVolume", "尿量", true),
        BLOOD_PROVIDER("bloodProvider", "输血者", true),
        END_TIME("endTime", "手术结束具体时间", true),
        PATIENT_ID("patientId", "病人标识", true),
        NARCOSIS_PHYSICIAN("narcosisPhysician", "麻醉医师", true),
        NARCOSIS_ENDT_IME("narcosisEndTime", "麻醉结束具体时间", true),
        OPERATION_ROOM_NO("operationRoomNo", "手术间", true),
        OPERATION_ROOM_NAME("operationRoomName", "手术室", true),
        OPERATION_LEVEL("operationLevel", "手术等级", true),
        INFUSION_QUANTITY("infusionQuantity", "输液量", true),
        OPERATION_NAME("operationName", "手术名称", true),
        EMERGENCY_FLAG("emergencyFlag", "急诊标志", true),
        NARCOSIS_START_TIME("narcosisStartTime", "麻醉开始具体时间", true),
        IN_TIME("inTime", "进手术室具体时间", true),
        SMOOTH_FLAG("smoothFlag", "手术过程顺利标志", true),
        LOST_BLOOD_VOLUME("lostBloodVolume", "失血量", true),
        PATIENT_LOCATED_DEPARTMENT("patientLocatedDepartment", "病人所在科室", true);

        private final String propName;
        private final String columnName;
        private final boolean flag;

        ColumnMapping(String pPropName, String pColumnName, boolean pFlag) {
            this.propName = pPropName;
            this.columnName = pColumnName;
            this.flag = pFlag;
        }

        public String propName() {
            return this.propName;
        }

        public String columnName() {
            return this.columnName;
        }

        public boolean isRequired() {
            return this.flag;
        }
    }
}
