package com.hitales.service;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.constant.CommonConstant;
import com.hitales.common.support.TextFormatter;
import com.hitales.dao.standard.TextDao;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * text数据类型服务类
 *
 * @author aron
 */
@Slf4j
public abstract class TextService<T> extends BaseService {

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
            List<T> resultList = currentDao().findRecord(dataSource, startPage, getPageSize());
            if (resultList == null || resultList.isEmpty()) {
                log.info("runStart(): can not found any record");
                break;
            }
            if (resultList != null && resultList.size() < getPageSize()) {
                isFinish = true;
            }

            List<JSONObject> jsonList = new ArrayList<>();
            //遍历record
            for (T entity : resultList) {
                Record record = new Record();
                initial(record, entity, dataSource);

                customProcess(record, entity, orgOdCatCaches, patientCaches, dataSource);
                putText2Record(entity, record);

                //校验Record,不满足则跳过
                if (!validateRecord(record)) {
                    continue;
                }
                postProcess(entity, record, jsonList);
            }
            if (orgOdCatCaches.size() > 50000) {
                orgOdCatCaches.clear();
            }
            if (patientCaches.size() > 50000) {
                patientCaches.clear();
            }
            count += jsonList.size();
            log.info("inserting record total count: " + jsonList.size());
            //把找到的record插入到mongodb hrs record中
            currentDao().batchInsert2HRS(jsonList, "Record");
            pageNum++;
        }
        log.info(">>>>>>>>>>>total inserted records: " + count + " from " + dataSource);
    }

    private void initial(Record record, T entity, String dataSource) {
        initRecordBasicColumn(record);

        customInitInfo(record, entity);

    }

    protected void postProcess(T entity, Record record, List<JSONObject> jsonList) {
        JSONObject jsonObject = bean2Json(record);
        jsonObject.put("_id", ObjectId.get().toString());
        jsonList.add(jsonObject);
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

    protected abstract void customInitInfo(Record record, T entity);

    protected abstract TextDao<T> currentDao();

    protected abstract void customProcess(Record record, T entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource);

    @Override
    protected Integer getCount(String dataSource) {
        return currentDao().getCount(dataSource);
    }

    protected void putText2Record(T entity, Record record) {
        JSONObject info = record.getInfo();
        //调用提供的方法得到锚点文本
        Map<String, String> stringMap = null;
        try {
            stringMap = getFormattedText(entity);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        info.put(TextFormatter.TEXT, stringMap.get(TextFormatter.TEXT));
        info.put(TextFormatter.TEXT_ARS, stringMap.get(TextFormatter.TEXT_ARS));
    }

    protected abstract Map<String, String> getFormattedText(T entity) throws IntrospectionException, InvocationTargetException, IllegalAccessException;


    /**
     * Set Record basic info
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

}