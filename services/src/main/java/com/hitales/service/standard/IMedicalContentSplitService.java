package com.hitales.service.standard;

import com.alibaba.fastjson.JSONObject;

import java.util.Set;

public interface IMedicalContentSplitService {

    boolean medicalContentSplit(String type);

    Set<String> datacul(String sql);

    Integer dataculAdd(String sql);

    boolean importYXXGExcel();

    JSONObject pandian();

    JSONObject mongoPandian(String batchNo);
}
