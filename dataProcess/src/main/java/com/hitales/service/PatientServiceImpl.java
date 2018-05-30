package com.hitales.service;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.support.ProgressCount;
import com.hitales.common.util.TimeUtil;
import com.hitales.dao.standard.IPatientDao;
import com.hitales.entity.Patient;
import com.hitales.entity.StockInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aron
 * @date 2018.02.27
 */
@Slf4j
@Service("patientService")
public class PatientServiceImpl extends BaseService {

    @Autowired
    @Qualifier("patientDao")
    IPatientDao patientDao;

    private Long currentTimeMillis = TimeUtil.getCurrentTimeMillis();

    private String batchNo = null;
    //﻿上海长海
    private String hospitalId = null;

//    private String patientPrefix = null;

    @Override
    protected void initProcess() {
        if (StringUtils.isEmpty(super.getXmlPath())) {
            throw new RuntimeException("no xml path!");
        }
        patientDao.initXmlPath(super.getXmlPath());

        initial();
    }

    @Override
    public JSONObject bean2Json(Object entity) {
        Patient patient = (Patient) entity;
        patient.setBatchNo(batchNo);
        patient.setHospitalId(hospitalId);
        patient.setCreateTime(currentTimeMillis);
        return super.bean2Json(patient);
    }

    @Override
    protected void runStart(String dataSource, Integer startPage, Integer endPage) {
        log.info(">>>>>>>>>Starting process from dataSource: " + dataSource);
        int pageNum = startPage;
        boolean isFinish = false;
        Long count = 0L;
        Map<String, JSONObject> patientMap = new HashMap<>();
        while (!isFinish) {
            if (pageNum >= endPage) {
                isFinish = true;
                ProgressCount.putProgress(batchNo+"-patient", 95);
                continue;
            }
            //只两位小数
            String progressCount = String.format("%.2f", (pageNum + 0.0) / (endPage + 0.0));
            Integer progressValue = Integer.valueOf(progressCount.substring(progressCount.indexOf(".") + 1));
            ProgressCount.putProgress("patient", progressValue >= 95 ? 95 : progressValue);

            List<Patient> patientList = patientDao.findPatients(dataSource, pageNum, getPageSize());
            if (patientList != null && patientList.size() < getPageSize()) {
                isFinish = true;
            }
            if (patientList == null || patientList.isEmpty()) {
                continue;
            }
            for (Patient patient : patientList) {
                if (StringUtils.isEmpty(patient.getPatientId())) {
                    continue;
                }
                //对于重复的PID只取一个
                if (patientMap.containsKey(patient.getPatientId())) {
                    continue;
                }
                //如果patient已近存在于mongodb中则不再插入
                JSONObject result = patientDao.findPatientByIdInHRS(patient.getPatientId());
                if (result != null) {
                    log.debug("process(): Patient : " + patient.getPatientId() + " already exist in DB");
                    continue;
                }
                JSONObject patientJson = this.bean2Json(patient);
                patientMap.put(patient.getPatientId(), patientJson);
            }
            //插入到mongodb中
            log.info("inserting available patient count: " + patientMap.size());
            List<JSONObject> insertList = new ArrayList<>();
            for (Map.Entry<String, JSONObject> entry : patientMap.entrySet()) {
                if (entry != null) {
                    insertList.add(entry.getValue());
                }
            }
            count += insertList.size();
            patientDao.batchInsert2HRS(insertList);
            pageNum++;
            //5000清空
            if (patientMap.size() >= 5000) {
                patientMap.clear();
            }
        }
        log.info(">>>>>>>>>>>total inserted patients: " + count + " from " + dataSource + ",currentTimeMillis:" + currentTimeMillis);
    }

    private void initial() {
        Object basicInfo = super.getBasicInfo();
        if (basicInfo instanceof StockInfo) {
            StockInfo info = ((StockInfo) basicInfo);
            if (info == null) {
                throw new RuntimeException("No basic info!");
            }
            this.hospitalId = info.getHospitalId();
            this.batchNo = info.getPrefix() + info.getBatchNo();
        }
    }

    @Override
    protected Integer getCount(String dataSource) {
        return patientDao.getCount(dataSource);
    }

}
