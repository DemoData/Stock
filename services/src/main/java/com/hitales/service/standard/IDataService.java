package com.hitales.service.standard;

import com.hitales.entity.Record;

/**
 * @author aron
 * @date 2018.02.27
 */
public interface IDataService {
    boolean processData();

    Record getBasicInfo();
}
