package com.hitales.dao.standard;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.standard.IDataDao;

import java.util.List;

/**
 * @author aron
 */
public interface TextDao<T> extends IDataDao {
    void batchInsert2HRS(List<JSONObject> records, String collectionName);

    Integer getCount(String dataSource);

    List<T> findRecord(String dataSource, int pageNum, int pageSize);

}