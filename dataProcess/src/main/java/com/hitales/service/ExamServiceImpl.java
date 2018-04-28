package com.hitales.service;

import com.alibaba.fastjson.JSONObject;
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
@Service("examService")
public class ExamServiceImpl extends TextService<Map<String, Object>> {

    @Autowired
    @Qualifier("examDao")
    private IExamDao examDao;

    @Override
    protected TextDao<Map<String, Object>> currentDao() {
        return examDao;
    }

    @Override
    protected void initProcess() {
        if (StringUtils.isEmpty(super.getXmlPath())) {
            throw new RuntimeException("no xml path!");
        }
        examDao.initXmlPath(super.getXmlPath());
    }

    @Override
    protected void customProcess(Record record, Map<String, Object> inspection, Map<String, List<String>> orgOdCatCaches, Map<String, String> groupRecordCaches, String dataSource) {
        Object encounterID = inspection.get("就诊ID");//就诊id
        Object encounterType = inspection.get("就诊类型");
        if (StringUtils.isEmpty(encounterID) || StringUtils.isEmpty(encounterType)) {
            return;
        }
        String encounterIDStr = encounterID.toString();
        String encounterTypeStr = encounterType.toString();
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(encounterIDStr))) {
            List<String> orgOdCategories = examDao.findOrgOdCatByGroupRecordName(dataSource, encounterIDStr);
            if (orgOdCategories != null && !orgOdCategories.isEmpty()) {
                orgOdCatCaches.put(encounterIDStr, orgOdCategories);
            }
        }
        List<String> ods = orgOdCatCaches.get(encounterIDStr);
        if (ods != null && !ods.isEmpty()) {
            record.setOrgOdCategories(ods.toArray(new String[0]));
        }
        //0-门诊，1-住院，2-急诊，3-体检
        if ("住院".equals(encounterTypeStr) && (groupRecordCaches.isEmpty() || StringUtils.isEmpty(groupRecordCaches.get(encounterIDStr)))) {
            String groupRecordName = examDao.findRequiredColByCondition(dataSource, encounterIDStr);
            if (!StringUtils.isEmpty(groupRecordName)) {
                groupRecordCaches.put(encounterIDStr, groupRecordName);
            }
        }
        if (!StringUtils.isEmpty(groupRecordCaches.get(encounterIDStr))) {
            //一次就诊号
            record.setGroupRecordName(groupRecordCaches.get(encounterIDStr));
        }
        if (!"住院".equals(encounterTypeStr)) {
            record.setGroupRecordName(encounterIDStr);
        }
    }

    @Override
    protected Map<String, String> getFormattedText(Map<String, Object> inspection) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        return TextFormatter.textFormatter(inspection);
    }

    @Override
    protected void customInitInfo(Record record, Map<String, Object> inspection) {
        record.setPatientId(inspection.get("patientId").toString());
        record.setSourceId(inspection.get("id").toString());
        //移除不需要的字段
        inspection.remove("id");
        inspection.remove("patientId");
    }

}
