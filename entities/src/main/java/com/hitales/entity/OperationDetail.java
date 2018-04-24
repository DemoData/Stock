package com.hitales.entity;

import lombok.Data;

@Deprecated
@Data
public class OperationDetail {
    private Integer id;
    private String groupRecordName;//一次就诊号
    private String endTime;//结束时间
    private String name;//手术名称
    private String serialNumber;//手术编号
    private String part;//手术部位
    private String level;//手术等级
    private String kerfType;//切口类型
    private String specialRequest;//特殊要求
    private String startTime;//开始时间
    private String levelDesc;//手术等级描述
    private String tumorSize;//肿瘤大小

    public enum ColumnMapping {
        END_TIME("结束时间"),
        NAME("手术名称"),
        SERIAL_NUMBER("手术编号"),
        PART("手术部位"),
        LEVEL("手术等级"),
        KERF_TYPE("切口类型"),
        SPECIAL_REQUEST("特殊要求"),
        START_TIME("开始时间"),
        LEVEL_DESC("手术等级描述"),
        TUMOR_SIZE("肿瘤大小");

        private final String value;

        ColumnMapping(String pValue) {
            this.value = pValue;
        }

        public String value() {
            return this.value;
        }
    }
}
