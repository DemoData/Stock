package com.hitales.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Patient")
public class Patient {
    private String batchNo;
    private String hospitalId;
    @JSONField(name = "_id")
    private String patientId;
    @JSONField(name = "性别")
    private String sex = "";
    @JSONField(name = "年龄")
    private String age = "";
    @JSONField(name = "出生日期")
    private String birthDay = "";//生日
    @JSONField(name = "姓名")
    private String name = "";
    @JSONField(name = "籍贯")
    private String origin = "";//籍贯
    @JSONField(name = "婚姻状况")
    private String marriage = "";//婚姻状况
    @JSONField(name = "血型")
    private String bloodType = "";//血型
    @JSONField(name = "名族")
    private String nation = "";//名族
    @JSONField(name = "职业")
    private String job = "";//职业
    @JSONField(name = "现住址")
    private String address = "";//现住址
    private boolean isForged = false;//是否伪造的patient

    private Long updateTime = System.currentTimeMillis();//上次更新时间

    private Long createTime;

}
