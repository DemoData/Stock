package com.hitales.service;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.constant.CommonConstant;
import com.hitales.dao.TableDao;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class TableService<T> extends BaseService {

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
                record.setOdCategories(new String[]{getOdCategory(dataSource)});
                record.setOrgOdCategories(new String[]{CommonConstant.EMPTY_FLAG});

                initBasicInfo(record, dataSource);
                initRecordBasicInfo(record);

                customProcess(record, orgOdCatCaches, patientCaches, dataSource);

                //查找医嘱通过patientId
                initInfoArray(record, currentDao().findArrayListByCondition(dataSource, getArrayCondition(record)));
                //校验Record,不满足则跳过
                if (!validateRecord(record)) {
                    continue;
                }
                //移除id,添加string类型_id
                JSONObject jsonObject = bean2Json(record);
                jsonObject.remove("id");
                jsonObject.remove("reportDate");
                jsonObject.put("_id", new ObjectId().toString());
                jsonList.add(jsonObject);
            }
            if (orgOdCatCaches.size() > 50000) {
                orgOdCatCaches.clear();
            }
            postProcess(jsonList, orgOdCatCaches, patientCaches, dataSource);
            count += jsonList.size();
            log.info("inserting record total count: " + jsonList.size());
            batchInsert(jsonList);
            pageNum++;
        }
        log.info(">>>>>>>>>>>total inserted records: " + count + " from " + dataSource);
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
        return true;
    }

    /**
     * info.basicInfo
     *
     * @param record
     * @param dataSource
     */
    protected void initBasicInfo(Record record, String dataSource) {

    }

    protected void postProcess(List<JSONObject> jsonList, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
    }

    protected abstract String[] getArrayCondition(Record record);

    protected abstract void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource);

    protected abstract TableDao<T> currentDao();

    @Override
    protected Integer getCount(String dataSource) {
        return currentDao().getCount(dataSource);
    }


    /**
     * Set Record basic info
     *
     * @param record
     */
    protected abstract void initRecordBasicInfo(Record record);

    protected abstract void initInfoArray(Record record, List<T> assayList);
}
