package com.hitales.service;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.standard.IAdviceDao;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service("tableService")
public class TableServiceImpl extends TableService<Map<String, Object>, Map<String, Object>> {

    @Autowired
    @Qualifier("tableDao")
    private IAdviceDao adviceDao;

    @Override
    protected String[] getArrayCondition(Record record) {
        return new String[]{record.getId()};
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> groupRecordCaches, String dataSource) {
        //TODO:customProcess 里面抽象一个类出来 通过装饰模式 来扩展多个功能和方法
        String identifiedStr = record.getGroupRecordName();
        if (StringUtils.isEmpty(identifiedStr)) {
            Map<String, Object> condition = record.getCondition();
            Object encounterID = condition.get("EncounterID");//就诊id
            Object encounterType = condition.get("EncounterType");
            if (StringUtils.isEmpty(encounterID) || StringUtils.isEmpty(encounterType)) {
                return;
            }
            identifiedStr = encounterID.toString();
            String encounterTypeStr = encounterType.toString();
            //0-门诊，1-住院，2-急诊，3-体检
            if ("1".equals(encounterTypeStr) && (groupRecordCaches.isEmpty() || StringUtils.isEmpty(groupRecordCaches.get(identifiedStr)))) {
                String realGroupRecordName = adviceDao.findRequiredColByCondition(dataSource, identifiedStr);
                if (!StringUtils.isEmpty(realGroupRecordName)) {
                    groupRecordCaches.put(identifiedStr, realGroupRecordName);
                }
            }
            if (!StringUtils.isEmpty(groupRecordCaches.get(identifiedStr))) {
                //一次就诊号
                record.setGroupRecordName(groupRecordCaches.get(identifiedStr));
            }
            if (!"1".equals(encounterTypeStr)) {
                record.setGroupRecordName(identifiedStr);
            }
        }

        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(identifiedStr))) {
            List<String> orgOdCategories = adviceDao.findOrgOdCatByGroupRecordName(dataSource, identifiedStr);
            if (orgOdCategories != null && !orgOdCategories.isEmpty()) {
                orgOdCatCaches.put(identifiedStr, orgOdCategories);
            }
        }
        List<String> ods = orgOdCatCaches.get(identifiedStr);
        if (ods != null && !ods.isEmpty()) {
            record.setOrgOdCategories(ods.toArray(new String[0]));
        }
    }

    @Override
    protected void initInfoBasic(Record record, String dataSource) {
        List<Map<String, Object>> applyList = adviceDao.findBasicArrayByCondition(dataSource, record.getId());
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
            basicInfo.put(nameKey.substring(1), names.size() == 1 ? names.get(0) : names.toArray(new String[]{}));
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
        return adviceDao;
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
        //处理数值中文和非中文
        if ("化验记录".equals(record.getRecordType())) {
            for (Map<String, Object> map : assayList) {
                String textValue = map.get("文本结果") == null ? "" : map.get("文本结果").toString();
                String numValue = map.get("数值结果") == null ? "" : map.get("数值结果").toString();
                String chineseRegex = "([\\u4e00-\\u9fa5]+)";
                String nonChineseRegex = "([^\\u4e00-\\u9fa5]+)";
                if (!StringUtils.isEmpty(textValue)) {
                    filterValue(chineseRegex, "文本结果", textValue, map);
                }
                if (!StringUtils.isEmpty(numValue)) {
                    filterValue(nonChineseRegex, "数值结果", numValue, map);
                }
                if (StringUtils.isEmpty(numValue) && !StringUtils.isEmpty(textValue)) {
                    filterValue(nonChineseRegex, "数值结果", textValue, map);
                }
                if (StringUtils.isEmpty(textValue) && !StringUtils.isEmpty(numValue)) {
                    filterValue(chineseRegex, "文本结果", numValue, map);
                }
            }
        }
        //init info
        List<Map<String, Object>> detailArray = record.getInfo().getObject("detailArray", List.class);
        //init detail array
        detailArray.addAll(assayList);
    }

    private void filterValue(String regex, String key, String value, Map<String, Object> map) {
        StringBuffer temp = new StringBuffer();
        Matcher matcher = Pattern.compile(regex).matcher(value);
        while (matcher.find()) {
            temp.append(matcher.group(0));
        }
        map.put(key, temp.toString());
    }

    protected boolean validateRecord(Record record) {
        //TODO:通过配置文件优化制定哪些需要验证
        if (!super.validateRecord(record)) {
            return false;
        }
        if ("化验记录".equals(record.getRecordType())) {
            List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
            if (detailArray == null || detailArray.isEmpty()) {
                log.debug("validateRecord(): detailArray is empty:" + record.toString());
                return false;
            }
            return true;
        }
        return true;
    }

    public IAdviceDao getAdviceDao() {
        return adviceDao;
    }
}
