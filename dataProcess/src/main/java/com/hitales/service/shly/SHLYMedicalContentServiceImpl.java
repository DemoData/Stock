package com.hitales.service.shly;

import com.hitales.entity.MedicalHistory;
import com.hitales.entity.Record;
import com.hitales.service.MedicalContentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service("shlyMedicalContentService")
public class SHLYMedicalContentServiceImpl extends MedicalContentServiceImpl {


    @Override
    protected void customProcess(Record record, MedicalHistory entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        //通過mapping set对应的类型
        super.setRecordType(record, entity);

        String groupRecordName = entity.getGroupRecordName();
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            //====上海六院病历文书特殊逻辑处理===
            List<Map<String, Object>> icdList = getMedicalHistoryDao().getJdbcTemplate(dataSource).queryForList("select ID from shly_in_patient_visit_record_20180423 where AdmissionNumber=? group by AdmissionNumber,ID", groupRecordName);
            if (icdList != null || !icdList.isEmpty()) {
                String encounterID = icdList.get(0).get("ID").toString();
                List<String> orgOdCategories = getMedicalHistoryDao().findOrgOdCatByGroupRecordName(dataSource, encounterID);
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
            String patientId = getMedicalHistoryDao().findRequiredColByCondition(dataSource, groupRecordName);
            if (!StringUtils.isEmpty(patientId)) {
                patientCaches.put(groupRecordName, patientId);
            }
        }
        record.setPatientId(patientCaches.get(groupRecordName) == null ? "" : patientCaches.get(groupRecordName));
    }


}
