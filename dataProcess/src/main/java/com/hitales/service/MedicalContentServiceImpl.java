package com.hitales.service;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.config.MongoDataSourceConfig;
import com.hitales.common.support.Mapping;
import com.hitales.common.support.MappingMatch;
import com.hitales.common.support.TextFormatter;
import com.hitales.dao.standard.IMedicalHistoryDao;
import com.hitales.dao.standard.TextDao;
import com.hitales.entity.MedicalHistory;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service("medicalContentService")
public class MedicalContentServiceImpl extends TextService<MedicalHistory> {

    @Autowired
    @Qualifier("medicalContentDao")
    IMedicalHistoryDao medicalHistoryDao;

    @Autowired
    @Qualifier(MongoDataSourceConfig.HRS_MONGO_TEMPLATE)
    protected MongoTemplate hrsMongoTemplate;

    @Override
    protected TextDao<MedicalHistory> currentDao() {
        return medicalHistoryDao;
    }

    @Override
    protected void initProcess() {
        if (StringUtils.isEmpty(super.getXmlPath())) {
            throw new RuntimeException("no xml path!");
        }
        medicalHistoryDao.initXmlPath(super.getXmlPath());
    }

    @Override
    protected void customProcess(Record record, MedicalHistory entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        //通過mapping set对应的类型
        setRecordType(record, entity);

        String groupRecordName = entity.getGroupRecordName();
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            //====上海六院病历文书特殊逻辑处理===
            List<Map<String, Object>> icdList = medicalHistoryDao.getJdbcTemplate(dataSource).queryForList("select ID from shly_in_patient_visit_record_20180423 where AdmissionNumber=? group by AdmissionNumber,ID", groupRecordName);
            if (icdList != null || !icdList.isEmpty()) {
                String encounterID = icdList.get(0).get("ID").toString();
                List<String> orgOdCategories = medicalHistoryDao.findOrgOdCatByGroupRecordName(dataSource, encounterID);
                if (orgOdCategories != null && !orgOdCategories.isEmpty()) {
                    orgOdCatCaches.put(groupRecordName, orgOdCategories);
                }
            }
            //====end====
        }
        List<String> orgOds = orgOdCatCaches.get(groupRecordName);
        if (orgOds != null && !orgOds.isEmpty()) {
            record.setOrgOdCategories(orgOds.toArray(new String[0]));
        }
        if (patientCaches.isEmpty() || StringUtils.isEmpty(patientCaches.get(groupRecordName))) {
            String patientId = medicalHistoryDao.findRequiredColByCondition(dataSource, groupRecordName);
            if (!StringUtils.isEmpty(patientId)) {
                patientCaches.put(groupRecordName, patientId);
            }
        }
        record.setPatientId(patientCaches.get(groupRecordName) == null ? "" : patientCaches.get(groupRecordName));
    }

    @Override
    protected Map<String, String> getFormattedText(MedicalHistory entity) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        return null;
    }

    @Override
    protected void customInitInfo(Record record, MedicalHistory medicalHistory) {
        record.setGroupRecordName(medicalHistory.getGroupRecordName());
        record.setSourceId(medicalHistory.getId().toString());
        record.setSourceRecordType(medicalHistory.getMedicalHistoryName());
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
//        String text = TextFormatter.formatTextByAnchaor(medicalContent);
        info.put(TextFormatter.TEXT, medicalContent);

        String temp = medicalContent.replaceAll("【【", "");
        String textARS = temp.replaceAll("】】", "");
        info.put(TextFormatter.TEXT_ARS, textARS);

//        anchorMatch(medicalContent, record);
    }

    private void setRecordType(Record record, MedicalHistory medicalHistory) {
        String medicalHistoryName = medicalHistory.getMedicalHistoryName();
        if (StringUtils.isEmpty(medicalHistoryName)) {
            log.error("!!!!!!!!!!!! mapping is empty , id : " + medicalHistory.getId() + "!!!!!!!!!!");
            return;
        }
        List<Mapping> mapping = null;
        synchronized (this) {
            mapping = hrsMongoTemplate.findAll(Mapping.class, "Mapping");
            if (mapping == null || mapping.isEmpty()) {
                MappingMatch.addMappingRule(hrsMongoTemplate);
                mapping = hrsMongoTemplate.findAll(Mapping.class, "Mapping");
            }
        }

        String mappedValue = MappingMatch.getMappedValue(mapping, medicalHistoryName);

        String[] types = mappedValue.split("-");
        if (types.length != 2) {
            log.error("!!!!!!!!!!!! mapping value is invalid , id : " + medicalHistory.getId() + "!!!!!!!!!!");
            return;
        }
        record.setRecordType(types[0]);
        record.setSubRecordType(types[1]);
    }

    @Override
    protected boolean validateRecord(Record record) {
        if (!super.validateRecord(record)) {
            return false;
        }
        Object testARS = record.getInfo().get(TextFormatter.TEXT_ARS);
        //如果文本字符少于20则不入库
        if (StringUtils.isEmpty(testARS.toString()) || testARS.toString().length() < 20) {
            log.info("字符少于20,不做入库处理,id:" + record.getSourceId());
            return false;
        }
        String recordType = record.getRecordType();
        //对于入出院记录，如果字符小于300，则属于其他类型
        if (("入院记录".equals(recordType) || "出院记录".equals(recordType)) && testARS.toString().length() < 300) {
            log.info("字符过小修改为其他类型,id:" + record.getSourceId());
            record.setRecordType("其他记录");
            record.setSubRecordType("其他");
        }
        return true;
    }

    /**
     * 锚点匹配操作，目前只对出入院做处理
     *
     * @param anchorContent
     * @param record
     */
    protected void anchorMatch(String anchorContent, Record record) {
        if (!("入院记录".equals(record.getRecordType()) || "出院记录".equals(record.getRecordType()))) {
            return;
        }
        String[] inHospital = {"现病史", "个人史", "婚育史", "月经史", "家族史"};
        String[] outHospital = {"治疗经过", "诊疗经过", "出院指导", "出院医嘱", "出院诊断"};

        Pattern pattern = Pattern.compile("【【(.*?)】】");
        Matcher matcher = pattern.matcher(anchorContent);

        Map<String, String> anchors = new HashMap<>();
        while (matcher.find()) {
            String group = matcher.group(1);
            if (group == null) {
                continue;
            }
            group.trim();
            if ("".equals(group)) {
                continue;
            }
            anchors.put(matcher.group(1), null);
        }
        int inCount = 0;
        int outCount = 0;
        for (String in : inHospital) {
            if (anchors.containsKey(in)) {
                inCount++;
            }
        }
        for (String out : outHospital) {
            if (anchors.containsKey(out)) {
                outCount++;
            }
        }
        boolean matched = false;
        if ("入院记录".equals(record.getRecordType())) {
            if (inCount >= 2) {
                matched = true;
            }
            if (inCount < 2 && outCount >= 2) {
                matched = true;
                log.info("匹配锚点个数为：" + inCount + "，修改为出院记录,sourceRecordType:" + record.getSourceRecordType() + ",id:" + record.getSourceId());
                record.setRecordType("出院记录");
                if (anchorContent.contains("死亡时间")) {
                    record.setSubRecordType("死亡记录");
                } else if (anchorContent.contains("出院小结")) {
                    record.setSubRecordType("出院小结");
                } else {
                    record.setSubRecordType("出院记录");
                }
            }
        }
        if ("出院记录".equals(record.getRecordType())) {
            if (outCount >= 2) {
                matched = true;
            }
            if (outCount < 2 && inCount >= 2) {
                matched = true;
                log.info("匹配锚点个数为：" + outCount + "，修改为入院记录,sourceRecordType:" + record.getSourceRecordType() + ",id:" + record.getSourceId());
                record.setRecordType("入院记录");

                if (anchorContent.contains("小时内入出院")) {
                    record.setSubRecordType("24小时内入出院");
                } else if (anchorContent.contains("病案首页")) {
                    record.setSubRecordType("病案首页");
                } else {
                    record.setSubRecordType("入院记录");
                }
            }
        }
        /*if (!matched) {
            log.info("沒有匹配修改为其他记录,sourceRecordType:" + record.getSourceRecordType() + ",id:" + record.getSourceId());
            record.setRecordType("其他记录");
            record.setSubRecordType("其他");
        }*/
    }

}
