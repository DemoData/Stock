package com.hitales.service;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.standard.INewAssayDao;
import com.hitales.dao.standard.TableDao;
import com.hitales.entity.LabBasic;
import com.hitales.entity.LabDetail;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service("assayService")
public class AssayServiceImpl extends TableService<Map<String, Object>> {

    @Autowired
    @Qualifier("assayDao")
    private INewAssayDao assayDao;

    @Override
    protected String[] getArrayCondition(Record record) {
        //这里是检验申请号
        return new String[]{record.getId()};
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> groupRecordCaches, String dataSource) {
        initBasicInfo(record, dataSource);

        String jzid = record.getId();//就诊id
        if (StringUtils.isEmpty(jzid)) {
            return;
        }
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(jzid))) {
            List<String> orgOdCategories = assayDao.findOrgOdCatByGroupRecordName(dataSource, jzid);
            orgOdCatCaches.put(jzid, orgOdCategories);
        }
        if (groupRecordCaches.isEmpty() || StringUtils.isEmpty(groupRecordCaches.get(jzid))) {
            String groupRecordName = assayDao.findRequiredColByCondition(dataSource, jzid);
            groupRecordCaches.put(jzid, groupRecordName);
        }

        record.setOrgOdCategories(orgOdCatCaches.get(jzid).toArray(new String[0]));
        //一次就诊号
        record.setGroupRecordName(groupRecordCaches.get(jzid));

        Map<Object, Object> basicInfo = (Map<Object, Object>) super.getBasicInfo();
        //需要给pid 加上前缀
        record.setPatientId(basicInfo.get("patientPrefix").toString() + record.getPatientId());
    }

    @Override
    protected void initBasicInfo(Record record, String dataSource) {
        List<Map<String, Object>> applyList = assayDao.findBasicArrayByCondition(dataSource, record.getId());
        if (applyList == null || applyList.isEmpty()) {
            return;
        }
        JSONObject basicInfo = record.getInfo().getJSONObject("basicInfo");
        //init detail array
        List<String> names = new ArrayList<>();
        if (assayDao.isMultiple()) {
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
                    log.info("initBasicInfo(): assay name is empty:" + assay.toString());
                    continue;
                }
                names.add(assay.get(nameKey).toString());
            }
        }
        basicInfo.put(LabBasic.ColumnMapping.ASSAY_NAME.value(), names.size() == 1 ? names.get(0) : names.toArray(new String[]{}));
        Map<String, Object> assayApply = applyList.get(0);

        for (Map.Entry<String, Object> entry : assayApply.entrySet()) {
            String key = entry.getKey();
            if (key.contains("@")) {
                key = key.replace("@", EMPTY_FLAG);
            }
            Object value = entry.getValue();
            if (value == null) {
                value = EMPTY_FLAG;
            }
            basicInfo.put(key, value);
        }
    }

    @Override
    protected TableDao<Map<String, Object>> currentDao() {
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
        List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
        if (detailArray == null || detailArray.isEmpty()) {
            log.debug("validateRecord(): detailArray is empty:" + record.toString());
            return false;
        }
        return true;
    }

}
