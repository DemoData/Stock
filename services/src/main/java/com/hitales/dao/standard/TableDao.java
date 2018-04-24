package com.hitales.dao.standard;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.standard.IDataDao;
import com.hitales.entity.LabDetail;
import com.hitales.entity.Record;

import javax.persistence.Basic;
import java.util.List;

/**
 * @author aron
 */
public interface TableDao<Basic,Sub> extends IDataDao {
    void batchInsert2HRS(List<JSONObject> records, String collectionName);

    Integer getCount(String dataSource);

    List<Record> findRecord(String dataSource, int pageNum, int pageSize);

    List<Sub> findArrayListByCondition(String dataSource, String... params);

    List<Basic> findBasicArrayByCondition(String dataSource, String applyId);

}
