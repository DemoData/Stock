package com.hitales.service;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.standard.TableDao;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * table数据类型服务类
 *
 * @author aron
 */
@Slf4j
public abstract class TableService<Basic, Sub> extends BaseService {

    @Override
    protected void initProcess() {
        if (StringUtils.isEmpty(super.getXmlPath())) {
            throw new RuntimeException("no xml path!");
        }
        currentDao().initXmlPath(super.getXmlPath());
    }

    @Override
    protected void runStart(String dataSource, Integer startPage, Integer endPage) {
        log.info(">>>>>>>>>Starting process from dataSource: " + dataSource);
        int pageNum = startPage;
        boolean isFinish = false;
        Long count = 0L;
        Map<String, List<String>> orgOdCatCaches = new HashMap<>();
        Map<String, String> patientCaches = new HashMap<>();
        while (!isFinish) {
            if (pageNum >= endPage) {
                isFinish = true;
                continue;
            }
            List<Record> orderList = currentDao().findRecord(dataSource, startPage, getPageSize());

            if (orderList == null || orderList.isEmpty()) {
                log.info("runStart(): record is 0");
                break;
            }
            log.info("runStart(): found record " + orderList.size());
            //orderList.size() > getPageSize() 如果结果数据大于指定数量，则不分页处理
            if ((orderList != null && orderList.size() < getPageSize()) || orderList.size() > getPageSize()) {
                isFinish = true;
            }
            List<JSONObject> jsonList = new ArrayList<>();
            //遍历record
            for (Record record : orderList) {
                log.debug(record.toString());
                initial(record, dataSource);
                customProcess(record, orgOdCatCaches, patientCaches, dataSource);
                //校验Record,不满足则跳过
                if (!validateRecord(record)) {
                    continue;
                }
                postProcess(record, jsonList);
            }
            if (orgOdCatCaches.size() > 5000) {
                orgOdCatCaches.clear();
            }
            finalProcess(jsonList, orgOdCatCaches, patientCaches, dataSource);
            count += jsonList.size();
            log.info("inserting record total count: " + jsonList.size());
            batchInsert(jsonList);
            pageNum++;
        }
        log.info(">>>>>>>>>>>total inserted records: " + count + " from " + dataSource);
    }

    /**
     * 后续处理
     *
     * @param record
     * @param jsonList
     */
    private void postProcess(Record record, List<JSONObject> jsonList) {
        JSONObject jsonObject = bean2Json(record);
        jsonObject.put("_id", ObjectId.get().toString());
        jsonList.add(jsonObject);
    }

    /**
     * 初始化操作
     *
     * @param record
     * @param dataSource
     */
    private void initial(Record record, String dataSource) {
        initRecordBasicColumn(record);

        customInitInfo(record);

        initInfoBasic(record, dataSource);

        //设置detailArray值
        initInfoArray(record, currentDao().findArrayListByCondition(dataSource, getArrayCondition(record)));
    }

    protected void batchInsert(List<JSONObject> jsonList) {
        //把找到的record插入到mongodb hrs record中
        int pointsDataLimit = 1000;//限制条数
        if (jsonList.size() > pointsDataLimit) {
            Integer size = jsonList.size();
            int part = size / pointsDataLimit;//分批数
            log.info("共有 ： " + size + "条，！" + " 分为 ：" + part + "批");

            for (int i = 0; i < part; i++) {
                List<JSONObject> records = jsonList.subList(0, pointsDataLimit);
                currentDao().batchInsert2HRS(records, "Record");
                //clear
                jsonList.subList(0, pointsDataLimit).clear();
            }
            if (!jsonList.isEmpty()) {
                log.info("process after subList : " + jsonList.size());
                //处理遗留
                currentDao().batchInsert2HRS(jsonList, "Record");
            }
        } else {
            log.info("don't need to subList : " + jsonList.size());
            currentDao().batchInsert2HRS(jsonList, "Record");
        }
    }

    /**
     * 相关校验规则,用于子类去重写
     *
     * @param record
     * @return
     */
    protected boolean validateRecord(Record record) {
        String patientId = record.getPatientId();
        if (StringUtils.isEmpty(patientId)) {
            return false;
        }
        String groupRecordName = record.getGroupRecordName();
        if (StringUtils.isEmpty(groupRecordName)) {
            return false;
        }
        return true;
    }

    /**
     * info.basicInfo
     *
     * @param record
     * @param dataSource
     */
    protected void initInfoBasic(Record record, String dataSource) {

    }

    protected void finalProcess(List<JSONObject> jsonList, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
    }

    protected abstract String[] getArrayCondition(Record record);

    protected abstract void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource);

    protected abstract TableDao<Basic, Sub> currentDao();

    @Override
    protected Integer getCount(String dataSource) {
        return currentDao().getCount(dataSource);
    }

    /**
     * Set Record basic column
     *
     * @param record
     */
    protected void initRecordBasicColumn(Record record) {
        Record basicInfo = (Record) getBasicInfo();
        if (basicInfo == null) {
            return;
        }
        BeanUtils.copyProperties(basicInfo, record, getNullPropertyNames(basicInfo));
        record.setCreateTime(currentTimeMillis);
    }

    /**
     * 找到空属性名
     *
     * @param source
     * @return
     */
    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null || EMPTY_FLAG.equals(srcValue)) {
                emptyNames.add(pd.getName());
            }
        }
        //这里由于record info中存在集合，所以需要把info过滤
        emptyNames.add("info");
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    protected abstract void customInitInfo(Record record);

    protected abstract void initInfoArray(Record record, List<Sub> assayList);
}
