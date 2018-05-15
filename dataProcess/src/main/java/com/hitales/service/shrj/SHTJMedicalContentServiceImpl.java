package com.hitales.service.shrj;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.config.MongoDataSourceConfig;
import com.hitales.common.support.Mapping;
import com.hitales.common.support.MappingMatch;
import com.hitales.common.support.SplitAnchor;
import com.hitales.common.support.TextFormatter;
import com.hitales.dao.standard.IMedicalHistoryDao;
import com.hitales.dao.standard.TextDao;
import com.hitales.entity.MedicalHistory;
import com.hitales.entity.Record;
import com.hitales.service.MedicalContentServiceImpl;
import com.hitales.service.TextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service("shrjMedicalContentService")
public class SHTJMedicalContentServiceImpl extends MedicalContentServiceImpl {

    //前面不是中文的需要打上锚点
    static List<String> prefAnchorList = new ArrayList<>();

    static {
        prefAnchorList.add("补充病史和体征");
        prefAnchorList.add("初步诊断");
        prefAnchorList.add("诊断依据");
        prefAnchorList.add("鉴别诊断");
        prefAnchorList.add("诊疗计划");
        prefAnchorList.add("注意事项");
        prefAnchorList.add("分析讨论");
        prefAnchorList.add("入院日期");
        prefAnchorList.add("出院日期");
        prefAnchorList.add("门诊诊断");
        prefAnchorList.add("入院诊断");
//        prefAnchorList.add("出院诊断");
        prefAnchorList.add("入院时主要症状及体征");
        prefAnchorList.add("病程与治疗结果（注明手术日期、手术名称、输血量及抢救情况）");
        prefAnchorList.add("病程与治疗结果(注明手术日期、手术名称、输血量及抢救情况)");
        prefAnchorList.add("出院时情况（症状及体征）");
        prefAnchorList.add("出院时情况(症状及体征)");
        prefAnchorList.add("出院后用药及建议");
        prefAnchorList.add("医师签名");
        prefAnchorList.add("主　诉");
        prefAnchorList.add("现病史");
        prefAnchorList.add("既往史");
        prefAnchorList.add("个人史");
        prefAnchorList.add("婚育史");
        prefAnchorList.add("月经史");
        prefAnchorList.add("家族史");
        prefAnchorList.add("体　格　检　查");
        prefAnchorList.add("专　科　检　查");
        prefAnchorList.add("实验室及其他辅助检查");
        prefAnchorList.add("初步诊断");
        prefAnchorList.add("医师签名");
        prefAnchorList.add("日　　期");
        prefAnchorList.add("主要化验结果");
        prefAnchorList.add("特殊检验及重要会诊");
        prefAnchorList.add("合并症");
        prefAnchorList.add("治疗结果");
        prefAnchorList.add("病史可靠程度");
        prefAnchorList.add("病史陈述者");
        prefAnchorList.add("病史采集时间");
        prefAnchorList.add("入院时主要病状及体征");
        prefAnchorList.add("预约提示");
        prefAnchorList.add("主任医师");
        prefAnchorList.add("主治医师");
        prefAnchorList.add("住院医师");
        prefAnchorList.add("记录日期");
        prefAnchorList.add("系统回顾(阳性表现应在空间内填写发病时间及扼要诊疗经过)");
        prefAnchorList.add("姓名");
        prefAnchorList.add("门（急）诊诊断");
        prefAnchorList.add("与患者关系");
        prefAnchorList.add("主  　诉");
        prefAnchorList.add("入院情况");
        prefAnchorList.add("姓    名");

        prefAnchorList.add("日期");
        prefAnchorList.add("记录时间");
        prefAnchorList.add("病史陈述者");
        prefAnchorList.add("主治医师第一次查房诊断");
        prefAnchorList.add("主  　诉");
        prefAnchorList.add("出院时间");
        prefAnchorList.add("入院情况");
        prefAnchorList.add("诊疗经过");
        prefAnchorList.add("出院情况");
        prefAnchorList.add("出院医嘱");
        prefAnchorList.add("签名 （签名）");
        prefAnchorList.add("死亡时间");
        prefAnchorList.add("急诊诊断");
        prefAnchorList.add("死亡诊断");
        prefAnchorList.add("主要诊断 ， 其他诊断");
        prefAnchorList.add("诊断日期");
        prefAnchorList.add("本　科　检　查");
    }

    //前面不能为中文，后面跟冒号作为锚点
    static List<String> colonAnchorList = new ArrayList<>();

    static {
        colonAnchorList.add("查体");
        colonAnchorList.add("入院查体");
    }

    static List<String> colonEndAnchorList = new ArrayList<>();

    static {
        colonEndAnchorList.add("出院诊断");
    }

    @Override
    protected void customProcess(Record record, MedicalHistory entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        super.customProcess(record, entity, orgOdCatCaches, patientCaches, dataSource);
        if (StringUtils.isEmpty(record.getPatientId())) {
            String groupRecordName = record.getGroupRecordName();
            String value = groupRecordName.substring(groupRecordName.length() - 5);
            if (patientCaches.isEmpty() || StringUtils.isEmpty(patientCaches.get(value))) {
                List<Map<String, Object>> maps = getMedicalHistoryDao().getJdbcTemplate(dataSource).queryForList("select patientId from 仁济_patient where groupRecordName like '%" + value + "%'");
                if (!maps.isEmpty()) {
                    patientCaches.put(value, maps.get(0).get("patientId").toString());
                }
            }
//            record.setVersion(1.1);
            record.setPatientId(patientCaches.get(value) == null ? "" : "shrj_"+patientCaches.get(value));
        }
    }

    /**
     * Put MedicalHistory data to Record
     *
     * @param medicalHistory
     * @param record
     */
    protected void putText2Record(MedicalHistory medicalHistory, Record record) {
        JSONObject info = record.getInfo();
        //调用提供的方法得到锚点文本
        String medicalContent = medicalHistory.getMedicalContent();
        if (StringUtils.isEmpty(medicalContent)) {
            log.error("!!!! 病历内容为空 , id : " + medicalHistory.getId() + "!!!!");
        }
        String text = processAnchor(medicalContent);
        info.put(TextFormatter.TEXT, text);
        info.put(TextFormatter.TEXT_ARS, medicalContent);
    }

    public String processAnchor(String textARS) {
        for (String anchor : prefAnchorList) {
            int lastIndex = 0;
            while (textARS.indexOf(anchor, lastIndex) != -1) {
                int index = textARS.indexOf(anchor, lastIndex);
                if (index == 0 || !SplitAnchor.isChinese(textARS.charAt(index - 1))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(textARS.substring(0, index));
                    stringBuilder.append("【【" + anchor + "】】");
                    stringBuilder.append(textARS.substring(index + anchor.length()));
                    textARS = stringBuilder.toString();
                }
                lastIndex = index + anchor.length();
            }
        }
        for (String anchor : colonAnchorList) {
            int lastIndex = 0;
            while (textARS.indexOf(anchor, lastIndex) != -1) {
                int index = textARS.indexOf(anchor, lastIndex);
                if ((index == 0 || !SplitAnchor.isChinese(textARS.charAt(index - 1))) &&
                        ('：' == textARS.charAt(index + anchor.length()) || ':' == textARS.charAt(index + anchor.length())
                                || ' ' == textARS.charAt(index + anchor.length()) || '\n' == textARS.charAt(index + anchor.length())
                                || '　' == textARS.charAt(index + anchor.length()))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(textARS.substring(0, index));
                    stringBuilder.append("【【" + anchor + "】】");
                    stringBuilder.append(textARS.substring(index + anchor.length()));
                    textARS = stringBuilder.toString();
                }
                lastIndex = index + anchor.length();
            }
        }
        for (String anchor : colonEndAnchorList) {
            int lastIndex = 0;
            while (textARS.indexOf(anchor, lastIndex) != -1) {
                int index = textARS.indexOf(anchor, lastIndex);
                if ('：' == textARS.charAt(index + anchor.length()) || ':' == textARS.charAt(index + anchor.length()) || ' ' == textARS.charAt(index + anchor.length())) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(textARS.substring(0, index));
                    stringBuilder.append("【【" + anchor + "】】");
                    stringBuilder.append(textARS.substring(index + anchor.length()));
                    textARS = stringBuilder.toString();
                }
                lastIndex = index + anchor.length();
            }
        }
        textARS = textARS.replaceAll("【【门（急）诊诊断】】 【【门（急）诊诊断】】", "【【门（急）诊诊断】】");
        textARS = textARS.replaceAll("【【门（急）诊诊断】】  【【门（急）诊诊断】】", "【【门（急）诊诊断】】");
        textARS = textARS.replaceAll("【【门（急）诊诊断】】   【【门（急）诊诊断】】", "【【门（急）诊诊断】】");
        textARS = textARS.replaceAll("【【门（急）诊诊断】】    【【门（急）诊诊断】】", "【【门（急）诊诊断】】");
        textARS = textARS.replaceAll("【【主治医师】】第一次查房诊断", "【【主治医师第一次查房诊断】】");
        textARS = textARS.replaceAll("【【主治医师第一次查房诊断】】 【【主治医师第一次查房诊断】】", "【【主治医师第一次查房诊断】】");
        textARS = textARS.replaceAll("【【日期】】 【【日期】】", "【【日期】】");
        textARS = textARS.replaceAll("【【主治医师第一次查房诊断】】： 【【主治医师第一次查房诊断】】", "【【主治医师第一次查房诊断】】:");
        textARS = textARS.replaceAll("【【诊断日期】】： 【【诊断日期】】", "【【诊断日期】】:");
        textARS = textARS.replaceAll("【【家族史（注意与患者现病有关的遗传病及传染性疾病）】】", "【【家族史】】");
        textARS = textARS.replaceAll("（记录父母、兄弟、姐妹健康状况，有无与患者类似疾病，有无家族遗传倾向的疾病）", "");
        textARS = textARS.replaceAll("（注意与患者现病有关的遗传病及传染性疾病）", "");

        return textARS;
    }

}
