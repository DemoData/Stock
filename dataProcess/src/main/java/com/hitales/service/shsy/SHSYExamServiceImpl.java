package com.hitales.service.shsy;

import com.hitales.entity.Record;
import com.hitales.service.HalfTextServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service("shsyExamService")
public class SHSYExamServiceImpl extends HalfTextServiceImpl {

    @Override
    protected void customProcess(Record record, Map<String, Object> inspection, Map<String, List<String>> orgOdCatCaches, Map<String, String> groupRecordCaches, String dataSource) {
        super.customProcess(record, inspection, orgOdCatCaches, groupRecordCaches, dataSource);
        String groupRecordName = record.getGroupRecordName();
        //通过住院号找不到就通过patientID找
        if (record.getOrgOdCategories().length == 0) {
            //如果cache中已近存在就不在重复查找
            if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
                List<String> orgOdCategories = getExamDao().findOrgOdCatByGroupRecordName(dataSource, "PatientID", groupRecordName);
                if (orgOdCategories != null && !orgOdCategories.isEmpty()) {
                    orgOdCatCaches.put(groupRecordName, orgOdCategories);
                }
            }
            List<String> ods = orgOdCatCaches.get(groupRecordName);
            if (ods != null && !ods.isEmpty()) {
                record.setOrgOdCategories(ods.toArray(new String[0]));
            }
        }
    }

}