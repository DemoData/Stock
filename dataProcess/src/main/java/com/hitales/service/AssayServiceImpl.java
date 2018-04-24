package com.hitales.service;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.standard.IAssayDao;
import com.hitales.dao.standard.TableDao;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service("assayService")
public class AssayServiceImpl extends TableService<Map<String, Object>, Map<String, Object>> {

    @Autowired
    @Qualifier("assayDao")
    private IAssayDao assayDao;

    @Override
    protected String[] getArrayCondition(Record record) {
        //这里是检验ID
        return new String[]{record.getId()};
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> groupRecordCaches, String dataSource) {
        Map<String, Object> condition = record.getCondition();
        Object encounterID = condition.get("EncounterID");//就诊id
        Object encounterType = condition.get("EncounterType");
        if (StringUtils.isEmpty(encounterID) || StringUtils.isEmpty(encounterType)) {
            return;
        }
        String encounterIDStr = encounterID.toString();
        String encounterTypeStr = encounterType.toString();
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(encounterIDStr))) {
            List<String> orgOdCategories = assayDao.findOrgOdCatByGroupRecordName(dataSource, encounterIDStr);
            if (orgOdCategories != null && !orgOdCategories.isEmpty()) {
                orgOdCatCaches.put(encounterIDStr, orgOdCategories);
            }
        }
        List<String> ods = orgOdCatCaches.get(encounterIDStr);
        if (ods != null && !ods.isEmpty()) {
            record.setOrgOdCategories(ods.toArray(new String[0]));
        }
        //0-门诊，1-住院，2-急诊，3-体检
        if ("1".equals(encounterTypeStr) && (groupRecordCaches.isEmpty() || StringUtils.isEmpty(groupRecordCaches.get(encounterIDStr)))) {
            String groupRecordName = assayDao.findRequiredColByCondition(dataSource, encounterIDStr);
            if (!StringUtils.isEmpty(groupRecordName)) {
                groupRecordCaches.put(encounterIDStr, groupRecordName);
            }
        }
        if (!StringUtils.isEmpty(groupRecordCaches.get(encounterIDStr))) {
            //一次就诊号
            record.setGroupRecordName(groupRecordCaches.get(encounterIDStr));
        }
        if (!"1".equals(encounterTypeStr)) {
            record.setGroupRecordName(encounterIDStr);
        }

    }

    @Override
    protected void initInfoBasic(Record record, String dataSource) {
        List<Map<String, Object>> applyList = assayDao.findBasicArrayByCondition(dataSource, record.getId());
        if (applyList == null || applyList.isEmpty()) {
            return;
        }
        JSONObject basicInfo = record.getInfo().getJSONObject("basicInfo");
        //init detail array
        List<String> names = new ArrayList<>();
        if (applyList.size() > 1) {
            String nameKey = null;
            for (Map<String, Object> assay : applyList) {
                if (nameKey == null) {
                    Set<String> keySet = assay.keySet();
                    for (String key : keySet) {
                        if (key.contains("@")) {
                            nameKey = key;
                            break;
                        }
                    }
                }
                if (StringUtils.isEmpty(assay.get(nameKey).toString())) {
                    log.info("initInfoBasic(): assay name is empty:" + assay.toString());
                    continue;
                }
                names.add(assay.get(nameKey).toString());
            }
            basicInfo.put(nameKey, names.size() == 1 ? names.get(0) : names.toArray(new String[]{}));
        }
        Map<String, Object> assayApply = applyList.get(0);

        for (Map.Entry<String, Object> entry : assayApply.entrySet()) {
            String key = entry.getKey();
            if (key.contains("@")) {
                key = key.replace("@", EMPTY_FLAG);
                //如果是多条数据，这里不再处理
                if (applyList.size() > 1) {
                    continue;
                }
            }
            Object value = entry.getValue();
            if (value == null) {
                value = EMPTY_FLAG;
            }
            basicInfo.put(key, value);
        }
    }

    @Override
    protected TableDao<Map<String, Object>, Map<String, Object>> currentDao() {
        return assayDao;
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

    /**
     * Set Record basic info
     *
     * @param record
     */
    protected void customInitInfo(Record record) {

    }

    protected void initInfoArray(Record record, List<Map<String, Object>> assayList) {
        if (assayList == null || assayList.isEmpty()) {
            return;
        }
        //init info
        List<Map<String, Object>> detailArray = record.getInfo().getObject("detailArray", List.class);
        //init detail array
        detailArray.addAll(assayList);
    }

    protected boolean validateRecord(Record record) {
        if (super.validateRecord(record)) {
            List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
            if (detailArray == null || detailArray.isEmpty()) {
                log.debug("validateRecord(): detailArray is empty:" + record.toString());
                return false;
            }
            return true;
        }
        return false;
    }

}
