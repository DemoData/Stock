package com.hitales.entity;

import lombok.Data;

@Data
public class Patient {
    private Integer id;

    private String patientId;

    private String sex = "";

    private String age = "";

    private String birthDay = "";//生日

    private String name = "";

    private String origin = "";//籍贯

    private String marriage = "";//婚姻状况

    private String bloodType = "";//血型

    private String nation = "";//名族

    private String job = "";//职业

    private String address = "";//住址

    private boolean isForged = false;//是否伪造的patient

    private Long updateTime = System.currentTimeMillis();//上次更新时间

    private Long createTime;

    public enum ColumnMapping {
        ID("_id"),
        BATCH_NO("batchNo"),
        HOSPITAL_ID("hospitalId"),
        SEX("性别"),
        AGE("年龄"),
        BIRTHDAY("出生日期"),
        CREATE_TIME("createTime"),
        UPDATE_TIME("updateTime"),
        ORIGIN("籍贯"),
        MARRIAGE("婚姻状况"),
        ADDRESS("现住址"),
        NAME("姓名"),
        BLOOD_TYPE("血型"),
        NATION("名族"),
        JOB("职业");

        private final String value;

        ColumnMapping(String pValue) {
            this.value = pValue;
        }

        public String value() {
            return this.value;
        }
    }
}
