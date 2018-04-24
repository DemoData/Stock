package com.hitales.dao.standard;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.standard.IDataDao;
import com.hitales.entity.Record;

import java.util.List;

/**
 * @author aron
 */
public interface TableDao<T> extends IDataDao {
    void batchInsert2HRS(List<JSONObject> records, String collectionName);

    Integer getCount(String dataSource);

    List<Record> findRecord(String dataSource, int pageNum, int pageSize);

    List<T> findArrayListByCondition(String dataSource, String... params);
}
