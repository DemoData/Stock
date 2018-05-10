package com.hitales.service;

import com.hitales.common.support.TextFormatter;
import com.hitales.dao.standard.IExamDao;
import com.hitales.dao.standard.TextDao;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("textService")
public class HalfTextServiceImpl extends TextService<Map<String, Object>> {

    @Autowired
    @Qualifier("textDao")
    private IExamDao examDao;

    @Override
    protected TextDao<Map<String, Object>> currentDao() {
        return examDao;
    }

    @Override
    protected void customProcess(Record record, Map<String, Object> inspection, Map<String, List<String>> orgOdCatCaches, Map<String, String> groupRecordCaches, String dataSource) {
        String groupRecordName = record.getGroupRecordName();
        if (StringUtils.isEmpty(groupRecordName)) {
            Object encounterID = inspection.get("就诊ID");//就诊id
            Object encounterType = inspection.get("就诊类型");
            if (StringUtils.isEmpty(encounterID) || StringUtils.isEmpty(encounterType)) {
                return;
            }
            groupRecordName = encounterID.toString();
            String encounterTypeStr = encounterType.toString();

            //0-门诊，1-住院，2-急诊，3-体检
            if ("住院".equals(encounterTypeStr) && (groupRecordCaches.isEmpty() || StringUtils.isEmpty(groupRecordCaches.get(groupRecordName)))) {
                String groupRecordNameValue = examDao.findRequiredColByCondition(dataSource, groupRecordName);
                if (!StringUtils.isEmpty(groupRecordNameValue)) {
                    groupRecordCaches.put(groupRecordName, groupRecordNameValue);
                }
            }
            if (!StringUtils.isEmpty(groupRecordCaches.get(groupRecordName))) {
                //一次就诊号
                record.setGroupRecordName(groupRecordCaches.get(groupRecordName));
            }
            if (!"住院".equals(encounterTypeStr)) {
                record.setGroupRecordName(groupRecordName);
            }
        }
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = examDao.findOrgOdCatByGroupRecordName(dataSource, null, groupRecordName);
            if (orgOdCategories != null && !orgOdCategories.isEmpty()) {
                orgOdCatCaches.put(groupRecordName, orgOdCategories);
            }
        }
        List<String> ods = orgOdCatCaches.get(groupRecordName);
        if (ods != null && !ods.isEmpty()) {
            record.setOrgOdCategories(ods.toArray(new String[0]));
        }

    }

    @Override
    protected Map<String, String> getFormattedText(Map<String, Object> inspection) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        return TextFormatter.textFormatter(inspection);
    }

    @Override
    protected void customInitInfo(Record record, Map<String, Object> inspection) {
        Object pid = inspection.get("#patientId");
        Object grn = inspection.get("#groupRecordName");
        Object sid = inspection.get("#sourceId");
        if (pid != null) {
            record.setPatientId(pid.toString());
            inspection.remove("#patientId");
        }
        if (grn != null) {
            record.setGroupRecordName(grn.toString());
            inspection.remove("#groupRecordName");
        }
        if (sid != null) {
            record.setSourceId(sid.toString());
            inspection.remove("#sourceId");
        }
    }

    public IExamDao getExamDao() {
        return examDao;
    }
}
