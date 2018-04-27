package com.hitales.service;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.util.TimeUtil;
import com.hitales.dao.standard.IPatientDao;
import com.hitales.entity.Patient;
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

    private String patientPrefix = null;

    @Override
    protected void initProcess() {
        if (StringUtils.isEmpty(super.getXmlPath())) {
            throw new RuntimeException("no xml path!");
        }
        patientDao.initXmlPath(super.getXmlPath());
    }

    @Override
    public JSONObject bean2Json(Object entity) {
        //TODO：优化为读取bean属性
        Patient patient = (Patient) entity;
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Patient.ColumnMapping.ID.value(), patientPrefix + patient.getPatientId());
        jsonObj.put(Patient.ColumnMapping.BATCH_NO.value(), batchNo);
        jsonObj.put(Patient.ColumnMapping.HOSPITAL_ID.value(), hospitalId);
        jsonObj.put(Patient.ColumnMapping.CREATE_TIME.value(), patient.getCreateTime());
        jsonObj.put(Patient.ColumnMapping.UPDATE_TIME.value(), patient.getUpdateTime());
        jsonObj.put(Patient.ColumnMapping.SEX.value(), StringUtils.isEmpty(patient.getSex()) ? EMPTY_FLAG : patient.getSex());
        jsonObj.put(Patient.ColumnMapping.AGE.value(), StringUtils.isEmpty(patient.getAge()) ? EMPTY_FLAG : patient.getAge());
        jsonObj.put(Patient.ColumnMapping.BIRTHDAY.value(), StringUtils.isEmpty(patient.getBirthDay()) ? EMPTY_FLAG : patient.getBirthDay());
        jsonObj.put(Patient.ColumnMapping.NAME.value(), StringUtils.isEmpty(patient.getName()) ? EMPTY_FLAG : patient.getName());
        jsonObj.put(Patient.ColumnMapping.ADDRESS.value(), StringUtils.isEmpty(patient.getAddress()) ? EMPTY_FLAG : patient.getAddress());
        jsonObj.put(Patient.ColumnMapping.ORIGIN.value(), StringUtils.isEmpty(patient.getOrigin()) ? EMPTY_FLAG : patient.getOrigin());
        jsonObj.put(Patient.ColumnMapping.MARRIAGE.value(), StringUtils.isEmpty(patient.getMarriage()) ? EMPTY_FLAG : patient.getMarriage());
        jsonObj.put(Patient.ColumnMapping.BLOOD_TYPE.value(), StringUtils.isEmpty(patient.getBloodType()) ? EMPTY_FLAG : patient.getBloodType());
        jsonObj.put(Patient.ColumnMapping.NATION.value(), StringUtils.isEmpty(patient.getNation()) ? EMPTY_FLAG : patient.getNation());
        jsonObj.put(Patient.ColumnMapping.JOB.value(), StringUtils.isEmpty(patient.getJob()) ? EMPTY_FLAG : patient.getJob());
        jsonObj.put("isForged", patient.isForged());
        return jsonObj;
    }

    @Override
    protected void runStart(String dataSource, Integer startPage, Integer endPage) {
        log.info(">>>>>>>>>Starting process from dataSource: " + dataSource);
        int pageNum = startPage;
        boolean isFinish = false;
        Long count = 0L;
        Map<String, JSONObject> patientMap = new HashMap<>();
        initial();
        while (!isFinish) {
            if (pageNum >= endPage) {
                isFinish = true;
                continue;
            }
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
                JSONObject result = patientDao.findPatientByIdInHRS(patientPrefix + patient.getPatientId());
                if (result != null) {
                    log.debug("process(): Patient : " + patient.getPatientId() + " already exist in DB");
                    continue;
                }
                patient.setCreateTime(currentTimeMillis);
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
            //大小大于30000清空
            if (patientMap.size() > 30000) {
                patientMap.clear();
            }
        }
        log.info(">>>>>>>>>>>total inserted patients: " + count + " from " + dataSource + ",currentTimeMillis:" + currentTimeMillis);
    }

    private void initial() {
        Object basicInfo = super.getBasicInfo();
        if (basicInfo instanceof Map) {
            Map info = ((Map) basicInfo);
            if (info == null || info.isEmpty()) {
                throw new RuntimeException("No basic info!");
            }
            this.hospitalId = info.get("hospitalId").toString();
            this.batchNo = info.get("batchNo").toString();
            this.patientPrefix = info.get("patientPrefix").toString();
        }
    }

    @Override
    protected Integer getCount(String dataSource) {
        return patientDao.getCount(dataSource);
    }

}
